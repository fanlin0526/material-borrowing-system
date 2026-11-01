package com.example.borrow;

import com.example.borrow.entity.User;
import com.example.borrow.repository.UserRepository;
import com.example.borrow.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 采购入库列表与详情只允许教师、超级管理员访问。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StockInAccessControlIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void studentCannotReadStockInListOrDetail() {
        HttpEntity<Void> request = authorizedRequest("zhangming");

        ResponseEntity<String> list = restTemplate.exchange(
                "/api/stock-in", HttpMethod.GET, request, String.class);
        ResponseEntity<String> detail = restTemplate.exchange(
                "/api/stock-in/1", HttpMethod.GET, request, String.class);

        assertEquals(HttpStatus.FORBIDDEN, list.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, detail.getStatusCode());
    }

    @Test
    void teacherCanReadStockInList() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/stock-in", HttpMethod.GET, authorizedRequest("teacher"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() != null && response.getBody().contains("\"code\":200"));
    }

    private HttpEntity<Void> authorizedRequest(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(JwtUtil.generate(user.getUsername(), user.getRole(), user.getRealName()));
        return new HttpEntity<>(headers);
    }
}
