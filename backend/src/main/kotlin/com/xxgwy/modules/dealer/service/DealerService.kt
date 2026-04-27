package com.xxgwy.modules.dealer.service

import com.xxgwy.common.exception.BizException
import com.xxgwy.modules.dealer.dto.DealerPriceRequest
import com.xxgwy.modules.dealer.dto.DealerRequest
import com.xxgwy.modules.dealer.dto.DealerResponse
import com.xxgwy.modules.dealer.entity.Dealer
import com.xxgwy.modules.dealer.entity.DealerPrice
import com.xxgwy.modules.dealer.repository.DealerPriceRepository
import com.xxgwy.modules.dealer.repository.DealerRepository
import com.xxgwy.modules.product.repository.ProductRepository
import com.xxgwy.modules.product.repository.ProductSkuRepository
import com.xxgwy.modules.user.entity.User
import com.xxgwy.modules.user.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DealerService(
    private val dealerRepository: DealerRepository,
    private val dealerPriceRepository: DealerPriceRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val productSkuRepository: ProductSkuRepository
) {

    @Transactional
    fun create(request: DealerRequest): DealerResponse {
        if (dealerRepository.existsByUserId(request.userId)) {
            throw BizException.conflict("该用户已是经销商")
        }
        val user = userRepository.findById(request.userId)
            .orElseThrow { BizException.notFound("用户不存在") }
        user.role = User.Role.DEALER
        userRepository.save(user)

        val dealer = Dealer().apply {
            this.user = user
            name = request.name
            phone = request.phone
            address = request.address
            contactPerson = request.contactPerson
        }
        dealerRepository.save(dealer)
        return DealerResponse.from(dealer)
    }

    @Transactional
    fun update(id: UUID, request: DealerRequest): DealerResponse {
        val dealer = dealerRepository.findById(id)
            .orElseThrow { BizException.notFound("经销商不存在") }
        dealer.name = request.name
        dealer.phone = request.phone
        dealer.address = request.address
        dealer.contactPerson = request.contactPerson
        dealerRepository.save(dealer)
        return DealerResponse.from(dealer)
    }

    fun getById(id: UUID): DealerResponse {
        val dealer = dealerRepository.findById(id)
            .orElseThrow { BizException.notFound("经销商不存在") }
        return DealerResponse.from(dealer)
    }

    fun list(pageable: Pageable): Page<DealerResponse> {
        return dealerRepository.findAll(pageable).map { DealerResponse.from(it) }
    }

    fun toggle(id: UUID): DealerResponse {
        val dealer = dealerRepository.findById(id)
            .orElseThrow { BizException.notFound("经销商不存在") }
        dealer.enabled = !dealer.enabled
        dealerRepository.save(dealer)
        return DealerResponse.from(dealer)
    }

    @Transactional
    fun delete(id: UUID) {
        val dealer = dealerRepository.findById(id)
            .orElseThrow { BizException.notFound("经销商不存在") }
        dealerPriceRepository.deleteByDealerIdAndProductId(id, UUID.randomUUID())
        dealerRepository.delete(dealer)
    }

    @Transactional
    fun setPrices(dealerId: UUID, prices: List<DealerPriceRequest>) {
        val dealer = dealerRepository.findById(dealerId)
            .orElseThrow { BizException.notFound("经销商不存在") }
        if (!dealer.enabled) throw BizException.badRequest("经销商已禁用")

        prices.forEach { p ->
            val product = productRepository.findById(p.productId)
                .orElseThrow { BizException.notFound("商品不存在: ${p.productId}") }
            val sku = p.skuId?.let {
                productSkuRepository.findById(it)
                    .orElseThrow { BizException.notFound("SKU不存在: $it") }
            }

            val existing = dealerPriceRepository.findByDealerIdAndProductIdAndSkuId(
                dealerId, p.productId, p.skuId
            )

            if (existing.isPresent) {
                existing.get().price = p.price
                dealerPriceRepository.save(existing.get())
            } else {
                dealerPriceRepository.save(DealerPrice().apply {
                    this.dealer = dealer
                    this.product = product
                    this.sku = sku
                    this.price = p.price
                })
            }
        }
    }

    fun getPrices(dealerId: UUID, productId: UUID? = null): List<DealerPrice> {
        val dealer = dealerRepository.findById(dealerId)
            .orElseThrow { BizException.notFound("经销商不存在") }
        return if (productId != null) {
            dealerPriceRepository.findByDealerIdAndProductId(dealerId, productId)
        } else {
            dealerPriceRepository.findByDealerId(dealerId)
        }
    }
}
