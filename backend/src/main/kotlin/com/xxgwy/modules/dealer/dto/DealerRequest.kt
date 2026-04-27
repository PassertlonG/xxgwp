package com.xxgwy.modules.dealer.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class DealerRequest(
    @field:NotBlank @field:Size(max = 100)
    val name: String,

    val userId: UUID,

    @field:Size(max = 20)
    val phone: String? = null,

    @field:Size(max = 200)
    val address: String? = null,

    @field:Size(max = 100)
    val contactPerson: String? = null
)
