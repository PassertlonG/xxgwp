package com.xxgwy.modules.dealer.controller

import com.xxgwy.common.response.PageResult
import com.xxgwy.common.response.R
import com.xxgwy.modules.dealer.dto.BatchPriceRequest
import com.xxgwy.modules.dealer.dto.DealerPriceRequest
import com.xxgwy.modules.dealer.dto.DealerRequest
import com.xxgwy.modules.dealer.dto.DealerResponse
import com.xxgwy.modules.dealer.service.DealerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@Tag(name = "经销商管理")
@RestController
@RequestMapping("/api/dealers")
@PreAuthorize("hasRole('ADMIN')")
class DealerController(private val dealerService: DealerService) {

    @Operation(summary = "创建经销商（关联用户）")
    @PostMapping
    fun create(@Valid @RequestBody request: DealerRequest): R<DealerResponse> =
        R.ok(dealerService.create(request))

    @Operation(summary = "更新经销商")
    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: DealerRequest): R<DealerResponse> =
        R.ok(dealerService.update(id, request))

    @Operation(summary = "获取经销商详情")
    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): R<DealerResponse> =
        R.ok(dealerService.getById(id))

    @Operation(summary = "经销商列表（分页）")
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): PageResult<DealerResponse> {
        val result = dealerService.list(PageRequest.of(page, size))
        return PageResult.of(result.content, result.totalElements, page, size)
    }

    @Operation(summary = "启用/禁用经销商")
    @PatchMapping("/{id}/toggle")
    fun toggle(@PathVariable id: UUID): R<DealerResponse> =
        R.ok(dealerService.toggle(id))

    @Operation(summary = "删除经销商")
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): R<Nothing> {
        dealerService.delete(id)
        return R.ok()
    }

    @Operation(summary = "设置经销商专属价格")
    @PostMapping("/{id}/prices")
    fun setPrices(@PathVariable id: UUID, @Valid @RequestBody request: BatchPriceRequest): R<Nothing> {
        dealerService.setPrices(id, request.prices)
        return R.ok()
    }

    @Operation(summary = "查询经销商价格")
    @GetMapping("/{id}/prices")
    fun getPrices(
        @PathVariable id: UUID,
        @RequestParam(required = false) productId: UUID?
    ): R<List<Map<String, Any?>>> {
        val prices = dealerService.getPrices(id, productId)
        return R.ok(prices.map { p ->
            mapOf(
                "id" to p.id,
                "productId" to p.product!!.id,
                "productName" to p.product!!.name,
                "skuId" to p.sku?.id,
                "skuName" to p.sku?.name,
                "price" to p.price
            )
        })
    }
}
