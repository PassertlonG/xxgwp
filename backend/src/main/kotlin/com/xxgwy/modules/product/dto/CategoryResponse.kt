package com.xxgwy.modules.product.dto

import com.xxgwy.modules.product.entity.Category
import java.util.UUID

data class CategoryResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val sortOrder: Int,
    val enabled: Boolean,
    val productCount: Long = 0
) {
    companion object {
        fun from(category: Category, productCount: Long = 0) = CategoryResponse(
            id = category.id!!,
            name = category.name,
            description = category.description,
            sortOrder = category.sortOrder,
            enabled = category.enabled,
            productCount = productCount
        )
    }
}
