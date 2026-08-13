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
 * 智能筛选引擎控制器
 * <p>
 * 对投递记录执行智能筛选，调用筛选规则引擎计算候选人匹配得分并给出通过结论。
 * 支持按投递记录筛选（写入结果）和直接对候选人-岗位进行匹配测试（不落库）。
 * </p>
 */
@RestController
@RequestMapping("/api/screen")
@RequiredArgsConstructor
@Tag(name = "筛选引擎", description = "对投递记录执行智能筛选并输出得分与通过结论")
public class ScreenController {

    private final ScreeningEngineService screeningEngineService;

    /**
     * 按 Query 参数对投递记录执行筛选
     * <p>兼容前端旧写法，结果写入 screen_result 表。</p>
     *
     * @param applicationId 投递记录 ID
     * @return 筛选结果，包含得分与通过结论
     */
    @PostMapping("/run")
    @Operation(summary = "按投递记录执行筛选（Query 参数）", description = "兼容前端旧写法：/api/screen/run?applicationId=123")
    public R<ScreenResult> runByQuery(@RequestParam Long applicationId) {
        return R.ok(screeningEngineService.screen(applicationId));
    }

    /**
     * 按 JSON Body 对投递记录执行筛选
     * <p>推荐写法，body 可使用 applicationId 或 id 字段，结果写入 screen_result 表。</p>
     *
     * @param body 请求体，需包含 applicationId（或 id）
     * @return 筛选结果，包含得分与通过结论
     */
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

    /**
     * 直接测试候选人匹配（不生成投递记录）
     * <p>仅调用匹配规则计算得分，不写入 screen_result 表，用于规则预演。</p>
     *
     * @param candidateId 候选人 ID
     * @param positionId  岗位 ID
     * @return 筛选结果，包含得分与通过结论
     */
    @PostMapping("/test")
    @Operation(summary = "直接测试候选人匹配（不生成投递记录）", description = "仅调用匹配规则得出分数，不写入 screen_result 表")
    public R<ScreenResult> test(@RequestParam Long candidateId,
                                @RequestParam Long positionId) {
        return R.ok(screeningEngineService.screenCandidateDirect(candidateId, positionId));
    }
}
