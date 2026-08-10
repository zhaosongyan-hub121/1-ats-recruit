package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.entity.Position;
import com.recruit.mapper.PositionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("职位服务单元测试")
class PositionServiceTest {

    @Mock
    private PositionMapper positionMapper;

    @InjectMocks
    private PositionService positionService;

    private Position buildPosition(Long id, String title, String dept, String status) {
        Position p = new Position();
        p.setId(id);
        p.setTitle(title);
        p.setDepartment(dept);
        p.setDescription("职位描述" + id);
        p.setRequirements("任职要求" + id);
        p.setStatus(status);
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    // ==================== 分页查询 ====================

    @Test
    @DisplayName("分页查询：无关键词时返回全部结果")
    void page_noKeyword_returnsAll() {
        Page<Position> expectedPage = new Page<>(1, 10);
        List<Position> records = Arrays.asList(
                buildPosition(1L, "Java工程师", "技术部", "OPEN"),
                buildPosition(2L, "产品经理", "产品部", "OPEN")
        );
        expectedPage.setRecords(records);
        expectedPage.setTotal(2);

        when(positionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(expectedPage);

        Page<Position> result = positionService.page(1, 10, null);

        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("Java工程师", result.getRecords().get(0).getTitle());
    }

    @Test
    @DisplayName("分页查询：有关键词时按标题/部门/描述模糊匹配")
    void page_withKeyword_appliesLike() {
        Page<Position> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(buildPosition(1L, "高级Java工程师", "技术部", "OPEN")));
        expectedPage.setTotal(1);

        when(positionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(expectedPage);

        Page<Position> result = positionService.page(1, 10, "Java");

        assertEquals(1, result.getTotal());
        verify(positionMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询：空字符串关键词等同于无关键词")
    void page_emptyKeyword_noFilter() {
        Page<Position> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(Arrays.asList(buildPosition(1L, "Java工程师", "技术部", "OPEN")));
        expectedPage.setTotal(1);

        when(positionMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(expectedPage);

        Page<Position> result1 = positionService.page(1, 10, "");
        Page<Position> result2 = positionService.page(1, 10, "   ");

        assertEquals(1, result1.getTotal());
        assertEquals(1, result2.getTotal());
    }

    // ==================== 单条查询 ====================

    @Test
    @DisplayName("按ID查询：存在时返回职位")
    void get_existId_returnsPosition() {
        Position pos = buildPosition(5L, "前端工程师", "技术部", "OPEN");
        when(positionMapper.selectById(5L)).thenReturn(pos);

        Position result = positionService.get(5L);

        assertNotNull(result);
        assertEquals("前端工程师", result.getTitle());
        assertEquals("技术部", result.getDepartment());
    }

    @Test
    @DisplayName("按ID查询：不存在时返回null")
    void get_notExistId_returnsNull() {
        when(positionMapper.selectById(999L)).thenReturn(null);
        assertNull(positionService.get(999L));
    }

    // ==================== 新增 ====================

    @Test
    @DisplayName("新增：未指定status时默认设为OPEN")
    void create_defaultStatus_open() {
        Position input = new Position();
        input.setTitle("测试职位");
        input.setDepartment("测试部");
        // 不设置 status

        doAnswer(invocation -> {
            Position p = invocation.getArgument(0);
            p.setId(99L);
            return 1;
        }).when(positionMapper).insert(any(Position.class));

        Long id = positionService.create(input);

        assertEquals(99L, id);
        assertEquals("OPEN", input.getStatus());
        verify(positionMapper, times(1)).insert(input);
    }

    @Test
    @DisplayName("新增：已指定status时保留原值")
    void create_withStatus_preserve() {
        Position input = new Position();
        input.setTitle("关闭的职位");
        input.setDepartment("封存部");
        input.setStatus("CLOSED");

        doAnswer(invocation -> {
            Position p = invocation.getArgument(0);
            p.setId(200L);
            return 1;
        }).when(positionMapper).insert(any(Position.class));

        Long id = positionService.create(input);

        assertEquals(200L, id);
        assertEquals("CLOSED", input.getStatus());
    }

    // ==================== 更新 / 删除 ====================

    @Test
    @DisplayName("更新：委托mapper.updateById")
    void update_delegatesMapper() {
        Position pos = buildPosition(1L, "新标题", "技术部", "OPEN");
        when(positionMapper.updateById(pos)).thenReturn(1);

        positionService.update(pos);

        verify(positionMapper, times(1)).updateById(pos);
    }

    @Test
    @DisplayName("删除：委托mapper.deleteById")
    void delete_delegatesMapper() {
        when(positionMapper.deleteById(42L)).thenReturn(1);
        positionService.delete(42L);
        verify(positionMapper, times(1)).deleteById(42L);
    }
}
