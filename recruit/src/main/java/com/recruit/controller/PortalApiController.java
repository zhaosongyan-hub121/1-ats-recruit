package com.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.BusinessException;
import com.recruit.common.R;
import com.recruit.dto.PortalApplyRequest;
import com.recruit.entity.*;
import com.recruit.mapper.*;
import com.recruit.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
@Tag(name = "求职者端", description = "求职者门户接口（职位浏览匿名可访问，投递/收藏/个人中心需登录）")
public class PortalApiController {

    private final PositionMapper positionMapper;
    private final CandidateMapper candidateMapper;
    private final ApplicationMapper applicationMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;

    @GetMapping("/categories")
    @Operation(summary = "获取职位分类列表")
    public R<List<Map<String, String>>> categories() {
        List<Map<String, String>> list = new ArrayList<>();
        String[][] cats = {{"", "全部"}, {"SOCIAL", "社招"}, {"CAMPUS", "校招"}, {"INTERN", "实习"}};
        for (String[] c : cats) {
            Map<String, String> m = new HashMap<>();
            m.put("value", c[0]); m.put("label", c[1]);
            list.add(m);
        }
        return R.ok(list);
    }

    @GetMapping("/companies")
    @Operation(summary = "获取所有有招聘岗位的公司列表（去重）")
    public R<List<String>> companies() {
        List<String> list = positionMapper.selectList(
                        new LambdaQueryWrapper<Position>()
                                .select(Position::getCompanyName)
                                .eq(Position::getStatus, "OPEN")
                                .isNotNull(Position::getCompanyName)
                                .groupBy(Position::getCompanyName)
                                .orderByAsc(Position::getCompanyName))
                .stream()
                .map(Position::getCompanyName)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        return R.ok(list);
    }

    @GetMapping("/positions")
    @Operation(summary = "分页获取招聘中职位（支持条件筛选）")
    public R<Map<String, Object>> listPositions(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String department) {

        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, "OPEN");

