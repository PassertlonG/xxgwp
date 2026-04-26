package com.xxgwy.modules.product.repository

import com.xxgwy.modules.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProductRepository : JpaRepository<Product, UUID>
