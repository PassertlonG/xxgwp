package com.xxgwy.modules.product.repository

import com.xxgwy.modules.product.entity.ProductSku
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductSkuRepository : JpaRepository<ProductSku, UUID> {
    fun findByProductIdOrderBySortOrderAsc(productId: UUID): List<ProductSku>
    fun deleteByProductId(productId: UUID)
}
