package com.xxgwy.modules.dealer.repository

import com.xxgwy.modules.dealer.entity.DealerPrice
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface DealerPriceRepository : JpaRepository<DealerPrice, UUID> {
    fun findByDealerId(dealerId: UUID): List<DealerPrice>
    fun findByDealerIdAndProductId(dealerId: UUID, productId: UUID): List<DealerPrice>
    fun findByDealerIdAndProductIdAndSkuId(
        dealerId: UUID, productId: UUID, skuId: UUID?
    ): Optional<DealerPrice>
    fun findByProductId(productId: UUID): List<DealerPrice>
    fun deleteByDealerIdAndProductId(dealerId: UUID, productId: UUID)
}
