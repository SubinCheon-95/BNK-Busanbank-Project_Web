package kr.co.busanbank.service;

import kr.co.busanbank.dto.ProductDTO;

import java.util.List;

public interface ProductService {

    List<ProductDTO> findAll();

    ProductDTO findById(int productNo);

    List<ProductDTO> searchProducts(String keyword);
}