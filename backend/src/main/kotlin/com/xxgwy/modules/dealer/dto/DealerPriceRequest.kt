package com.xxgwy.modules.dealer.dto

import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal
import java.util.UUID

data class DealerPriceRequest(
    val productId: UUID,
    val skuId: UUID? = null,
    @field:PositiveOrZero
    val price: BigDecimal
)

data class BatchPriceRequest(
    val prices: List<DealerPriceRequest>
)
