package com.xxgwy.modules.product.service

import com.xxgwy.common.exception.BizException
import com.xxgwy.modules.product.dto.CategoryRequest
import com.xxgwy.modules.product.dto.CategoryResponse
import com.xxgwy.modules.product.entity.Category
import com.xxgwy.modules.product.repository.CategoryRepository
import com.xxgwy.modules.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository
) {

    fun create(request: CategoryRequest): CategoryResponse {
        if (categoryRepository.existsByName(request.name)) {
            throw BizException.conflict("分类名称已存在")
        }
        val category = Category().apply {
            name = request.name
            description = request.description
            sortOrder = request.sortOrder
        }
        categoryRepository.save(category)
        return CategoryResponse.from(category)
    }

    fun update(id: UUID, request: CategoryRequest): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { BizException.notFound("分类不存在") }
        if (request.name != category.name && categoryRepository.existsByName(request.name)) {
            throw BizException.conflict("分类名称已存在")
        }
        category.name = request.name
        category.description = request.description
        category.sortOrder = request.sortOrder
        categoryRepository.save(category)
        return CategoryResponse.from(category)
    }

    fun getById(id: UUID): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { BizException.notFound("分类不存在") }
        val productCount = productRepository.countByCategoryId(id)
        return CategoryResponse.from(category, productCount)
    }

    fun list(): List<CategoryResponse> {
        return categoryRepository.findAllByOrderBySortOrderAsc().map { cat ->
            CategoryResponse.from(cat, productRepository.countByCategoryId(cat.id!!))
        }
    }

    fun toggle(id: UUID): CategoryResponse {
        val category = categoryRepository.findById(id)
            .orElseThrow { BizException.notFound("分类不存在") }
        category.enabled = !category.enabled
        categoryRepository.save(category)
        return CategoryResponse.from(category)
    }

    fun delete(id: UUID) {
        val category = categoryRepository.findById(id)
            .orElseThrow { BizException.notFound("分类不存在") }
        if (productRepository.countByCategoryId(id) > 0) {
            throw BizException.badRequest("该分类下还有商品，无法删除")
        }
        categoryRepository.delete(category)
    }
}
