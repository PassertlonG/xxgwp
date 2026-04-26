package com.xxgwy.common.config

import com.xxgwy.common.response.R
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/api/health")
    fun health(): R<Map<String, Any>> {
        return R.ok(
            mapOf(
                "status" to "UP",
                "service" to "xxgwy-backend",
                "version" to "0.1.0"
            )
        )
    }
}
