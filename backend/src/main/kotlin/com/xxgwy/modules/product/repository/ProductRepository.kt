package com.xxgwy.modules.product.repository

import com.xxgwy.modules.product.entity.Category
import com.xxgwy.modules.product.entity.Product
import com.xxgwy.modules.product.entity.Product.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID> {
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<Product>
    fun findByStatus(status: ProductStatus, pageable: Pageable): Page<Product>
    fun findByCategoryIdAndStatus(categoryId: UUID, status: ProductStatus, pageable: Pageable): Page<Product>

    @Query("""
        SELECT p FROM Product p 
        WHERE (:keyword IS NULL OR p.name LIKE %:keyword%)
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:status IS NULL OR p.status = :status)
    """)
    fun search(
        @Param("keyword") keyword: String?,
        @Param("categoryId") categoryId: UUID?,
        @Param("status") status: ProductStatus?,
        pageable: Pageable
    ): Page<Product>

    fun countByCategoryId(categoryId: UUID): Long
}
