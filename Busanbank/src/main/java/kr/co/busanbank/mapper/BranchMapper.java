package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.BranchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
@Mapper
public interface BranchMapper {

    /**
     * 모든 지점 목록 조회 (운영 중인 지점만)
     */
    List<BranchDTO> selectAllBranches();

    /**
     * 지점 ID로 조회
     */
    BranchDTO selectBranchById(@Param("branchId") Integer branchId);

    /**
     * 지점 코드로 조회
     */
    BranchDTO selectBranchByCode(@Param("branchCode") String branchCode);

    /**
     * 지역별 지점 목록 조회
     */
    List<BranchDTO> selectBranchesByRegion(@Param("regionCode") String regionCode);

    /**
     * 지점 등록
     */
    int insertBranch(BranchDTO branch);

    /**
     * 지점 수정
     */
    int updateBranch(BranchDTO branch);

    /**
     * 지점 삭제 (soft delete)
     */
    int deleteBranch(@Param("branchId") Integer branchId);
}