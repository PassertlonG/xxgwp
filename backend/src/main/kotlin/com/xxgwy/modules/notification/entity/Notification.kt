package com.xxgwy.modules.notification.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notifications")
class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    var id: UUID? = null

    @Column(nullable = false)
    var userId: UUID? = null

    @Column(nullable = false, length = 200)
    var title: String = ""

    @Column(columnDefinition = "TEXT")
    var content: String? = null

    @Column(nullable = false)
    var read: Boolean = false

    @Column(updatable = false)
    var createdAt: LocalDateTime? = null
}
