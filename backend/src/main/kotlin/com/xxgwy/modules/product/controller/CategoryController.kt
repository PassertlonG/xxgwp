package com.xxgwy.modules.product.controller

import com.xxgwy.common.response.R
import com.xxgwy.modules.product.dto.CategoryRequest
import com.xxgwy.modules.product.dto.CategoryResponse
import com.xxgwy.modules.product.service.CategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "商品分类管理")
@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryService: CategoryService) {

    @Operation(summary = "创建分类")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: CategoryRequest): R<CategoryResponse> =
        R.ok(categoryService.create(request))

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: CategoryRequest): R<CategoryResponse> =
        R.ok(categoryService.update(id, request))

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): R<CategoryResponse> =
        R.ok(categoryService.getById(id))

    @Operation(summary = "分类列表（全部）")
    @GetMapping
    fun list(): R<List<CategoryResponse>> =
        R.ok(categoryService.list())

    @Operation(summary = "启用/禁用分类")
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    fun toggle(@PathVariable id: UUID): R<CategoryResponse> =
        R.ok(categoryService.toggle(id))

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): R<Nothing> {
        categoryService.delete(id)
        return R.ok()
    }
}
