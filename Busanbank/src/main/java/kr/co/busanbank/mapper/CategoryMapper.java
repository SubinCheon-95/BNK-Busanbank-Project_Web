package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CategoryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    CategoryDTO findById(int categoryId);

    List<CategoryDTO> findDepth1();

    List<CategoryDTO> findChildren(int parentId);

}