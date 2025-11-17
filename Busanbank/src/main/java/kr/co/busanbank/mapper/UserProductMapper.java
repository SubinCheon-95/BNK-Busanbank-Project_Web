package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.UserProductDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** ********************************************
 *  UserProduct Mapper
 ******************************************** */
@Mapper
public interface UserProductMapper {

    /**
     * 사용자 상품 가입 등록
     * @param dto 가입 정보
     * @return 등록된 행 수
     */
    int insertUserProduct(UserProductDTO dto);

    /**
     * 사용자 상품 정보 조회 (단건)
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @param startDate 가입일 (YYYY-MM-DD)
     * @return 상품 가입 정보
     */
    UserProductDTO selectUserProduct(@Param("userId") int userId,
                                     @Param("productNo") int productNo,
                                     @Param("startDate") String startDate);

    /**
     * 사용자의 전체 가입 상품 목록 조회
     * @param userId 회원 ID
     * @return 가입 상품 목록
     */
    List<UserProductDTO> selectUserProductList(@Param("userId") int userId);

    /**
     * 사용자의 활성 상품만 조회
     * @param userId 회원 ID
     * @return 활성 상품 목록
     */
    List<UserProductDTO> selectActiveUserProducts(@Param("userId") int userId);

    /**
     * 상품 정보 수정
     * @param dto 수정할 정보
     * @return 수정된 행 수
     */
    int updateUserProduct(UserProductDTO dto);

    /**
     * 상품 해지 (상태 변경)
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @param startDate 가입일 (YYYY-MM-DD)
     * @param endDate 해지일 (YYYY-MM-DD)
     * @return 수정된 행 수
     */
    int terminateUserProduct(@Param("userId") int userId,
                             @Param("productNo") int productNo,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate);

    /**
     * 상품 삭제
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @param startDate 가입일 (YYYY-MM-DD)
     * @return 삭제된 행 수
     */
    int deleteUserProduct(@Param("userId") int userId,
                          @Param("productNo") int productNo,
                          @Param("startDate") String startDate);

    /**
     * 중복 가입 체크
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @return 가입 건수
     */
    int checkDuplicateProduct(@Param("userId") int userId,
                              @Param("productNo") int productNo);

    /**
     * 만기 예정 상품 조회
     * @param userId 회원 ID
     * @param daysBeforeMaturity 만기 며칠 전
     * @return 만기 예정 상품 목록
     */
    List<UserProductDTO> selectMaturityProducts(@Param("userId") int userId,
                                                @Param("daysBeforeMaturity") int daysBeforeMaturity);
}