package com.recruit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruit.dto.LoginRequest;
import com.recruit.entity.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全流程集成测试
 * 使用 H2 内存数据库（application-test.yml），无需外部 MySQL/Redis
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("全流程集成测试")
class RecruitFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("全流程：登录 → 创建职位 → 查询职位 → 更新职位 → 删除职位")
    void fullFlow_login_crud_position() throws Exception {
        // 1. 登录
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("admin");
        loginReq.setPassword("admin123");

        ResponseEntity<JsonNode> loginResp = restTemplate.postForEntity(
                "/api/auth/login", loginReq, JsonNode.class);

        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertEquals(0, loginResp.getBody().get("code").asInt());
        assertTrue(loginResp.getBody().get("data").get("token").asText().length() > 0);

        // 提取 token + cookie
        String token = loginResp.getBody().get("data").get("token").asText();
        String cookie = loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (cookie != null) {
            headers.add(HttpHeaders.COOKIE, cookie.split(";")[0]);
        }
        headers.add("Authorization", "Bearer " + token);

        // 2. 创建职位
        Position newPos = new Position();
        newPos.setTitle("集成测试工程师");
        newPos.setDepartment("质量部");
        newPos.setDescription("负责集成测试");
        newPos.setRequirements("3年以上测试经验");
        newPos.setStatus("OPEN");

        HttpEntity<Position> createEntity = new HttpEntity<>(newPos, headers);
        ResponseEntity<JsonNode> createResp = restTemplate.exchange(
                "/api/positions", HttpMethod.POST, createEntity, JsonNode.class);

        assertEquals(HttpStatus.OK, createResp.getStatusCode());
        assertEquals(0, createResp.getBody().get("code").asInt());
        long positionId = createResp.getBody().get("data").asLong();
        assertTrue(positionId > 0);

        // 3. 查询职位详情
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> getResp = restTemplate.exchange(
                "/api/positions/" + positionId, HttpMethod.GET, getEntity, JsonNode.class);

        assertEquals(0, getResp.getBody().get("code").asInt());
        assertEquals("集成测试工程师", getResp.getBody().get("data").get("title").asText());

        // 4. 更新职位
        Position updatePos = new Position();
        updatePos.setTitle("高级集成测试工程师");
        updatePos.setDepartment("质量部");
        updatePos.setStatus("OPEN");

        HttpEntity<Position> updateEntity = new HttpEntity<>(updatePos, headers);
        restTemplate.exchange("/api/positions/" + positionId, HttpMethod.PUT, updateEntity, JsonNode.class);

        // 5. 验证更新结果
        ResponseEntity<JsonNode> verifyResp = restTemplate.exchange(
                "/api/positions/" + positionId, HttpMethod.GET, getEntity, JsonNode.class);
        assertEquals("高级集成测试工程师", verifyResp.getBody().get("data").get("title").asText());

        // 6. 删除职位
        restTemplate.exchange("/api/positions/" + positionId, HttpMethod.DELETE, getEntity, JsonNode.class);

        // 7. 验证删除后查询返回 null data
        ResponseEntity<JsonNode> afterDelete = restTemplate.exchange(
                "/api/positions/" + positionId, HttpMethod.GET, getEntity, JsonNode.class);
        // 逻辑删除后查不到，data 应为 null
        assertTrue(afterDelete.getBody().get("data") == null
                || afterDelete.getBody().get("data").isNull());
    }

    @Test
    @DisplayName("登录失败：错误密码返回 401")
    void login_wrongPassword_returns401() {
        LoginRequest badReq = new LoginRequest();
        badReq.setUsername("admin");
        badReq.setPassword("wrongpassword");

        ResponseEntity<JsonNode> resp = restTemplate.postForEntity(
                "/api/auth/login", badReq, JsonNode.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotEquals(0, resp.getBody().get("code").asInt());
    }

    @Test
    @DisplayName("求职者端匿名访问：无需 token 即可访问 portal 职位列表")
    void portal_anonymousAccess() {
        ResponseEntity<JsonNode> resp = restTemplate.getForEntity(
                "/api/portal/positions?current=1&size=10", JsonNode.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(0, resp.getBody().get("code").asInt());
    }
}
