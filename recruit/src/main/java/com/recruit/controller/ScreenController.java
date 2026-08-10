package com.recruit.controller;

import com.recruit.common.R;
import com.recruit.entity.ScreenResult;
import com.recruit.service.ScreeningEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 筛选引擎 API
 */
@RestController
@RequestMapping("/api/screen")
@RequiredArgsConstructor
@Tag(name = "筛选引擎", description = "对投递记录执行智能筛选并输出得分与通过结论")
public class ScreenController {

    private final ScreeningEngineService screeningEngineService;

    @PostMapping("/run")
    @Operation(summary = "按投递记录执行筛选（Query 参数）", description = "兼容前端旧写法：/api/screen/run?applicationId=123")
    public R<ScreenResult> runByQuery(@RequestParam Long applicationId) {
        return R.ok(screeningEngineService.screen(applicationId));
    }

    @PostMapping
    @Operation(summary = "按投递记录执行筛选（JSON Body）", description = "推荐写法：POST /api/screen body {\"applicationId\": 123}")
    public R<ScreenResult> runByBody(@RequestBody Map<String, Long> body) {
        Long applicationId = body.get("applicationId");
        if (applicationId == null) {
            applicationId = body.get("id");
        }
        if (applicationId == null) {
            throw new IllegalArgumentException("applicationId 必填");
        }
        return R.ok(screeningEngineService.screen(applicationId));
    }

    @PostMapping("/test")
    @Operation(summary = "直接测试候选人匹配（不生成投递记录）", description = "仅调用匹配规则得出分数，不写入 screen_result 表")
    public R<ScreenResult> test(@RequestParam Long candidateId,
                                @RequestParam Long positionId) {
        return R.ok(screeningEngineService.screenCandidateDirect(candidateId, positionId));
    }
}
