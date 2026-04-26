package com.xxgwy.modules.cart.repository

import com.xxgwy.modules.cart.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CartRepository : JpaRepository<Cart, UUID>
