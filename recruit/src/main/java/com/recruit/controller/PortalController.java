package com.recruit.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 页面路由控制器
 * <p>
 * 负责求职者门户的 Thymeleaf 模板页面跳转，包括门户首页、登录页、注册页、
 * 职位详情页与个人中心页。该控制器仅做视图跳转，业务数据由 PortalApiController 提供。
 * </p>
 */
@Hidden
@Controller
public class PortalController {

    /**
     * 根路径重定向到门户首页
     *
     * @return 重定向到 /portal
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/portal";
    }

    /**
     * 门户首页
     *
     * @return Thymeleaf 模板名 "portal/index"
     */
    @GetMapping({"/portal", "/portal/index"})
    public String index() {
        return "portal/index";
    }

    /**
     * 登录页
     *
     * @return Thymeleaf 模板名 "login"
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页
     *
     * @return Thymeleaf 模板名 "register"
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 职位详情页
     *
     * @param id 职位 ID
     * @return Thymeleaf 模板名 "portal/position"
     */
    @GetMapping("/portal/position/{id}")
    public String positionDetail(@PathVariable Long id) {
        return "portal/position";
    }

    /**
     * 个人中心页
     *
     * @return Thymeleaf 模板名 "portal/profile"
     */
    @GetMapping("/portal/profile")
    public String profilePage() {
        return "portal/profile";
    }
}
