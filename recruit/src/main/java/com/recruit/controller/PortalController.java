package com.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.entity.Position;
import com.recruit.mapper.PositionMapper;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Hidden
@Controller
@RequiredArgsConstructor
public class PortalController {

    private final PositionMapper positionMapper;

    @GetMapping({"/portal", "/portal/index"})
    public String index(Model model) {
        List<Position> positions = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .eq(Position::getStatus, "OPEN")
                        .orderByDesc(Position::getCreatedAt)
        );
        model.addAttribute("positions", positions);
        return "portal/index";
    }

    @GetMapping("/portal/position/{id}")
    public String positionDetail(@PathVariable Long id, Model model) {
        Position position = positionMapper.selectById(id);
        if (position == null || !"OPEN".equals(position.getStatus())) {
            return "redirect:/portal";
        }
        model.addAttribute("position", position);
        return "portal/position";
    }
}
