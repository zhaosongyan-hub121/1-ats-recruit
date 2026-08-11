package com.recruit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.common.GlobalExceptionHandler;
import com.recruit.entity.Position;
import com.recruit.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PositionController 控制器层测试（MockMvc standalone）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("职位接口控制器测试")
class PositionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PositionService positionService;

    @InjectMocks
    private PositionController positionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(positionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询职位详情：返回 200 + 数据")
    void getById_success() throws Exception {
        Position pos = new Position();
        pos.setId(1L);
        pos.setTitle("Java 后端工程师");
        pos.setDepartment("技术部");
        pos.setStatus("OPEN");

        when(positionService.get(1L)).thenReturn(pos);

        mockMvc.perform(get("/api/positions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("Java 后端工程师"));
    }

    @Test
    @DisplayName("新建职位：返回 200 + 新 ID")
    void create_success() throws Exception {
        Position pos = new Position();
        pos.setTitle("测试工程师");
        pos.setDepartment("质量部");

        when(positionService.create(any(Position.class))).thenReturn(10L);

        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("更新职位：返回 200")
    void update_success() throws Exception {
        Position pos = new Position();
        pos.setTitle("高级 Java 工程师");

        doNothing().when(positionService).update(any(Position.class));

        mockMvc.perform(put("/api/positions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("删除职位：返回 200")
    void delete_success() throws Exception {
        doNothing().when(positionService).delete(anyLong());

        mockMvc.perform(delete("/api/positions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(positionService, times(1)).delete(1L);
    }
}
