package com.xxgwy.modules.order.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders")
class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false, length = 50)
    var orderNo: String = ""

    @Column(nullable = false, length = 20)
    var status: String = "PENDING"

    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO

    @Column(length = 50)
    var paymentMethod: String? = null

    @Column(nullable = false)
    var userId: UUID? = null

    var paidAt: LocalDateTime? = null

    @Column(updatable = false)
    var createdAt: LocalDateTime? = null

    var updatedAt: LocalDateTime? = null
}
