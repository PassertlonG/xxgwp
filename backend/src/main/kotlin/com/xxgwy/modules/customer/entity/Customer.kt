package com.xxgwy.modules.customer.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "customers")
class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false, length = 100)
    var name: String = ""

    @Column(length = 20)
    var phone: String? = null

    @Column(length = 200)
    var address: String? = null

    @Column(nullable = false)
    var enabled: Boolean = true
}
