package com.xxgwy.modules.payment.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payments")
class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false, length = 50)
    var paymentNo: String = ""

    @Column(nullable = false)
    var orderId: UUID? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO

    @Column(length = 30)
    var method: String? = null

    @Column(length = 20)
    var status: String = "PENDING"

    @Column(updatable = false)
    var createdAt: LocalDateTime? = null
}
