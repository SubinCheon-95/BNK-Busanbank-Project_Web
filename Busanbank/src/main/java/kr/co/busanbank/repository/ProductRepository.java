package kr.co.busanbank.repository;


import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}