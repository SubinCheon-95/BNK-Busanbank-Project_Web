package kr.co.busanbank.repository;


import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.dto.ProductDetailDTO;
import kr.co.busanbank.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 2025.11.25 김수진
@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final ProductMapper productMapper;

    public List<ProductDTO> findTopByOrderByMaturityRateDesc(int limit) {
        return productMapper.findTopByOrderByMaturityRateDesc(limit);
    }

    public List<ProductDTO> findTopSavingsByRate(int limit) {
        return productMapper.findTopSavingsByRate(limit);
    }

    public ProductDTO findByProductNo(int productNo) {
        return productMapper.selectProductById(productNo);
    }

    public List<ProductDTO> findAll() {
        return productMapper.selectAllProducts();
    }

    // --- 추가: 모든 추천용 상품을 가져오기 (productName, description, maturity rate, productNo 등)
    public List<ProductDTO> findAllForRecommendation() {
        return productMapper.findAllForRecommendation();
    }
    //List<ProductDTO> allProducts = productService.getAllProductsForRecommendation();
    // 이렇게 쓰면 repository에서 직접호출해서 service 사용하는거.

    // --- 추가: 모든 추천용 상품을 가져오기 (productName, description, maturity rate, productNo 등)
    public List<ProductDetailDTO> findAllProductDetails() {
        return productMapper.findAllProductDetails();
    }


}