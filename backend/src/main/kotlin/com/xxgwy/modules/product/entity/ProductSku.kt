package com.xxgwy.modules.product.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "product_skus")
class ProductSku {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null

    @Column(nullable = false, length = 100)
    var name: String = ""

    @Column(length = 200)
    var specs: String = ""

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO

    @Column(nullable = false)
    var stock: Int = 0

    @Column(length = 500)
    var imageUrl: String? = null

    @Column(nullable = false)
    var sortOrder: Int = 0
}
