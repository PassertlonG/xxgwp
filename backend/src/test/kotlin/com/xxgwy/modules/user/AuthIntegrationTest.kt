package com.xxgwy.modules.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.xxgwy.XxgwyApplication
import com.xxgwy.common.security.JwtUtil
import com.xxgwy.infrastructure.redis.TokenService
import com.xxgwy.modules.user.dto.LoginRequest
import com.xxgwy.modules.user.dto.LoginResponse
import com.xxgwy.modules.user.dto.RefreshTokenRequest
import com.xxgwy.modules.user.dto.RegisterRequest
import com.xxgwy.modules.user.entity.User
import com.xxgwy.modules.user.repository.UserRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.junit.jupiter.api.Assertions.assertNotNull

@SpringBootTest(classes = [XxgwyApplication::class])
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var tokenService: TokenService

    companion object {
        private const val USERNAME = "test_integration"
        private const val PASSWORD = "testPass123"
        private var accessToken: String? = null
        private var refreshToken: String? = null
    }

    @AfterEach
    fun cleanup() {
        userRepository.findByUsername(USERNAME).ifPresent { userRepository.delete(it) }
    }

    @Test
    @Order(1)
    fun `health check should return 200`() {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.status").value("UP"))
    }

    @Test
    @Order(2)
    fun `register should return 200 with tokens`() {
        val request = RegisterRequest(USERNAME, PASSWORD, "IntegrationTest")

        val result = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)
            .andExpect(jsonPath("$.data.user.username").value(USERNAME))
            .andExpect(jsonPath("$.data.user.nickname").value("IntegrationTest"))
            .andExpect(jsonPath("$.data.user.role").value("CUSTOMER"))
            .andReturn()

        val json = objectMapper.readTree(result.response.contentAsString)
        val dataNode = json.get("data")
        val response = objectMapper.treeToValue(dataNode, LoginResponse::class.java)
        accessToken = response.accessToken
        refreshToken = response.refreshToken
    }

    @Test
    @Order(3)
    fun `register duplicate should return 409`() {
        // First create user
        userRepository.save(User().apply {
            username = USERNAME
            password = passwordEncoder.encode(PASSWORD)
            nickname = "Existing"
        })

        val request = RegisterRequest(USERNAME, PASSWORD, "Duplicate")

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("用户名已存在"))
    }

    @Test
    @Order(4)
    fun `login should return 200 with tokens`() {
        // Ensure user exists
        userRepository.save(User().apply {
            username = USERNAME
            password = passwordEncoder.encode(PASSWORD)
            nickname = "IntegrationTest"
        })

        val request = LoginRequest(USERNAME, PASSWORD)

        val result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)
            .andExpect(jsonPath("$.data.user.username").value(USERNAME))
            .andReturn()

        val json = objectMapper.readTree(result.response.contentAsString)
        val dataNode = json.get("data")
        val response = objectMapper.treeToValue(dataNode, LoginResponse::class.java)
        accessToken = response.accessToken
        refreshToken = response.refreshToken
    }

    @Test
    @Order(5)
    fun `login with wrong password should return 401`() {
        userRepository.save(User().apply {
            username = USERNAME
            password = passwordEncoder.encode(PASSWORD)
            nickname = "IntegrationTest"
        })

        val request = LoginRequest(USERNAME, "wrong_password")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("用户名或密码错误"))
    }

    @Test
    @Order(6)
    fun `me should return current user with valid token`() {
        val user = userRepository.save(User().apply {
            username = "me_test"
            password = passwordEncoder.encode(PASSWORD)
            nickname = "MeTest"
        })
        val token = jwtUtil.createToken(user.id!!, user.role.name)

        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("me_test"))
            .andExpect(jsonPath("$.data.nickname").value("MeTest"))
    }

    @Test
    @Order(7)
    fun `me without token should return 401`() {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(8)
    fun `refresh should return new tokens`() {
        val user = userRepository.save(User().apply {
            username = "refresh_test"
            password = passwordEncoder.encode(PASSWORD)
            nickname = "RefreshTest"
        })
        val rt = jwtUtil.createToken(user.id!!, user.role.name, 168)
        tokenService.saveRefreshToken(user.id!!, rt, 168)

        val request = RefreshTokenRequest(rt)

        val result = mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty)
            .andReturn()

        val json = objectMapper.readTree(result.response.contentAsString)
        val dataNode = json.get("data")
        val response = objectMapper.treeToValue(dataNode, LoginResponse::class.java)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
    }

    @Test
    @Order(9)
    fun `refresh with invalid token should return 401`() {
        val request = RefreshTokenRequest("invalid_refresh_token_12345")

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("refreshToken 无效或已过期"))
    }
}
