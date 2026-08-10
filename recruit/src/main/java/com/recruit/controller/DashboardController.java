package com.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.entity.Application;
import com.recruit.entity.Candidate;
import com.recruit.entity.Position;
import com.recruit.entity.ScreenResult;
import com.recruit.mapper.ApplicationMapper;
import com.recruit.mapper.CandidateMapper;
import com.recruit.mapper.PositionMapper;
import com.recruit.mapper.ScreenResultMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 主界面 Dashboard 控制器
 * 服务端渲染：返回首页、登录页等 Thymeleaf 模板
 */
@Hidden
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private final PositionMapper positionMapper;
    private final CandidateMapper candidateMapper;
    private final ApplicationMapper applicationMapper;
    private final ScreenResultMapper screenResultMapper;

    /**
     * 根路径：直接跳转到管理后台首页
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    /**
     * 管理后台主界面（Dashboard）
     * 展示：顶部导航、4 张统计卡片、职位列表、最近投递列表
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 统计卡片
        long positionCount = positionMapper.selectCount(null);
        long candidateCount = candidateMapper.selectCount(null);
        long applicationCount = applicationMapper.selectCount(null);
        long passedCount = screenResultMapper.selectCount(
                new LambdaQueryWrapper<ScreenResult>().eq(ScreenResult::getPass, 1)
        );
        long rejectedCount = screenResultMapper.selectCount(
                new LambdaQueryWrapper<ScreenResult>().eq(ScreenResult::getPass, 0)
        );

        // 职位列表（按创建时间倒序取前 6 条）
        List<Position> positions = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .orderByDesc(Position::getCreatedAt)
                        .last("LIMIT 6")
        );

        // 每个职位的投递数（用于饼图）
        List<Application> allApps = applicationMapper.selectList(null);
        java.util.Map<String, Long> positionAppCountMap = new java.util.LinkedHashMap<>();
        for (Application app : allApps) {
            Position p = positionMapper.selectById(app.getPositionId());
            String title = p != null ? p.getTitle() : ("职位#" + app.getPositionId());
            positionAppCountMap.merge(title, 1L, Long::sum);
        }
        try {
            model.addAttribute("chartPositionLabels",
                    MAPPER.writeValueAsString(positionAppCountMap.keySet()));
            model.addAttribute("chartPositionData",
                    MAPPER.writeValueAsString(positionAppCountMap.values()));
        } catch (Exception e) {
            model.addAttribute("chartPositionLabels", "[]");
            model.addAttribute("chartPositionData", "[]");
        }
        model.addAttribute("chartPassLabels", "[\"通过\",\"不通过\"]");
        model.addAttribute("chartPassData", "[" + passedCount + "," + rejectedCount + "]");

        // 最近投递：取候选人姓名、职位、状态
        List<Application> recentApps = applicationMapper.selectList(
                new LambdaQueryWrapper<Application>()
                        .orderByDesc(Application::getCreatedAt)
                        .last("LIMIT 5")
        );
        java.util.Map<Long, String> candidateNameMap = new java.util.HashMap<>();
        java.util.Map<Long, String> positionTitleMap = new java.util.HashMap<>();
        java.util.Map<Long, String> appStatusClassMap = new java.util.HashMap<>();
        java.util.Map<Long, String> appStatusTextMap = new java.util.HashMap<>();
        for (Application app : recentApps) {
            Candidate c = candidateMapper.selectById(app.getCandidateId());
            if (c != null) candidateNameMap.put(app.getId(), c.getName());
            Position p = positionMapper.selectById(app.getPositionId());
            if (p != null) positionTitleMap.put(app.getId(), p.getTitle());
            fillAppStatusMap(app.getStatus(), app.getId(), appStatusClassMap, appStatusTextMap);
        }

        // 最近筛选结果（按时间倒序取前 5 条）
        List<ScreenResult> recentResults = screenResultMapper.selectList(
                new LambdaQueryWrapper<ScreenResult>()
                        .orderByDesc(ScreenResult::getCreatedAt)
                        .last("LIMIT 5")
        );
        java.util.Map<Long, String> srCandidateNameMap = new java.util.HashMap<>();
        java.util.Map<Long, String> srPositionTitleMap = new java.util.HashMap<>();
        java.util.Map<Long, String> srPassClassMap = new java.util.HashMap<>();
        java.util.Map<Long, String> srPassTextMap = new java.util.HashMap<>();
        for (ScreenResult sr : recentResults) {
            Application a = applicationMapper.selectById(sr.getApplicationId());
            if (a != null) {
                Candidate c = candidateMapper.selectById(a.getCandidateId());
                if (c != null) srCandidateNameMap.put(sr.getId(), c.getName());
                Position p = positionMapper.selectById(a.getPositionId());
                if (p != null) srPositionTitleMap.put(sr.getId(), p.getTitle());
            }
            if (Integer.valueOf(1).equals(sr.getPass())) {
                srPassClassMap.put(sr.getId(), "tag-open");
                srPassTextMap.put(sr.getId(), "✅ 通过");
            } else {
                srPassClassMap.put(sr.getId(), "tag-closed");
                srPassTextMap.put(sr.getId(), "❌ 不通过");
            }
        }

        model.addAttribute("positionCount", positionCount);
        model.addAttribute("candidateCount", candidateCount);
        model.addAttribute("applicationCount", applicationCount);
        model.addAttribute("passedCount", passedCount);
        model.addAttribute("positions", positions);
        model.addAttribute("recentApps", recentApps);
        model.addAttribute("candidateNameMap", candidateNameMap);
        model.addAttribute("positionTitleMap", positionTitleMap);
        model.addAttribute("appStatusClassMap", appStatusClassMap);
        model.addAttribute("appStatusTextMap", appStatusTextMap);
        // 最近筛选结果
        model.addAttribute("recentResults", recentResults);
        model.addAttribute("srCandidateNameMap", srCandidateNameMap);
        model.addAttribute("srPositionTitleMap", srPositionTitleMap);
        model.addAttribute("srPassClassMap", srPassClassMap);
        model.addAttribute("srPassTextMap", srPassTextMap);
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        return "dashboard";
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ==================== 子页面路由 ====================

    /** 职位管理页 */
    @GetMapping("/page/positions")
    public String positionsPage(Model model) {
        model.addAttribute("pageTitle", "职位管理");
        model.addAttribute("activeMenu", "positions");
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        return "positions";
    }

    /** 候选人库页 */
    @GetMapping("/page/candidates")
    public String candidatesPage(Model model) {
        model.addAttribute("pageTitle", "候选人库");
        model.addAttribute("activeMenu", "candidates");
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        return "candidates";
    }

    /** 投递记录页 */
    @GetMapping("/page/applications")
    public String applicationsPage(Model model) {
        model.addAttribute("pageTitle", "投递记录");
        model.addAttribute("activeMenu", "applications");
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        return "applications";
    }

    /** 筛选规则页 */
    @GetMapping("/page/rules")
    public String rulesPage(Model model) {
        model.addAttribute("pageTitle", "筛选规则");
        model.addAttribute("activeMenu", "rules");
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        return "rules";
    }

    /** 用户管理页 */
    @GetMapping("/page/users")
    public String usersPage(Model model) {
        model.addAttribute("pageTitle", "用户管理");
        model.addAttribute("activeMenu", "users");
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        return "users";
    }

    /** 职位详情页 */
    @GetMapping("/page/positions/{id}")
    public String positionDetailPage(@PathVariable Long id, Model model) {
        Position position = positionMapper.selectById(id);
        model.addAttribute("position", position);
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        model.addAttribute("activeMenu", "positions");
        model.addAttribute("pageTitle", "职位详情");

        List<Application> applications = applicationMapper.selectList(
                new LambdaQueryWrapper<Application>().eq(Application::getPositionId, id)
        );
        List<java.util.Map<String, Object>> appList = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm");
        for (Application app : applications) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", app.getId());
            map.put("status", app.getStatus());
            map.put("createdAt", app.getCreatedAt());
            map.put("createdTimeStr", app.getCreatedAt() != null ? app.getCreatedAt().format(fmt) : "-");
            Candidate c = candidateMapper.selectById(app.getCandidateId());
            map.put("candidateName", c != null ? c.getName() : "-");
            map.put("candidateId", app.getCandidateId());
            java.util.Map<Long, String> tempClassMap = new java.util.HashMap<>();
            java.util.Map<Long, String> tempTextMap = new java.util.HashMap<>();
            fillAppStatusMap(app.getStatus(), app.getId(), tempClassMap, tempTextMap);
            map.put("statusClass", tempClassMap.get(app.getId()));
            map.put("statusText", tempTextMap.get(app.getId()));
            ScreenResult sr = screenResultMapper.selectOne(
                    new LambdaQueryWrapper<ScreenResult>()
                            .eq(ScreenResult::getApplicationId, app.getId())
                            .orderByDesc(ScreenResult::getCreatedAt)
                            .last("LIMIT 1")
            );
            if (sr != null) {
                map.put("screenScore", sr.getTotalScore() + "/" + sr.getMaxScore());
                map.put("screenPass", sr.getPass());
                map.put("screenPassText", Integer.valueOf(1).equals(sr.getPass()) ? "✅通过" : "❌不通过");
            } else {
                map.put("screenScore", "-");
                map.put("screenPass", null);
                map.put("screenPassText", "未筛选");
            }
            appList.add(map);
        }
        model.addAttribute("applicationList", appList);

        return "position-detail";
    }

    /** 候选人详情页 */
    @GetMapping("/page/candidates/{id}")
    public String candidateDetailPage(@PathVariable Long id, Model model) {
        Candidate candidate = candidateMapper.selectById(id);
        model.addAttribute("candidate", candidate);
        model.addAttribute("today", java.time.LocalDate.now().toString());
        model.addAttribute("user", "系统管理员");
        model.addAttribute("activeMenu", "candidates");
        model.addAttribute("pageTitle", "候选人详情");

        List<Application> applications = applicationMapper.selectList(
                new LambdaQueryWrapper<Application>().eq(Application::getCandidateId, id)
        );
        List<java.util.Map<String, Object>> appList = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm");
        for (Application app : applications) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", app.getId());
            map.put("status", app.getStatus());
            map.put("createdAt", app.getCreatedAt());
            map.put("createdTimeStr", app.getCreatedAt() != null ? app.getCreatedAt().format(fmt) : "-");
            map.put("positionId", app.getPositionId());
            Position p = positionMapper.selectById(app.getPositionId());
            map.put("positionName", p != null ? p.getTitle() : "-");
            java.util.Map<Long, String> tempClassMap = new java.util.HashMap<>();
            java.util.Map<Long, String> tempTextMap = new java.util.HashMap<>();
            fillAppStatusMap(app.getStatus(), app.getId(), tempClassMap, tempTextMap);
            map.put("statusClass", tempClassMap.get(app.getId()));
            map.put("statusText", tempTextMap.get(app.getId()));
            ScreenResult sr = screenResultMapper.selectOne(
                    new LambdaQueryWrapper<ScreenResult>()
                            .eq(ScreenResult::getApplicationId, app.getId())
                            .orderByDesc(ScreenResult::getCreatedAt)
                            .last("LIMIT 1")
            );
            if (sr != null) {
                String passTag = Integer.valueOf(1).equals(sr.getPass()) ? " ✅通过" : " ❌不通过";
                map.put("screenResultText", sr.getTotalScore() + "/" + sr.getMaxScore() + passTag);
            } else {
                map.put("screenResultText", "未筛选");
            }
            appList.add(map);
        }
        model.addAttribute("applicationList", appList);

        return "candidate-detail";
    }

    // ==================== 辅助方法 ====================

    private static void fillAppStatusMap(String status, Long appId,
                                         java.util.Map<Long, String> classMap,
                                         java.util.Map<Long, String> textMap) {
        if ("PENDING".equals(status)) {
            classMap.put(appId, "tag-pending");
            textMap.put(appId, "⏳ 待审核");
        } else if ("REVIEWED".equals(status)) {
            classMap.put(appId, "tag-reviewed");
            textMap.put(appId, "👁️ 已查看");
        } else if ("ACCEPTED".equals(status)) {
            classMap.put(appId, "tag-open");
            textMap.put(appId, "✅ 通过");
        } else {
            classMap.put(appId, "tag-closed");
            textMap.put(appId, "❌ 拒绝");
        }
    }
}
