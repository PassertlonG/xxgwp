package com.xxgwy.modules.order.repository

import com.xxgwy.modules.order.entity.Order
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OrderRepository : JpaRepository<Order, UUID>
