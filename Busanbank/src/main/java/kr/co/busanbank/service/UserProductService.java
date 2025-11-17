package kr.co.busanbank.service;

import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.mapper.UserProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** *******************************************
 *  UserProductService
 ******************************************** */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProductService {

    private final UserProductMapper userProductMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 상품 가입 등록
     * @param dto 가입 정보
     * @return 등록 성공 여부
     */
    @Transactional
    public boolean registerProduct(UserProductDTO dto) {
        log.info("상품 가입 시작 - userId: {}, productNo: {}", dto.getUserId(), dto.getProductNo());

        // 1. 중복 가입 체크
        int duplicateCount = userProductMapper.checkDuplicateProduct(dto.getUserId(), dto.getProductNo());
        if (duplicateCount > 0) {
            log.warn("이미 가입된 상품입니다 - userId: {}, productNo: {}", dto.getUserId(), dto.getProductNo());
            throw new IllegalStateException("이미 가입된 상품입니다.");
        }

        // 2. 예상 만기일 계산 (없는 경우)
        if (dto.getExpectedEndDate() == null || dto.getExpectedEndDate().isEmpty()) {
            LocalDate startDate = LocalDate.parse(dto.getStartDate(), DATE_FORMATTER);
            LocalDate expectedEndDate = startDate.plusMonths(dto.getContractTerm());
            dto.setExpectedEndDate(expectedEndDate.format(DATE_FORMATTER));
        }

        // 3. 초기 상태 설정
        if (dto.getStatus() == null || dto.getStatus().isEmpty()) {
            dto.setStatus("A"); // A: 활성
        }

        // 4. 가입 등록
        int result = userProductMapper.insertUserProduct(dto);

        if (result > 0) {
            log.info("상품 가입 완료 - userId: {}, productNo: {}", dto.getUserId(), dto.getProductNo());
            return true;
        } else {
            log.error("상품 가입 실패 - userId: {}, productNo: {}", dto.getUserId(), dto.getProductNo());
            return false;
        }
    }

    /**
     * 상품 정보 조회 (단건)
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @param startDate 가입일
     * @return 상품 정보
     */
    public UserProductDTO getProduct(int userId, int productNo, String startDate) {
        log.info("상품 조회 - userId: {}, productNo: {}, startDate: {}", userId, productNo, startDate);
        return userProductMapper.selectUserProduct(userId, productNo, startDate);
    }

    /**
     * 사용자의 전체 가입 상품 목록 조회
     * @param userId 회원 ID
     * @return 가입 상품 목록
     */
    public List<UserProductDTO> getProductList(int userId) {
        log.info("전체 상품 목록 조회 - userId: {}", userId);
        return userProductMapper.selectUserProductList(userId);
    }

    /**
     * 사용자의 활성 상품만 조회
     * @param userId 회원 ID
     * @return 활성 상품 목록
     */
    public List<UserProductDTO> getActiveProducts(int userId) {
        log.info("활성 상품 목록 조회 - userId: {}", userId);
        return userProductMapper.selectActiveUserProducts(userId);
    }

    /**
     * 상품 정보 수정
     * @param dto 수정할 정보
     * @return 수정 성공 여부
     */
    @Transactional
    public boolean updateProduct(UserProductDTO dto) {
        log.info("상품 정보 수정 - userId: {}, productNo: {}", dto.getUserId(), dto.getProductNo());

        int result = userProductMapper.updateUserProduct(dto);

        if (result > 0) {
            log.info("상품 정보 수정 완료");
            return true;
        } else {
            log.warn("수정할 상품을 찾을 수 없습니다");
            return false;
        }
    }

    /**
     * 상품 해지
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @param startDate 가입일
     * @return 해지 성공 여부
     */
    @Transactional
    public boolean terminateProduct(int userId, int productNo, String startDate) {
        log.info("상품 해지 - userId: {}, productNo: {}, startDate: {}", userId, productNo, startDate);

        String endDate = LocalDate.now().format(DATE_FORMATTER);
        int result = userProductMapper.terminateUserProduct(userId, productNo, startDate, endDate);

        if (result > 0) {
            log.info("상품 해지 완료");
            return true;
        } else {
            log.warn("해지할 상품을 찾을 수 없습니다");
            return false;
        }
    }

    /**
     * 상품 삭제 (실제 데이터 삭제)
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @param startDate 가입일
     * @return 삭제 성공 여부
     */
    @Transactional
    public boolean deleteProduct(int userId, int productNo, String startDate) {
        log.info("상품 삭제 - userId: {}, productNo: {}, startDate: {}", userId, productNo, startDate);

        int result = userProductMapper.deleteUserProduct(userId, productNo, startDate);

        if (result > 0) {
            log.info("상품 삭제 완료");
            return true;
        } else {
            log.warn("삭제할 상품을 찾을 수 없습니다");
            return false;
        }
    }

    /**
     * 만기 예정 상품 조회
     * @param userId 회원 ID
     * @param daysBeforeMaturity 만기 며칠 전 (예: 7일, 30일)
     * @return 만기 예정 상품 목록
     */
    public List<UserProductDTO> getMaturityProducts(int userId, int daysBeforeMaturity) {
        log.info("만기 예정 상품 조회 - userId: {}, days: {}", userId, daysBeforeMaturity);
        return userProductMapper.selectMaturityProducts(userId, daysBeforeMaturity);
    }

    /**
     * 중복 가입 체크
     * @param userId 회원 ID
     * @param productNo 상품 번호
     * @return 중복 여부
     */
    public boolean isDuplicateProduct(int userId, int productNo) {
        int count = userProductMapper.checkDuplicateProduct(userId, productNo);
        return count > 0;
    }
}