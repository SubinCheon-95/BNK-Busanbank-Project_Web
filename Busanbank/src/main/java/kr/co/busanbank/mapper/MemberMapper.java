package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.UsersDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {


    UsersDTO findByUserId(@Param("userId") String userId);

    void insertUser(UsersDTO user);

    int countByUserId(@Param("userId") String userId);
    int countByEmail(@Param("email") String email);
    int countByHp(@Param("hp") String hp);

    UsersDTO findUserIdInfoEmail(@Param("userName") String userName, @Param("email") String email);

    UsersDTO findUserIdInfoHp(@Param("userName") String userName, @Param("hp") String hp);


}