        if (StringUtils.hasText(category)) wrapper.eq(Position::getCategory, category);
        if (StringUtils.hasText(location)) wrapper.eq(Position::getLocation, location);
        if (StringUtils.hasText(company)) wrapper.like(Position::getCompanyName, company);
        if (StringUtils.hasText(department)) wrapper.like(Position::getDepartment, department);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Position::getTitle, keyword)
                    .or().like(Position::getDescription, keyword)
                    .or().like(Position::getRequirements, keyword)
                    .or().like(Position::getCompanyName, keyword));
        }
        wrapper.orderByDesc(Position::getCreatedAt);

        Page<Position> page = positionMapper.selectPage(new Page<>(current, size), wrapper);

        // 如果用户已登录，附加收藏状态
        List<Long> favIds = getCurrentUserFavoriteIds();

        List<Map<String, Object>> records = page.getRecords().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("companyName", p.getCompanyName());
            m.put("companyLogo", p.getCompanyLogo());
            m.put("department", p.getDepartment());
            m.put("category", p.getCategory());
            m.put("location", p.getLocation());
            m.put("salary", p.getSalary());
            m.put("education", p.getEducation());
            m.put("experience", p.getExperience());
            m.put("createdAt", p.getCreatedAt());
            m.put("requirements", p.getRequirements());
            m.put("description", p.getDescription());
            m.put("favorited", favIds != null && favIds.contains(p.getId()));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        data.put("pages", page.getPages());
        return R.ok(data);
    }

    @GetMapping("/positions/{id}")
    @Operation(summary = "获取职位详情")
    public R<Map<String, Object>> positionDetail(@PathVariable Long id) {
        Position p = positionMapper.selectById(id);
        if (p == null || !"OPEN".equals(p.getStatus())) {
            throw new BusinessException(404, "职位不存在或已关闭");
        }
        List<Long> favIds = getCurrentUserFavoriteIds();
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("companyName", p.getCompanyName());
        m.put("companyLogo", p.getCompanyLogo());
        m.put("department", p.getDepartment());
        m.put("description", p.getDescription());
        m.put("requirements", p.getRequirements());
        m.put("category", p.getCategory());
        m.put("location", p.getLocation());
        m.put("salary", p.getSalary());
        m.put("education", p.getEducation());
        m.put("experience", p.getExperience());
        m.put("createdAt", p.getCreatedAt());
        m.put("favorited", favIds != null && favIds.contains(p.getId()));
        return R.ok(m);
    }

    @PostMapping("/applications")
    @Operation(summary = "投递职位（需登录）")
    public R<Map<String, Object>> apply(@RequestBody @Valid PortalApplyRequest req) {
        UserContext.LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录后再投递");
        }
        Position position = positionMapper.selectById(req.getPositionId());
        if (position == null || !"OPEN".equals(position.getStatus())) {
            throw new BusinessException(404, "职位不存在或已关闭");
        }

        Long userId = user.getUserId();
        Long count = applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getUserId, userId)
                        .eq(Application::getPositionId, req.getPositionId()));
        if (count > 0) {
            throw new BusinessException(400, "您已投递过该职位，请勿重复投递");
        }

        // 查找或创建 candidate 记录
        Candidate candidate = candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>().eq(Candidate::getUserId, userId));
        if (candidate == null) {
            candidate = new Candidate();
            candidate.setUserId(userId);
            candidate.setName(req.getName());
            candidate.setEmail(req.getEmail());
            candidate.setPhone(req.getPhone());
            candidate.setSkills(req.getSkills());
            candidate.setExperienceYears(req.getExperienceYears());
            candidate.setResumeText(req.getResumeText());
            candidateMapper.insert(candidate);
        } else {
            candidate.setName(req.getName());
            candidate.setEmail(req.getEmail());
            candidate.setPhone(req.getPhone());
            if (StringUtils.hasText(req.getSkills())) candidate.setSkills(req.getSkills());
            if (req.getExperienceYears() != null) candidate.setExperienceYears(req.getExperienceYears());
            if (StringUtils.hasText(req.getResumeText())) candidate.setResumeText(req.getResumeText());
            candidateMapper.updateById(candidate);
        }

        Application application = new Application();
        application.setCandidateId(candidate.getId());
        application.setUserId(userId);
        application.setPositionId(req.getPositionId());
        application.setStatus("SUBMITTED");
        application.setCurrentRound(1);
        application.setCoverLetter(req.getCoverLetter());
        application.setStatusLog("[{\"time\":\"" + java.time.LocalDateTime.now() + "\",\"status\":\"SUBMITTED\",\"note\":\"简历投递成功，等待初筛\"}]");
        applicationMapper.insert(application);

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", application.getId());
        result.put("status", "SUBMITTED");
        return R.ok(result);
    }

    // ============ 收藏相关 ============

    @PostMapping("/favorites/{positionId}")
    @Operation(summary = "收藏/取消收藏职位（需登录，切换状态）")
    public R<Map<String, Object>> toggleFavorite(@PathVariable Long positionId) {
        UserContext.LoginUser user = requireLogin();
        Position p = positionMapper.selectById(positionId);
        if (p == null) throw new BusinessException(404, "职位不存在");

        Favorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getUserId())
                        .eq(Favorite::getPositionId, positionId));
        boolean favorited;
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            favorited = false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(user.getUserId());
            fav.setPositionId(positionId);
            favoriteMapper.insert(fav);
            favorited = true;
        }
        Map<String, Object> res = new HashMap<>();
        res.put("favorited", favorited);
        return R.ok(res);
    }

    @GetMapping("/me/favorites")
    @Operation(summary = "我的收藏职位列表（需登录）")
    public R<Map<String, Object>> myFavorites(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        UserContext.LoginUser user = requireLogin();
        Page<Favorite> favPage = favoriteMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getUserId())
                        .orderByDesc(Favorite::getCreatedAt));
        List<Long> positionIds = favPage.getRecords().stream()
                .map(Favorite::getPositionId).collect(Collectors.toList());
        List<Position> positions = positionIds.isEmpty() ? Collections.emptyList()
                : positionMapper.selectBatchIds(positionIds);
        Map<Long, Position> posMap = positions.stream()
                .collect(Collectors.toMap(Position::getId, p -> p));
        List<Map<String, Object>> records = favPage.getRecords().stream().map(f -> {
            Position p = posMap.get(f.getPositionId());
            Map<String, Object> m = new HashMap<>();
            m.put("favoriteId", f.getId());
            m.put("favoritedAt", f.getCreatedAt());
            if (p != null) {
                m.put("id", p.getId());
                m.put("title", p.getTitle());
                m.put("companyName", p.getCompanyName());
                m.put("companyLogo", p.getCompanyLogo());
                m.put("location", p.getLocation());
                m.put("salary", p.getSalary());
                m.put("status", p.getStatus());
                m.put("category", p.getCategory());
            }
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", favPage.getTotal());
        data.put("current", favPage.getCurrent());
        data.put("size", favPage.getSize());
        data.put("pages", favPage.getPages());
        return R.ok(data);
    }

    // ============ 个人中心 ============

    @GetMapping("/me/applications")
    @Operation(summary = "我的投递记录（需登录）")
    public R<Map<String, Object>> myApplications(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String status) {
        UserContext.LoginUser user = requireLogin();
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getUserId, user.getUserId());
        if (StringUtils.hasText(status)) wrapper.eq(Application::getStatus, status);
        wrapper.orderByDesc(Application::getCreatedAt);

        Page<Application> page = applicationMapper.selectPage(new Page<>(current, size), wrapper);
        List<Long> positionIds = page.getRecords().stream()
                .map(Application::getPositionId).distinct().collect(Collectors.toList());
        Map<Long, Position> posMap = new HashMap<>();
        if (!positionIds.isEmpty()) {
            positionMapper.selectBatchIds(positionIds)
                    .forEach(p -> posMap.put(p.getId(), p));
        }
        List<Map<String, Object>> records = page.getRecords().stream().map(a -> {
            Position p = posMap.get(a.getPositionId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("positionId", a.getPositionId());
            m.put("status", a.getStatus());
            m.put("statusText", statusText(a.getStatus()));
            m.put("currentRound", a.getCurrentRound());
            m.put("coverLetter", a.getCoverLetter());
            m.put("hrRemark", a.getHrRemark());
            m.put("createdAt", a.getCreatedAt());
            m.put("updatedAt", a.getUpdatedAt());
            if (p != null) {
                m.put("title", p.getTitle());
                m.put("companyName", p.getCompanyName());
                m.put("companyLogo", p.getCompanyLogo());
                m.put("location", p.getLocation());
                m.put("salary", p.getSalary());
            }
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        data.put("pages", page.getPages());
        return R.ok(data);
    }

    @GetMapping("/me/profile")
    @Operation(summary = "获取我的简历信息（需登录）")
    public R<Map<String, Object>> myProfile() {
        UserContext.LoginUser user = requireLogin();
        User u = userMapper.selectById(user.getUserId());
        Candidate c = candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>().eq(Candidate::getUserId, user.getUserId()));
        Map<String, Object> data = new HashMap<>();
        data.put("userId", u.getId());
        data.put("username", u.getUsername());
        data.put("realName", u.getRealName());
        data.put("email", u.getEmail());
        data.put("phone", u.getPhone());
        data.put("role", u.getRole());
        if (c != null) {
            data.put("skills", c.getSkills());
            data.put("experienceYears", c.getExperienceYears());
            data.put("resumeText", c.getResumeText());
            data.put("educationLevel", c.getEducationLevel());
            data.put("school", c.getSchool());
        }
        return R.ok(data);
    }

    @PutMapping("/me/profile")
    @Operation(summary = "更新我的个人信息/简历（需登录）")
    public R<Void> updateProfile(@RequestBody Map<String, Object> body) {
        UserContext.LoginUser user = requireLogin();
        User u = userMapper.selectById(user.getUserId());
        if (u == null) throw new BusinessException(404, "用户不存在");
        if (body.get("realName") != null) u.setRealName((String) body.get("realName"));
        if (body.get("email") != null) u.setEmail((String) body.get("email"));
        if (body.get("phone") != null) u.setPhone((String) body.get("phone"));
        userMapper.updateById(u);

        Candidate c = candidateMapper.selectOne(
                new LambdaQueryWrapper<Candidate>().eq(Candidate::getUserId, user.getUserId()));
        if (c == null) {
            c = new Candidate();
            c.setUserId(user.getUserId());
            c.setName(u.getRealName());
            c.setEmail(u.getEmail());
            c.setPhone(u.getPhone());
        }
        if (body.get("name") != null) c.setName((String) body.get("name"));
        if (c.getName() == null && u.getRealName() != null) c.setName(u.getRealName());
        if (body.get("email") != null) c.setEmail((String) body.get("email"));
        if (body.get("phone") != null) c.setPhone((String) body.get("phone"));
        if (body.get("skills") != null) c.setSkills((String) body.get("skills"));
        if (body.get("experienceYears") != null) c.setExperienceYears(Integer.valueOf(body.get("experienceYears").toString()));
        if (body.get("resumeText") != null) c.setResumeText((String) body.get("resumeText"));
        if (body.get("educationLevel") != null) c.setEducationLevel((String) body.get("educationLevel"));
        if (body.get("school") != null) c.setSchool((String) body.get("school"));
        if (c.getId() == null) candidateMapper.insert(c); else candidateMapper.updateById(c);
        return R.ok();
    }

    @GetMapping("/stats")
    @Operation(summary = "首页统计数据（匿名可访问）")
    public R<Map<String, Object>> stats() {
        LambdaQueryWrapper<Position> openWrapper = new LambdaQueryWrapper<Position>().eq(Position::getStatus, "OPEN");
        long total = positionMapper.selectCount(openWrapper);
        long social = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, "OPEN").eq(Position::getCategory, "SOCIAL"));
        long campus = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, "OPEN").eq(Position::getCategory, "CAMPUS"));
        long intern = positionMapper.selectCount(new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, "OPEN").eq(Position::getCategory, "INTERN"));
        List<Object> companyObjs = positionMapper.selectObjs(new LambdaQueryWrapper<Position>()
                .select(Position::getCompanyName)
                .eq(Position::getStatus, "OPEN")
                .isNotNull(Position::getCompanyName)
                .groupBy(Position::getCompanyName));
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("social", social);
        data.put("campus", campus);
        data.put("intern", intern);
        data.put("companies", companyObjs == null ? 0 : companyObjs.size());
        return R.ok(data);
    }

    // ============ 工具方法 ============

    private UserContext.LoginUser requireLogin() {
        UserContext.LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    private List<Long> getCurrentUserFavoriteIds() {
        UserContext.LoginUser user = UserContext.get();
        if (user == null) return null;
        return favoriteMapper.selectList(
                        new LambdaQueryWrapper<Favorite>()
                                .eq(Favorite::getUserId, user.getUserId())
                                .select(Favorite::getPositionId))
                .stream().map(Favorite::getPositionId).collect(Collectors.toList());
    }

    public static String statusText(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "SUBMITTED": return "初筛中";
            case "SCREENING_PASS": return "初筛通过";
            case "INTERVIEWING": return "面试中";
            case "INTERVIEWED": return "面试完成";
            case "OFFER": return "已发Offer";
            case "ACCEPTED": return "已录用";
            case "REJECTED": return "已结束";
            default: return status;
        }
    }
}
