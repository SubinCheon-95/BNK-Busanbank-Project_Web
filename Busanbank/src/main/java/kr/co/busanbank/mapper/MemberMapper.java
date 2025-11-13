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

    UsersDTO login(@Param("userId") String userId, @Param("userPw") String userPw);
}
