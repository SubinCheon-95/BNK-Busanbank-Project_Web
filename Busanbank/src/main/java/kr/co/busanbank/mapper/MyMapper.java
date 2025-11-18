package kr.co.busanbank.mapper;


import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.dto.UsersDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyMapper {

    int countUserItems(@Param("userId") String userId);

    String getProductRecentlyDate(@Param("userId") String userId);

    String getProductLastDate(@Param("userId") String userId);

    int  updateInfo(@Param("userId") String userId, @Param("email") String email, @Param("hp") String hp, @Param("zip") String zip, @Param("addr1") String addr1, @Param("addr2") String addr2);

    UsersDTO getUserById(@Param("userId") String userId);

    String getUserPwById(@Param("userId") String userId);

    void updatePw(@Param("userId") String userId, @Param("userPw") String userPw);

    void deleteUser(@Param("userId") String userId);

    List<UserProductDTO> getUserProducts(@Param("userId") String userId);

    List<UserProductDTO> getUserProductNames(@Param("userId") String userId);

    void deleteProduct(@Param("userId") String userId, @Param("productNo") String productNo);

    UserProductDTO getCancelProduct(@Param("userId") String userId, @Param("productNo") String productNo);
}
