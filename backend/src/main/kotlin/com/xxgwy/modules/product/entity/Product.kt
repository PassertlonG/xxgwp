package com.xxgwy.modules.product.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "products")
class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false, length = 100)
    var name: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false)
    var stock: Int = 0

    @Column(nullable = false, length = 50)
    var category: String = ""

    @Column(length = 500)
    var imageUrl: String? = null

    @Column(nullable = false)
    var enabled: Boolean = true
}
