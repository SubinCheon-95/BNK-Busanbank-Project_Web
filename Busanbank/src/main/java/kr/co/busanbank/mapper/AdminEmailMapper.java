package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.EmailCounselDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminEmailMapper {

    public EmailCounselDTO findById(int ecounselId);

    public List<EmailCounselDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO);

    public List<EmailCounselDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCount(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertEmail(EmailCounselDTO  emailCounselDTO);

    public void modifyEmail(EmailCounselDTO  emailCounselDTO);
}
