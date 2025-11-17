package kr.co.busanbank.service;

import kr.co.busanbank.dto.ProductDTO;
import kr.co.busanbank.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public List<ProductDTO> findAll() {
        return productMapper.findAll();
    }

    @Override
    public ProductDTO findById(int productNo) {
        return productMapper.findById(productNo);
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        return productMapper.searchProducts(keyword);
    }
}
