package com.xxgwy.modules.dealer.repository

import com.xxgwy.modules.dealer.entity.Dealer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface DealerRepository : JpaRepository<Dealer, UUID> {
    fun findByUserId(userId: UUID): Optional<Dealer>
    fun existsByUserId(userId: UUID): Boolean
}
