package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.ConsultantDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsultantMapper {

    // 상담원 등록
    int insertConsultant(ConsultantDTO consultant);

    // 상담원 상태 변경
    int updateConsultantStatus(@Param("consultantId") int consultantId,
                                @Param("status") String status);

    int updateConsultant(ConsultantDTO consultant);

    // pk로 조회
    ConsultantDTO selectConsultantById(@Param("consultantId") int consultantId);

    // 상태 기준 조회
    List<ConsultantDTO> selectConsultantByStatus(@Param("status") String status);

}
