package com.recruit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.common.BusinessException;
import com.recruit.entity.*;
import com.recruit.mapper.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 智能简历筛选引擎服务
 * <p>
 * 基于岗位配置的筛选规则，对候选人简历进行多维度智能匹配评分。
 * 支持三种规则类型：
 * <ul>
 *   <li>SKILL - 技能标签匹配（ANY/ALL模式）</li>
 *   <li>KEYWORD - 简历关键词匹配（ANY/ALL模式）</li>
 *   <li>EXPERIENCE - 工作经验年限匹配（MIN模式）</li>
 * </ul>
 * 评分规则：totalScore * 2 >= maxScore 即判定为通过。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningEngineService {

    private final ApplicationMapper applicationMapper;
    private final CandidateMapper candidateMapper;
    private final PositionMapper positionMapper;
    private final ScreenRuleService screenRuleService;
    private final ScreenResultMapper screenResultMapper;
    private final ObjectMapper objectMapper;

    /**
     * 对投递记录执行智能筛选
     *
     * @param applicationId 投递记录ID
     * @return 筛选结果（含得分、通过状态、规则匹配详情）
     * @throws BusinessException 404 投递记录/候选人/岗位不存在
     * @throws BusinessException 400 岗位暂无筛选规则
     */
    public ScreenResult screen(Long applicationId) {
        Application app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException(404, "投递记录不存在");
        }

        Candidate candidate = candidateMapper.selectById(app.getCandidateId());
        Position position = positionMapper.selectById(app.getPositionId());
        if (candidate == null || position == null) {
            throw new BusinessException(404, "候选人或职位信息不存在");
        }

        List<ScreenRule> rules = screenRuleService.listByPosition(position.getId());
        if (rules.isEmpty()) {
            throw new BusinessException(400, "该职位暂无筛选规则");
        }

        return doScreen(app, candidate, position, rules);
    }

    /**
     * 直接对候选人进行岗位匹配评估（无需投递记录）
     *
     * @param candidateId 候选人ID
     * @param positionId  岗位ID
     * @return 筛选结果
     */
    public ScreenResult screenCandidateDirect(Long candidateId, Long positionId) {
        Candidate candidate = candidateMapper.selectById(candidateId);
        Position position = positionMapper.selectById(positionId);
        if (candidate == null || position == null) {
            throw new BusinessException(404, "候选人或职位信息不存在");
        }

        List<ScreenRule> rules = screenRuleService.listByPosition(position.getId());
        if (rules.isEmpty()) {
            throw new BusinessException(400, "该职位暂无筛选规则");
        }

        Application dummyApp = new Application();
        dummyApp.setCandidateId(candidateId);
        dummyApp.setPositionId(positionId);

        return doScreen(dummyApp, candidate, position, rules);
    }

    /**
     * 执行筛选核心逻辑
     * <p>
     * 遍历所有规则逐一评估，累加得分，计算通过率。
     * 通过条件：totalScore * 2 >= maxScore（即至少50%权重匹配）。
     */
    private ScreenResult doScreen(Application app, Candidate candidate, Position position, List<ScreenRule> rules) {
        List<RuleMatchDetail> details = new ArrayList<>();
        int totalScore = 0;
        int maxScore = 0;

        for (ScreenRule rule : rules) {
            maxScore += rule.getWeight();
            RuleMatchDetail detail = evaluateRule(rule, candidate, position);
            detail.setScore(detail.matched ? rule.getWeight() : 0);
            if (detail.matched) {
                totalScore += rule.getWeight();
            }
            details.add(detail);
        }

        boolean pass = maxScore > 0 && totalScore * 2 >= maxScore;

        ScreenResult result = new ScreenResult();
        result.setApplicationId(app.getId() != null ? app.getId() : -1L);
        result.setTotalScore(totalScore);
        result.setMaxScore(maxScore);
        result.setPass(pass ? 1 : 0);
        try {
            result.setRuleDetails(objectMapper.writeValueAsString(details));
        } catch (Exception e) {
            log.error("序列化规则详情失败", e);
            result.setRuleDetails("[]");
        }

        if (app.getId() != null && app.getId() > 0) {
            screenResultMapper.insert(result);
        }

        log.info("筛选完成: candidate={}, position={}, score={}/{}, pass={}",
                candidate.getName(), position.getTitle(), totalScore, maxScore, pass);

        return result;
    }

    /**
     * 评估单条规则匹配情况
     *
     * @param rule     筛选规则
     * @param candidate 候选人档案
     * @param position  岗位信息
     * @return 规则匹配详情
     */
    private RuleMatchDetail evaluateRule(ScreenRule rule, Candidate candidate, Position position) {
        RuleMatchDetail detail = new RuleMatchDetail();
        detail.ruleId = rule.getId();
        detail.ruleName = rule.getName();
        detail.ruleType = rule.getRuleType();
        detail.weight = rule.getWeight();

        String[] expectedValues = rule.getExpectedValues().split(",");
        for (int i = 0; i < expectedValues.length; i++) {
            expectedValues[i] = expectedValues[i].trim();
        }

        switch (rule.getRuleType()) {
            case "SKILL":
                return evaluateSkillRule(rule, candidate, expectedValues, detail);
            case "KEYWORD":
                return evaluateKeywordRule(rule, candidate, expectedValues, detail);
            case "EXPERIENCE":
                return evaluateExperienceRule(rule, candidate, expectedValues, detail);
            default:
                detail.matched = false;
                detail.reason = "未知规则类型: " + rule.getRuleType();
                return detail;
        }
    }

    /**
     * 技能匹配评估
     * <p>
     * ANY模式：任一技能匹配即通过；ALL模式：所有技能都需匹配。
     */
    private RuleMatchDetail evaluateSkillRule(ScreenRule rule, Candidate candidate,
                                               String[] expectedValues, RuleMatchDetail detail) {
        String candidateSkills = candidate.getSkills() != null ? candidate.getSkills().toLowerCase() : "";
        List<String> matchedValues = new ArrayList<>();
        List<String> unmatchedValues = new ArrayList<>();

        for (String expected : expectedValues) {
            if (candidateSkills.contains(expected.toLowerCase())) {
                matchedValues.add(expected);
            } else {
                unmatchedValues.add(expected);
            }
        }

        boolean isMatched;
        if ("ALL".equals(rule.getMatchMode())) {
            isMatched = unmatchedValues.isEmpty();
        } else {
            isMatched = !matchedValues.isEmpty();
        }

        detail.matched = isMatched;
        detail.matchedValues = matchedValues;
        detail.unmatchedValues = unmatchedValues;
        detail.reason = isMatched
                ? "技能匹配: " + String.join(", ", matchedValues)
                : "未匹配到任何要求技能";
        return detail;
    }

    /**
     * 关键词匹配评估
     * <p>
     * 在候选人简历文本中搜索指定关键词，支持ANY/ALL匹配模式。
     */
    private RuleMatchDetail evaluateKeywordRule(ScreenRule rule, Candidate candidate,
                                                String[] expectedValues, RuleMatchDetail detail) {
        String resumeText = candidate.getResumeText() != null ? candidate.getResumeText().toLowerCase() : "";
        List<String> matchedValues = new ArrayList<>();
        List<String> unmatchedValues = new ArrayList<>();

        for (String expected : expectedValues) {
            if (resumeText.contains(expected.toLowerCase())) {
                matchedValues.add(expected);
            } else {
                unmatchedValues.add(expected);
            }
        }

        boolean isMatched;
        if ("ALL".equals(rule.getMatchMode())) {
            isMatched = unmatchedValues.isEmpty();
        } else {
            isMatched = !matchedValues.isEmpty();
        }

        detail.matched = isMatched;
        detail.matchedValues = matchedValues;
        detail.unmatchedValues = unmatchedValues;
        detail.reason = isMatched
                ? "关键词匹配: " + String.join(", ", matchedValues)
                : "简历未包含要求关键词";
        return detail;
    }

    /**
     * 经验年限匹配评估
     * <p>
     * 校验候选人工作年限是否达到规则要求的最小值。
     */
    private RuleMatchDetail evaluateExperienceRule(ScreenRule rule, Candidate candidate,
                                                    String[] expectedValues, RuleMatchDetail detail) {
        int candidateYears = candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0;
        int minYears = Integer.parseInt(expectedValues[0]);

        boolean isMatched = candidateYears >= minYears;
        List<String> matchedValues;
        List<String> unmatchedValues;
        if (isMatched) {
            matchedValues = Collections.singletonList(String.valueOf(candidateYears));
            unmatchedValues = Collections.emptyList();
        } else {
            matchedValues = Collections.emptyList();
            unmatchedValues = Collections.singletonList("要求≥" + minYears + "年, 实际" + candidateYears + "年");
        }

        detail.matched = isMatched;
        detail.matchedValues = matchedValues;
        detail.unmatchedValues = unmatchedValues;
        detail.reason = isMatched
                ? "经验满足: " + candidateYears + "年 ≥ " + minYears + "年"
                : "经验不足: " + candidateYears + "年 < " + minYears + "年";
        return detail;
    }

    /**
     * 规则匹配详情（内部DTO）
     * <p>
     * 记录每条规则的匹配结果，用于前端展示筛选明细。
     */
    @Data
    public static class RuleMatchDetail {
        /** 规则ID */
        private Long ruleId;
        /** 规则名称 */
        private String ruleName;
        /** 规则类型 */
        private String ruleType;
        /** 规则权重 */
        private int weight;
        /** 实际得分 */
        private int score;
        /** 是否匹配 */
        private boolean matched;
        /** 已匹配的值 */
        private List<String> matchedValues;
        /** 未匹配的值 */
        private List<String> unmatchedValues;
        /** 匹配原因说明 */
        private String reason;
    }
}
