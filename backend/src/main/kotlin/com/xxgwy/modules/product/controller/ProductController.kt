package com.xxgwy.modules.product.controller

import com.xxgwy.common.response.PageResult
import com.xxgwy.common.response.R
import com.xxgwy.modules.product.dto.ProductRequest
import com.xxgwy.modules.product.dto.ProductResponse
import com.xxgwy.modules.product.entity.Product.ProductStatus
import com.xxgwy.modules.product.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "商品管理")
@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @Operation(summary = "创建商品")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@Valid @RequestBody request: ProductRequest): R<ProductResponse> =
        R.ok(productService.create(request))

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: ProductRequest): R<ProductResponse> =
        R.ok(productService.update(id, request))

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): R<ProductResponse> =
        R.ok(productService.getById(id))

    @Operation(summary = "商品列表（分页）")
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) categoryId: UUID?,
        @RequestParam(required = false) status: ProductStatus?
    ): PageResult<ProductResponse> {
        val pageable = PageRequest.of(page, size)
        val result = productService.search(keyword, categoryId, status, pageable)
        return PageResult.of(result.content, result.totalElements, page, size)
    }

    @Operation(summary = "上架商品")
    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    fun enable(@PathVariable id: UUID): R<ProductResponse> =
        R.ok(productService.updateStatus(id, ProductStatus.ENABLED))

    @Operation(summary = "下架商品")
    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    fun disable(@PathVariable id: UUID): R<ProductResponse> =
        R.ok(productService.updateStatus(id, ProductStatus.DISABLED))

    @Operation(summary = "更新库存")
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateStock(
        @PathVariable id: UUID,
        @RequestParam(required = false) skuId: UUID?,
        @RequestParam stock: Int
    ): R<ProductResponse> =
        R.ok(productService.updateStock(id, skuId, stock))

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): R<Nothing> {
        productService.delete(id)
        return R.ok()
    }
}
