package com.xxgwy.modules.dealer.repository

import com.xxgwy.modules.dealer.entity.Dealer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DealerRepository : JpaRepository<Dealer, UUID>
