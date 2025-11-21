package kr.co.busanbank.service;

import kr.co.busanbank.dto.ConsultantDTO;
import kr.co.busanbank.mapper.ConsultantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ConsultantService {

    private final ConsultantMapper consultantMapper;

    public int registerConsultant(String name, String specialty){
        ConsultantDTO dto = new ConsultantDTO();
        dto.setConsultantName(name);
        dto.setSpecialty(specialty);

        return consultantMapper.insertConsultant(dto);
    }

    // 상담원 조회
    public ConsultantDTO getConsultant(int consultantId){
        return consultantMapper.selectConsultantById(consultantId);
    }

    // 상담원 상태 변경
    public int updateStatus(int consultantId, String status){
        return consultantMapper.updateConsultantStatus(consultantId, status);
    }

    // READY 상담원 목록
    public List<ConsultantDTO> getReadyConsultant(){
        return consultantMapper.selectConsultantByStatus("READY");
    }

    // 로그인 시 READY 로 변경
    public int consultantLogin(int consultantId){
        return consultantMapper.updateConsultantStatus(consultantId, "READY");
    }

    // 로그아웃 시 OFFLINE으로 변경
    public int consultantLogout(int consultantId){
        return consultantMapper.updateConsultantStatus(consultantId, "OFFLINE");
    }
}
