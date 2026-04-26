package com.xxgwy.modules.customer.repository

import com.xxgwy.modules.customer.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository : JpaRepository<Customer, UUID>
