package com.xxgwy.modules.payment.repository

import com.xxgwy.modules.payment.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PaymentRepository : JpaRepository<Payment, UUID>
