package com.xxgwy.modules.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoryRequest(
    @field:NotBlank @field:Size(max = 50)
    val name: String,

    @field:Size(max = 200)
    val description: String? = null,

    val sortOrder: Int = 0
)
