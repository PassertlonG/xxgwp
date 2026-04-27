package com.xxgwy.modules.dealer.entity

import com.xxgwy.modules.product.entity.Product
import com.xxgwy.modules.product.entity.ProductSku
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "dealer_prices", uniqueConstraints = [
    UniqueConstraint(columnNames = ["dealer_id", "product_id", "sku_id"])
])
class DealerPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    var dealer: Dealer? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    var sku: ProductSku? = null

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO
}
