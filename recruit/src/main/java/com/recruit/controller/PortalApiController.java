package com.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.R;
import com.recruit.dto.PortalApplyRequest;
import com.recruit.entity.Application;
import com.recruit.entity.Candidate;
import com.recruit.entity.Position;
import com.recruit.mapper.ApplicationMapper;
import com.recruit.mapper.CandidateMapper;
import com.recruit.mapper.PositionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
@Tag(name = "求职者端", description = "匿名可访问的求职者门户接口")
public class PortalApiController {

    private final PositionMapper positionMapper;
    private final CandidateMapper candidateMapper;
    private final ApplicationMapper applicationMapper;

    @GetMapping("/positions")
    @Operation(summary = "获取招聘中职位列表")
    public R<List<Position>> listOpenPositions() {
        List<Position> positions = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .eq(Position::getStatus, "OPEN")
                        .orderByDesc(Position::getCreatedAt)
        );
        return R.ok(positions);
    }

    @PostMapping("/applications")
    @Operation(summary = "匿名投递职位")
    public R<Long> apply(@Valid @RequestBody PortalApplyRequest req) {
        Position position = positionMapper.selectById(req.getPositionId());
        if (position == null || !"OPEN".equals(position.getStatus())) {
            return R.fail(400, "职位不存在或已关闭");
        }

        Candidate candidate;
        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            candidate = candidateMapper.selectOne(
                    new LambdaQueryWrapper<Candidate>()
                            .eq(Candidate::getEmail, req.getEmail().trim())
                            .last("LIMIT 1")
            );
        } else {
            candidate = null;
        }

        if (candidate == null) {
            candidate = new Candidate();
            candidate.setName(req.getName());
            candidate.setEmail(req.getEmail());
            candidate.setPhone(req.getPhone());
            candidate.setSkills(req.getSkills());
            candidate.setExperienceYears(req.getExperienceYears());
            candidate.setResumeText(req.getResumeText());
            candidateMapper.insert(candidate);
        } else {
            boolean needUpdate = false;
            if (req.getName() != null && !req.getName().equals(candidate.getName())) {
                candidate.setName(req.getName());
                needUpdate = true;
            }
            if (req.getPhone() != null && !req.getPhone().equals(candidate.getPhone())) {
                candidate.setPhone(req.getPhone());
                needUpdate = true;
            }
            if (req.getSkills() != null && !req.getSkills().equals(candidate.getSkills())) {
                candidate.setSkills(req.getSkills());
                needUpdate = true;
            }
            if (req.getExperienceYears() != null && !req.getExperienceYears().equals(candidate.getExperienceYears())) {
                candidate.setExperienceYears(req.getExperienceYears());
                needUpdate = true;
            }
            if (req.getResumeText() != null && !req.getResumeText().equals(candidate.getResumeText())) {
                candidate.setResumeText(req.getResumeText());
                needUpdate = true;
            }
            if (needUpdate) {
                candidateMapper.updateById(candidate);
            }
        }

        Long existingCount = applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getCandidateId, candidate.getId())
                        .eq(Application::getPositionId, req.getPositionId())
        );
        if (existingCount != null && existingCount > 0) {
            return R.fail(400, "该职位您已投递");
        }

        Application application = new Application();
        application.setCandidateId(candidate.getId());
        application.setPositionId(req.getPositionId());
        application.setStatus("PENDING");
        application.setCoverLetter(req.getCoverLetter());
        applicationMapper.insert(application);

        return R.ok(application.getId());
    }
}
