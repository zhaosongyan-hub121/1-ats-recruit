package com.recruit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.common.BusinessException;
import com.recruit.entity.Application;
import com.recruit.entity.Candidate;
import com.recruit.entity.Position;
import com.recruit.entity.ScreenResult;
import com.recruit.entity.ScreenRule;
import com.recruit.mapper.ApplicationMapper;
import com.recruit.mapper.CandidateMapper;
import com.recruit.mapper.PositionMapper;
import com.recruit.mapper.ScreenResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("筛选引擎服务单元测试")
class ScreeningEngineServiceTest {

    @Mock
    private ApplicationMapper applicationMapper;
    @Mock
    private CandidateMapper candidateMapper;
    @Mock
    private PositionMapper positionMapper;
    @Mock
    private ScreenRuleService screenRuleService;
    @Mock
    private ScreenResultMapper screenResultMapper;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ScreeningEngineService screeningEngineService;

    private Candidate candidate;
    private Position position;
    private Application application;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(1L);
        candidate.setName("张三");
        candidate.setSkills("Java,Spring Boot,MySQL");
        candidate.setExperienceYears(5);
        candidate.setResumeText("5年Java开发经验，熟悉Spring Boot、MyBatis、MySQL数据库，参与过电商项目后端开发");

        position = new Position();
        position.setId(10L);
        position.setTitle("高级Java工程师");
        position.setDepartment("技术部");

