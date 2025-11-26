package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.CsPDFDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminArchiveMapper {

    public void insertPDF(CsPDFDTO csPDFDTO);
}