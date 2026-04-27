package com.xxgwy.modules.product.dto

import com.xxgwy.modules.product.entity.Product
import com.xxgwy.modules.product.entity.Product.ProductStatus
import com.xxgwy.modules.product.entity.ProductSku
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ProductResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val categoryId: UUID,
    val categoryName: String,
    val imageUrl: String,
    val images: List<String>,
    val hasSpecs: Boolean,
    val status: ProductStatus,
    val minPrice: BigDecimal?,
    val totalStock: Int,
    val skus: List<SkuResponse>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(product: Product, skus: List<ProductSku> = emptyList()): ProductResponse {
            val prices = skus.map { it.price }
            return ProductResponse(
                id = product.id!!,
                name = product.name,
                description = product.description,
                categoryId = product.category!!.id!!,
                categoryName = product.category!!.name,
                imageUrl = product.imageUrl,
                images = product.images.toList(),
                hasSpecs = product.hasSpecs,
                status = product.status,
                minPrice = if (prices.isEmpty()) null else prices.min(),
                totalStock = if (product.hasSpecs) skus.sumOf { it.stock } else 0,
                skus = skus.map { SkuResponse.from(it) },
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}

data class SkuResponse(
    val id: UUID,
    val name: String,
    val specs: String,
    val price: BigDecimal,
    val stock: Int,
    val imageUrl: String?,
    val sortOrder: Int
) {
    companion object {
        fun from(sku: ProductSku) = SkuResponse(
            id = sku.id!!,
            name = sku.name,
            specs = sku.specs,
            price = sku.price,
            stock = sku.stock,
            imageUrl = sku.imageUrl,
            sortOrder = sku.sortOrder
        )
    }
}
