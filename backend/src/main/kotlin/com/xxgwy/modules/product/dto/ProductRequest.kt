package com.xxgwy.modules.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

data class ProductRequest(
    @field:NotBlank @field:Size(max = 100)
    val name: String,

    val description: String? = null,

    val categoryId: UUID,

    @field:Size(max = 200)
    val imageUrl: String = "",

    val images: List<String> = emptyList(),

    val hasSpecs: Boolean = false,

    val skus: List<SkuItem> = emptyList()
) {
    data class SkuItem(
        @field:NotBlank @field:Size(max = 100)
        val name: String,

        val specs: String = "",

        @field:PositiveOrZero
        val price: BigDecimal = BigDecimal.ZERO,

        @field:PositiveOrZero
        val stock: Int = 0,

        val imageUrl: String? = null,

        val sortOrder: Int = 0,

        val id: UUID? = null
    )
}
