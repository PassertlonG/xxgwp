package com.xxgwy.modules.user

import com.fasterxml.jackson.databind.ObjectMapper
import com.xxgwy.XxgwyApplication
import com.xxgwy.common.security.JwtUtil
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
import java.util.*

@SpringBootTest(classes = [XxgwyApplication::class])
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserControllerIntegrationTest {

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

    private lateinit var adminToken: String
    private lateinit var customerToken: String
    private lateinit var targetUserId: UUID

    @BeforeEach
    fun setup() {
        val admin = userRepository.save(User().apply {
            username = "admin_test_${UUID.randomUUID().toString().take(8)}"
            password = passwordEncoder.encode("admin123")
            nickname = "Admin"
            role = User.Role.ADMIN
        })
        adminToken = jwtUtil.createToken(admin.id!!, "ADMIN")

        val customer = userRepository.save(User().apply {
            username = "customer_test_${UUID.randomUUID().toString().take(8)}"
            password = passwordEncoder.encode("customer123")
            nickname = "Customer"
            role = User.Role.CUSTOMER
        })
        customerToken = jwtUtil.createToken(customer.id!!, "CUSTOMER")

        val target = userRepository.save(User().apply {
            username = "target_test_${UUID.randomUUID().toString().take(8)}"
            password = passwordEncoder.encode("target123")
            nickname = "Target"
            role = User.Role.DEALER
        })
        targetUserId = target.id!!
    }

    @AfterEach
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Test
    fun `list users should return paginated result for admin`() {
        mockMvc.perform(
            get("/api/users")
                .header("Authorization", "Bearer $adminToken")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.list").isArray)
            .andExpect(jsonPath("$.total").isNumber)
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
    }

    @Test
    fun `list users should return 403 for non-admin`() {
        mockMvc.perform(
            get("/api/users")
                .header("Authorization", "Bearer $customerToken")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `get user by id should return user for admin`() {
        mockMvc.perform(
            get("/api/users/{id}", targetUserId)
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(targetUserId.toString()))
            .andExpect(jsonPath("$.data.role").value("DEALER"))
    }

    @Test
    fun `update user role should succeed for admin`() {
        mockMvc.perform(
            put("/api/users/{id}/role", targetUserId)
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"role":"ADMIN"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))
    }

    @Test
    fun `delete user should succeed for admin`() {
        mockMvc.perform(
            delete("/api/users/{id}", targetUserId)
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
    }

    @Test
    fun `delete non-existent user should return 404`() {
        mockMvc.perform(
            delete("/api/users/{id}", UUID.randomUUID())
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("用户不存在"))
    }
}
