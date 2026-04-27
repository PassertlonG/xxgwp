package com.xxgwy.modules.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.xxgwy.XxgwyApplication
import com.xxgwy.common.security.JwtUtil
import com.xxgwy.modules.product.dto.CategoryRequest
import com.xxgwy.modules.product.repository.CategoryRepository
import com.xxgwy.modules.product.repository.ProductRepository
import com.xxgwy.modules.user.entity.User
import com.xxgwy.modules.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import java.util.UUID

@SpringBootTest(classes = [XxgwyApplication::class])
@AutoConfigureMockMvc
class CategoryControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    private lateinit var adminToken: String

    @BeforeEach
    fun setup() {
        val admin = userRepository.save(User().apply {
            username = "cat_admin_${UUID.randomUUID().toString().take(8)}"
            password = passwordEncoder.encode("admin123")
            nickname = "CatAdmin"
            role = User.Role.ADMIN
        })
        adminToken = jwtUtil.createToken(admin.id!!, "ADMIN")
    }

    @AfterEach
    fun cleanup() {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `create category should succeed for admin`() {
        val request = CategoryRequest("测试分类", "描述", 1)

        mockMvc.perform(
            post("/api/categories")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").value("测试分类"))
            .andExpect(jsonPath("$.data.description").value("描述"))
            .andExpect(jsonPath("$.data.sortOrder").value(1))
    }

    @Test
    fun `list categories should return all`() {
        categoryRepository.save(com.xxgwy.modules.product.entity.Category().apply {
            name = "分类A"
            sortOrder = 2
        })
        categoryRepository.save(com.xxgwy.modules.product.entity.Category().apply {
            name = "分类B"
            sortOrder = 1
        })

        mockMvc.perform(
            get("/api/categories")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].name").value("分类B"))
            .andExpect(jsonPath("$.data[1].name").value("分类A"))
    }

    @Test
    fun `delete category with products should fail`() {
        val cat = categoryRepository.save(com.xxgwy.modules.product.entity.Category().apply {
            name = "有商品的分类"
            sortOrder = 1
        })
        productRepository.save(com.xxgwy.modules.product.entity.Product().apply {
            name = "测试商品"
            category = cat
            imageUrl = "test.jpg"
        })

        mockMvc.perform(
            delete("/api/categories/${cat.id}")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isBadRequest)
    }
}
