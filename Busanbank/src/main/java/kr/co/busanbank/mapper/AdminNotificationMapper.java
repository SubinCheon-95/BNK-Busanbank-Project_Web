package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.NotificationDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminNotificationMapper {
    public List<NotificationDTO> findAutoList();
    public void markSent(@Param("id") int id);

    public List<NotificationDTO> findAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int selectCount(@Param("pageRequestDTO")  PageRequestDTO pageRequestDTO);

    public List<NotificationDTO> searchAll(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);
    public int searchCountTotal(@Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    public void insertPush(NotificationDTO notificationDTO);

    public void singleDelete(@Param("id") int id);
    public void delete(@Param("list") List<Long> idList);
}