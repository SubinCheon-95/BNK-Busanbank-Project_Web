package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Mapper
public interface MemberMapper {


    UsersDTO findByUserId(@Param("userId") String userId);

    void insertUser(UsersDTO user);

    int countByUserId(@Param("userId") String userId);
    int countByEmail(@Param("email") String email);
    int countByHp(@Param("hp") String hp);

    UsersDTO findUserIdInfoEmail(@Param("userName") String userName, @Param("email") String email);

    UsersDTO findUserIdInfoHp(@Param("userName") String userName, @Param("hp") String hp);


    UsersDTO findUserPwInfoEmail(@Param("userName") String userName, @Param("userId") String userId, @Param("email") String email);

    UsersDTO findUserPwInfoHp(@Param("userName") String userName, @Param("userId") String userId, @Param("hp") String hp);

    void updatePw(@Param("userId") String userId, @Param("encodedPass") String encodedPass);

    List<TermDTO> getTermsAll();

    //String findAccountPasswordByUserNo(Long userNo);
    // 2025/12/05 – CBC 적용 관련 로직 수정 – 작성자: 오서정
    List<String> selectAllEmails();
    List<String> selectAllHps();
    List<String> selectAllUserNames();

    List<UsersDTO> selectAllForIdFind();
    List<UsersDTO> selectAllForPwFind();

}
