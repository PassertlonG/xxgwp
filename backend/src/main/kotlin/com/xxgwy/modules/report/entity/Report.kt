package com.xxgwy.modules.report.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "reports")
class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false, length = 100)
    var title: String = ""

    @Column(columnDefinition = "TEXT")
    var content: String? = null

    @Column(length = 50)
    var type: String = ""

    @Column(updatable = false)
    var createdAt: LocalDateTime? = null
}