        application = new Application();
        application.setId(100L);
        application.setCandidateId(1L);
        application.setPositionId(10L);
    }

    private ScreenRule skillRule(String matchMode, String expectedValues, int weight) {
        ScreenRule r = new ScreenRule();
        r.setId(1L);
        r.setName("技能要求");
        r.setRuleType("SKILL");
        r.setExpectedValues(expectedValues);
        r.setMatchMode(matchMode);
        r.setWeight(weight);
        r.setEnabled(1);
        return r;
    }

    private ScreenRule keywordRule(String matchMode, String expectedValues, int weight) {
        ScreenRule r = new ScreenRule();
        r.setId(2L);
        r.setName("关键词匹配");
        r.setRuleType("KEYWORD");
        r.setExpectedValues(expectedValues);
        r.setMatchMode(matchMode);
        r.setWeight(weight);
        r.setEnabled(1);
        return r;
    }

    private ScreenRule experienceRule(String expectedValues, int weight) {
        ScreenRule r = new ScreenRule();
        r.setId(3L);
        r.setName("工作经验");
        r.setRuleType("EXPERIENCE");
        r.setExpectedValues(expectedValues);
        r.setMatchMode("MIN");
        r.setWeight(weight);
        r.setEnabled(1);
        return r;
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("投递记录不存在时抛出404异常")
    void screen_applicationNotFound_throws404() {
        when(applicationMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> screeningEngineService.screen(999L));
        assertEquals(404, ex.getCode());
        assertEquals("投递记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("候选人不存在时抛出404异常")
    void screen_candidateNotFound_throws404() {
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> screeningEngineService.screen(100L));
        assertEquals(404, ex.getCode());
        assertEquals("候选人或职位信息不存在", ex.getMessage());
    }

    @Test
    @DisplayName("职位无筛选规则时抛出400异常")
    void screen_noRules_throws400() {
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> screeningEngineService.screen(100L));
        assertEquals(400, ex.getCode());
        assertEquals("该职位暂无筛选规则", ex.getMessage());
    }

    // ==================== SKILL 规则 ====================

    @Test
    @DisplayName("SKILL-ANY模式：匹配任一技能即通过")
    void evaluateSkill_anyMode_matchOne() {
        List<ScreenRule> rules = Arrays.asList(skillRule("ANY", "Java,Dubbo", 40));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(40, result.getTotalScore());
        assertEquals(40, result.getMaxScore());
        assertEquals(1, result.getPass());
        verify(screenResultMapper, times(1)).insert(any(ScreenResult.class));
    }

    @Test
    @DisplayName("SKILL-ALL模式：全部技能匹配才通过")
    void evaluateSkill_allMode_allMatch() {
        List<ScreenRule> rules = Arrays.asList(skillRule("ALL", "Java,Spring Boot", 40));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(40, result.getTotalScore());
        assertEquals(1, result.getPass());
    }

    @Test
    @DisplayName("SKILL-ALL模式：部分技能未匹配则不通过")
    void evaluateSkill_allMode_partialUnmatch() {
        List<ScreenRule> rules = Arrays.asList(skillRule("ALL", "Java,Dubbo", 40));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(0, result.getTotalScore());
        assertEquals(0, result.getPass());
    }

    // ==================== KEYWORD 规则 ====================

    @Test
    @DisplayName("KEYWORD-ANY模式：简历包含任一关键词")
    void evaluateKeyword_anyMode_matchOne() {
        List<ScreenRule> rules = Arrays.asList(keywordRule("ANY", "电商,金融", 30));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(30, result.getTotalScore());
        assertEquals(1, result.getPass());
    }

    @Test
    @DisplayName("KEYWORD模式：简历无任何匹配关键词")
    void evaluateKeyword_noMatch() {
        List<ScreenRule> rules = Arrays.asList(keywordRule("ANY", "人工智能,区块链", 30));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(0, result.getTotalScore());
        assertEquals(0, result.getPass());
    }

    // ==================== EXPERIENCE 规则 ====================

    @Test
    @DisplayName("EXPERIENCE：经验年限满足要求")
    void evaluateExperience_enoughYears() {
        List<ScreenRule> rules = Arrays.asList(experienceRule("3", 30));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(30, result.getTotalScore());
        assertEquals(1, result.getPass());
    }

    @Test
    @DisplayName("EXPERIENCE：经验年限不足")
    void evaluateExperience_notEnoughYears() {
        candidate.setExperienceYears(1);
        List<ScreenRule> rules = Arrays.asList(experienceRule("3", 30));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(0, result.getTotalScore());
        assertEquals(0, result.getPass());
    }

    @Test
    @DisplayName("EXPERIENCE：候选人为空经验字段，默认0年")
    void evaluateExperience_nullYears_defaultZero() {
        candidate.setExperienceYears(null);
        List<ScreenRule> rules = Arrays.asList(experienceRule("1", 30));
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);
        assertEquals(0, result.getTotalScore());
    }

    // ==================== 综合评分 ====================

    @Test
    @DisplayName("综合：多规则组合，得分率50%及以上视为通过")
    void screen_compositeRules_passThreshold() {
        // 总分40+30+30=100, 候选人得40+0+30=70, 70*2>=100 → 通过
        List<ScreenRule> rules = Arrays.asList(
                skillRule("ANY", "Java,MySQL", 40),
                keywordRule("ANY", "金融,区块链", 30),
                experienceRule("3", 30)
        );
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(70, result.getTotalScore());
        assertEquals(100, result.getMaxScore());
        assertEquals(1, result.getPass());
    }

    @Test
    @DisplayName("综合：得分率低于50%不通过")
    void screen_compositeRules_belowThreshold() {
        // 权重60+40=100, 得0+0=0 → 不通过
        List<ScreenRule> rules = Arrays.asList(
                skillRule("ALL", "Dubbo,RabbitMQ", 60),
                keywordRule("ANY", "人工智能,算法", 40)
        );
        when(applicationMapper.selectById(100L)).thenReturn(application);
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screen(100L);

        assertEquals(0, result.getTotalScore());
        assertEquals(0, result.getPass());
    }

    // ==================== 直接筛选 screenCandidateDirect ====================

    @Test
    @DisplayName("直接筛选：不依赖投递记录，不写入结果表")
    void screenCandidateDirect_noPersist() {
        List<ScreenRule> rules = Arrays.asList(skillRule("ANY", "Java", 50));
        when(candidateMapper.selectById(1L)).thenReturn(candidate);
        when(positionMapper.selectById(10L)).thenReturn(position);
        when(screenRuleService.listByPosition(10L)).thenReturn(rules);

        ScreenResult result = screeningEngineService.screenCandidateDirect(1L, 10L);

        assertEquals(50, result.getTotalScore());
        assertEquals(1, result.getPass());
        verify(screenResultMapper, never()).insert(any());
    }

    @Test
    @DisplayName("直接筛选：候选人不存在抛404")
    void screenCandidateDirect_candidateNotFound_throws404() {
        when(candidateMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> screeningEngineService.screenCandidateDirect(99L, 10L));
        assertEquals(404, ex.getCode());
    }
}
