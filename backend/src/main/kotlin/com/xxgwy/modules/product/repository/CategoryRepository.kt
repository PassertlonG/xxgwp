package com.xxgwy.modules.product.repository

import com.xxgwy.modules.product.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun existsByName(name: String): Boolean
    fun findAllByOrderBySortOrderAsc(): List<Category>
    fun findByEnabledTrueOrderBySortOrderAsc(): List<Category>
}
