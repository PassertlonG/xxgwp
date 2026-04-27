package com.xxgwy.modules.user

import com.xxgwy.common.exception.BizException
import com.xxgwy.common.security.JwtConfig
import com.xxgwy.common.security.JwtUtil
import com.xxgwy.infrastructure.redis.TokenService
import com.xxgwy.modules.user.dto.LoginRequest
import com.xxgwy.modules.user.dto.RegisterRequest
import com.xxgwy.modules.user.entity.User
import com.xxgwy.modules.user.repository.UserRepository
import com.xxgwy.modules.user.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: BCryptPasswordEncoder
    private lateinit var jwtUtil: JwtUtil
    private lateinit var jwtConfig: JwtConfig
    private lateinit var tokenService: TokenService
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        passwordEncoder = BCryptPasswordEncoder()
        jwtConfig = JwtConfig().apply {
            secret = "test-secret-key-that-is-at-least-32-bytes-long-for-hmac!!"
            expirationHours = 24
            refreshExpirationHours = 168
        }
        jwtUtil = JwtUtil(jwtConfig)
        tokenService = mockk()
        userService = UserService(userRepository, passwordEncoder, jwtUtil, jwtConfig, tokenService)
    }

    @Test
    fun `register should throw conflict when username exists`() {
        every { userRepository.existsByUsername("existing") } returns true

        val ex = assertThrows<BizException> {
            userService.register(RegisterRequest("existing", "password123"))
        }
        assertEquals(409, ex.httpStatus)
        assertEquals("用户名已存在", ex.message)
    }

    @Test
    fun `register should succeed and return LoginResponse`() {
        val request = RegisterRequest("newuser", "password123", "NewUser")
        every { userRepository.existsByUsername("newuser") } returns false
        every { userRepository.save(any()) } answers {
            val u: User = firstArg()
            u.id = UUID.randomUUID()
            u
        }
        every { tokenService.saveRefreshToken(any(), any(), any()) } returns Unit

        val response = userService.register(request)

        assertEquals("newuser", response.user.username)
        assertEquals("NewUser", response.user.nickname)
        assertEquals("CUSTOMER", response.user.role)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        verify { userRepository.save(any()) }
        verify { tokenService.saveRefreshToken(any(), any(), any()) }
    }

    @Test
    fun `login should throw unauthorized when user not found`() {
        every { userRepository.findByUsername("nobody") } returns Optional.empty()

        val ex = assertThrows<BizException> {
            userService.login(LoginRequest("nobody", "password123"))
        }
        assertEquals(401, ex.httpStatus)
        assertEquals("用户名或密码错误", ex.message)
    }

    @Test
    fun `login should throw unauthorized when password wrong`() {
        val user = User().apply {
            username = "testuser"
            password = passwordEncoder.encode("correct_password")
            nickname = "Test"
            id = UUID.randomUUID()
        }
        every { userRepository.findByUsername("testuser") } returns Optional.of(user)

        val ex = assertThrows<BizException> {
            userService.login(LoginRequest("testuser", "wrong_password"))
        }
        assertEquals(401, ex.httpStatus)
        assertEquals("用户名或密码错误", ex.message)
    }

    @Test
    fun `login should succeed with valid credentials`() {
        val userId = UUID.randomUUID()
        val user = User().apply {
            id = userId
            username = "testuser"
            password = passwordEncoder.encode("correct_password")
            nickname = "Test"
            role = User.Role.CUSTOMER
        }
        every { userRepository.findByUsername("testuser") } returns Optional.of(user)
        every { tokenService.saveRefreshToken(any(), any(), any()) } returns Unit

        val response = userService.login(LoginRequest("testuser", "correct_password"))

        assertEquals("testuser", response.user.username)
        assertEquals("CUSTOMER", response.user.role)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        verify { tokenService.saveRefreshToken(any(), any(), any()) }
    }
}
