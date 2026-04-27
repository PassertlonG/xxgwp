package com.xxgwy.modules.dealer.dto

import com.xxgwy.modules.dealer.entity.Dealer
import java.time.LocalDateTime
import java.util.UUID

data class DealerResponse(
    val id: UUID,
    val userId: UUID,
    val username: String,
    val name: String,
    val phone: String?,
    val address: String?,
    val contactPerson: String?,
    val enabled: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    companion object {
        fun from(dealer: Dealer) = DealerResponse(
            id = dealer.id!!,
            userId = dealer.user!!.id!!,
            username = dealer.user!!.username,
            name = dealer.name,
            phone = dealer.phone,
            address = dealer.address,
            contactPerson = dealer.contactPerson,
            enabled = dealer.enabled,
            createdAt = dealer.createdAt,
            updatedAt = dealer.updatedAt
        )
    }
}
