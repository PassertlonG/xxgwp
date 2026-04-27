package com.xxgwy.modules.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.xxgwy.XxgwyApplication
import com.xxgwy.common.security.JwtUtil
import com.xxgwy.modules.product.dto.ProductRequest
import com.xxgwy.modules.product.dto.CategoryRequest
import com.xxgwy.modules.product.entity.Category
import com.xxgwy.modules.product.repository.CategoryRepository
import com.xxgwy.modules.product.repository.ProductRepository
import com.xxgwy.modules.product.repository.ProductSkuRepository
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
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest(classes = [XxgwyApplication::class])
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

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
    private lateinit var productSkuRepository: ProductSkuRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    private lateinit var adminToken: String
    private lateinit var category: Category

    @BeforeEach
    fun setup() {
        val admin = userRepository.save(User().apply {
            username = "prod_admin_${UUID.randomUUID().toString().take(8)}"
            password = passwordEncoder.encode("admin123")
            nickname = "ProdAdmin"
            role = User.Role.ADMIN
        })
        adminToken = jwtUtil.createToken(admin.id!!, "ADMIN")

        category = categoryRepository.save(Category().apply {
            name = "测试分类_${UUID.randomUUID().toString().take(8)}"
            sortOrder = 1
        })
    }

    @AfterEach
    fun cleanup() {
        productSkuRepository.deleteAll()
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `create product should succeed`() {
        val request = ProductRequest(
            name = "测试商品",
            description = "商品描述",
            categoryId = category.id!!,
            imageUrl = "http://example.com/img.jpg"
        )

        mockMvc.perform(
            post("/api/products")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").value("测试商品"))
            .andExpect(jsonPath("$.data.categoryName").value(category.name))
    }

    @Test
    fun `create product with SKUs should succeed`() {
        val request = ProductRequest(
            name = "多规格商品",
            categoryId = category.id!!,
            imageUrl = "http://example.com/img.jpg",
            hasSpecs = true,
            skus = listOf(
                ProductRequest.SkuItem(name = "红色-L", specs = "颜色:红,尺寸:L", price = BigDecimal("99.99"), stock = 10),
                ProductRequest.SkuItem(name = "蓝色-M", specs = "颜色:蓝,尺寸:M", price = BigDecimal("89.99"), stock = 20)
            )
        )

        val result = mockMvc.perform(
            post("/api/products")
                .header("Authorization", "Bearer $adminToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.hasSpecs").value(true))
            .andExpect(jsonPath("$.data.skus.length()").value(2))
            .andExpect(jsonPath("$.data.skus[0].name").value("红色-L"))
            .andReturn()
    }

    @Test
    fun `search products should work`() {
        val product = com.xxgwy.modules.product.entity.Product().apply {
            name = "可搜索的商品"
            this.category = this@ProductControllerIntegrationTest.category
            imageUrl = "test.jpg"
        }
        productRepository.save(product)

        mockMvc.perform(
            get("/api/products")
                .header("Authorization", "Bearer $adminToken")
                .param("keyword", "可搜索")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.list.length()").value(1))
            .andExpect(jsonPath("$.list[0].name").value("可搜索的商品"))
    }

    @Test
    fun `non-admin should get 403 on create`() {
        val request = ProductRequest(
            name = "无权限",
            categoryId = category.id!!,
            imageUrl = "test.jpg"
        )

        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }
}
