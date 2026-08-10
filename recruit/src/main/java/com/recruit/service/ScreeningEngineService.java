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

    @Data
    public static class RuleMatchDetail {
        private Long ruleId;
        private String ruleName;
        private String ruleType;
        private int weight;
        private int score;
        private boolean matched;
        private List<String> matchedValues;
        private List<String> unmatchedValues;
        private String reason;
    }
}
