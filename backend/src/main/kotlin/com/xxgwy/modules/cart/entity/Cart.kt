package com.xxgwy.modules.cart.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "cart_items")
class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false)
    var userId: UUID? = null

    @Column(nullable = false)
    var productId: UUID? = null

    @Column(nullable = false)
    var quantity: Int = 1

    @Column(precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO
}
