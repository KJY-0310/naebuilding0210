package com.example.naebuilding;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/sql/test-data.sql") // ✅ 테스트 시작 전 데이터 주입
class RequestSecurityIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void userCannotUpdateStatus_should403() throws Exception {
        mvc.perform(patch("/api/admin/requests/1/status")
                        .with(httpBasic("user1", "user1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUpdateStatus_should200() throws Exception {
        mvc.perform(patch("/api/admin/requests/1/status")
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk());
    }
}
