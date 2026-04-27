package com.xxgwy.modules.product.service

import com.xxgwy.common.exception.BizException
import com.xxgwy.modules.product.dto.ProductRequest
import com.xxgwy.modules.product.dto.ProductResponse
import com.xxgwy.modules.product.entity.Category
import com.xxgwy.modules.product.entity.Product
import com.xxgwy.modules.product.entity.Product.ProductStatus
import com.xxgwy.modules.product.entity.ProductSku
import com.xxgwy.modules.product.repository.CategoryRepository
import com.xxgwy.modules.product.repository.ProductRepository
import com.xxgwy.modules.product.repository.ProductSkuRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
    private val productSkuRepository: ProductSkuRepository,
    private val categoryRepository: CategoryRepository
) {

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { BizException.notFound("分类不存在") }

        val product = Product().apply {
            name = request.name
            description = request.description
            this.category = category
            imageUrl = request.imageUrl
            images = request.images.toMutableList()
            hasSpecs = request.hasSpecs
            status = ProductStatus.ENABLED
        }
        productRepository.save(product)

        val skus = if (request.hasSpecs) {
            request.skus.map { s ->
                ProductSku().apply {
                    this.product = product
                    name = s.name
                    specs = s.specs
                    price = s.price
                    stock = s.stock
                    imageUrl = s.imageUrl
                    sortOrder = s.sortOrder
                }
            }.also { productSkuRepository.saveAll(it) }
        } else {
            emptyList()
        }

        return ProductResponse.from(product, skus)
    }

    @Transactional
    fun update(id: UUID, request: ProductRequest): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { BizException.notFound("商品不存在") }
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { BizException.notFound("分类不存在") }

        product.name = request.name
        product.description = request.description
        product.category = category
        product.imageUrl = request.imageUrl
        product.images = request.images.toMutableList()
        product.hasSpecs = request.hasSpecs
        productRepository.save(product)

        val skus = if (request.hasSpecs) {
            productSkuRepository.deleteByProductId(id)
            request.skus.map { s ->
                ProductSku().apply {
                    this.product = product
                    name = s.name
                    specs = s.specs
                    price = s.price
                    stock = s.stock
                    imageUrl = s.imageUrl
                    sortOrder = s.sortOrder
                }
            }.also { productSkuRepository.saveAll(it) }
        } else {
            if (!product.hasSpecs) productSkuRepository.deleteByProductId(id)
            emptyList()
        }

        return ProductResponse.from(product, skus)
    }

    fun getById(id: UUID): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { BizException.notFound("商品不存在") }
        val skus = productSkuRepository.findByProductIdOrderBySortOrderAsc(id)
        return ProductResponse.from(product, skus)
    }

    fun list(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findAll(pageable).map { ProductResponse.from(it) }
    }

    fun search(keyword: String?, categoryId: UUID?, status: ProductStatus?, pageable: Pageable): Page<ProductResponse> {
        return productRepository.search(keyword, categoryId, status, pageable)
            .map { ProductResponse.from(it) }
    }

    fun updateStatus(id: UUID, status: ProductStatus): ProductResponse {
        val product = productRepository.findById(id)
            .orElseThrow { BizException.notFound("商品不存在") }
        product.status = status
        productRepository.save(product)
        return getById(id)
    }

    @Transactional
    fun updateStock(id: UUID, skuId: UUID?, stock: Int): ProductResponse {
        if (skuId != null) {
            val sku = productSkuRepository.findById(skuId)
                .orElseThrow { BizException.notFound("SKU不存在") }
            sku.stock = stock
            productSkuRepository.save(sku)
        }
        return getById(id)
    }

    @Transactional
    fun delete(id: UUID) {
        val product = productRepository.findById(id)
            .orElseThrow { BizException.notFound("商品不存在") }
        productSkuRepository.deleteByProductId(id)
        productRepository.delete(product)
    }
}
