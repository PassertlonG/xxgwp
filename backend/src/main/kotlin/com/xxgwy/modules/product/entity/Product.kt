package com.xxgwy.modules.product.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null

    @Column(nullable = false, length = 200)
    var imageUrl: String = ""

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = [JoinColumn(name = "product_id")])
    @Column(name = "image_url", length = 500)
    var images: MutableList<String> = mutableListOf()

    @Column(nullable = false)
    var hasSpecs: Boolean = false

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: ProductStatus = ProductStatus.DRAFT

    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null

    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }

    enum class ProductStatus {
        DRAFT, ENABLED, DISABLED
    }
}
