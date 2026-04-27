package com.xxgwy.modules.user.controller

import com.xxgwy.common.exception.BizException
import com.xxgwy.common.response.PageResult
import com.xxgwy.common.response.R
import com.xxgwy.modules.user.dto.UserInfoResponse
import com.xxgwy.modules.user.entity.User
import com.xxgwy.modules.user.repository.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
class UserController(private val userRepository: UserRepository) {

    @Operation(summary = "用户列表（分页）", responses = [
        ApiResponse(responseCode = "200", description = "成功"),
        ApiResponse(responseCode = "403", description = "权限不足", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): PageResult<UserInfoResponse> {
        val userPage = userRepository.findAll(PageRequest.of(page, size))
        return PageResult.of(
            userPage.content.map { UserInfoResponse.from(it) },
            userPage.totalElements,
            page,
            size
        )
    }

    @Operation(summary = "获取单个用户", responses = [
        ApiResponse(responseCode = "200", description = "成功"),
        ApiResponse(responseCode = "404", description = "用户不存在", content = [Content(schema = Schema(implementation = R::class))]),
        ApiResponse(responseCode = "403", description = "权限不足", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): R<UserInfoResponse> {
        val user = userRepository.findById(id).orElseThrow { BizException.notFound("用户不存在") }
        return R.ok(UserInfoResponse.from(user))
    }

    @Operation(summary = "修改用户角色", responses = [
        ApiResponse(responseCode = "200", description = "成功"),
        ApiResponse(responseCode = "404", description = "用户不存在", content = [Content(schema = Schema(implementation = R::class))]),
        ApiResponse(responseCode = "403", description = "权限不足", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @PutMapping("/{id}/role")
    fun updateRole(@PathVariable id: UUID, @RequestBody request: UpdateRoleRequest): R<UserInfoResponse> {
        val user = userRepository.findById(id).orElseThrow { BizException.notFound("用户不存在") }
        user.role = request.role
        userRepository.save(user)
        return R.ok(UserInfoResponse.from(user))
    }

    @Operation(summary = "删除用户", responses = [
        ApiResponse(responseCode = "200", description = "成功"),
        ApiResponse(responseCode = "404", description = "用户不存在", content = [Content(schema = Schema(implementation = R::class))]),
        ApiResponse(responseCode = "403", description = "权限不足", content = [Content(schema = Schema(implementation = R::class))])
    ])
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): R<Nothing> {
        val user = userRepository.findById(id).orElseThrow { BizException.notFound("用户不存在") }
        userRepository.delete(user)
        return R.ok()
    }

    data class UpdateRoleRequest(val role: User.Role)
}
