package io.rashed.finance.api;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end authentication flow against the real security filter
 * chain and database:
 *
 * register → me → refresh (rotation) → logout → refresh rejected.
 *
 * Requires the local PostgreSQL from infra/compose.yaml, like all
 * {@code @SpringBootTest} tests in this project.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowIntegrationTest {

    private static final String PASSWORD = "integration-test-password";

    private static final String EMAIL =
            "it-" + UUID.randomUUID() + "@example.com";

    @Autowired
    private MockMvc mockMvc;

    private static String accessToken;
    private static Cookie refreshCookie;

    @Test
    @Order(1)
    void register_returnsAccessTokenAndRefreshCookie() throws Exception {

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "name": "Integration Test"
                                }
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(EMAIL))
                .andExpect(jsonPath("$.user.role").value("OWNER"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andReturn();

        accessToken = extractAccessToken(result);
        refreshCookie = result.getResponse().getCookie("refresh_token");

        assertNotNull(refreshCookie);
        assertTrue(refreshCookie.getMaxAge() > 0);
    }

    @Test
    @Order(2)
    void me_returnsCurrentUserWithValidToken() throws Exception {

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.name").value("Integration Test"));
    }

    @Test
    @Order(3)
    void protectedEndpoint_rejectsMissingAndInvalidTokens() throws Exception {

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void login_rejectsWrongPassword() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "wrong-password"
                                }
                                """.formatted(EMAIL)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    void refresh_rotatesTheRefreshToken() throws Exception {

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie rotated = result.getResponse().getCookie("refresh_token");

        assertNotNull(rotated);
        assertNotEquals(refreshCookie.getValue(), rotated.getValue());

        // The presented token was revoked by rotation; reusing it must fail.
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized());

        refreshCookie = rotated;
    }

    @Test
    @Order(6)
    void logout_revokesRefreshTokenAndClearsCookie() throws Exception {

        // Token reuse in order(5) revoked every session; log in again
        // to get a fresh pair for the logout scenario.
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie fresh = login.getResponse().getCookie("refresh_token");
        assertNotNull(fresh);

        MvcResult logout = mockMvc.perform(post("/api/auth/logout")
                        .cookie(fresh))
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie cleared = logout.getResponse().getCookie("refresh_token");
        assertNotNull(cleared);
        assertEquals(0, cleared.getMaxAge());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(fresh))
                .andExpect(status().isUnauthorized());
    }

    private static String extractAccessToken(MvcResult result) throws Exception {

        String body = result.getResponse().getContentAsString();

        int start = body.indexOf("\"accessToken\":\"") + "\"accessToken\":\"".length();
        int end = body.indexOf('"', start);

        return body.substring(start, end);
    }
}
