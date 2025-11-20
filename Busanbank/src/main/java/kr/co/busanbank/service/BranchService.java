package kr.co.busanbank.service;

import kr.co.busanbank.dto.BranchDTO;
import kr.co.busanbank.mapper.BranchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BranchService {

    private final BranchMapper branchMapper;

    /**
     * 모든 지점 목록 조회
     */
    public List<BranchDTO> getAllBranches() {
        log.info("모든 지점 목록 조회");
        return branchMapper.selectAllBranches();
    }

    /**
     * 지점 ID로 조회
     */
    public BranchDTO getBranchById(Integer branchId) {
        log.info("지점 조회 - branchId: {}", branchId);
        return branchMapper.selectBranchById(branchId);
    }

    /**
     * 지점 코드로 조회
     */
    public BranchDTO getBranchByCode(String branchCode) {
        log.info("지점 조회 - branchCode: {}", branchCode);
        return branchMapper.selectBranchByCode(branchCode);
    }

    /**
     * 지역별 지점 목록 조회
     */
    public List<BranchDTO> getBranchesByRegion(String regionCode) {
        log.info("지역별 지점 조회 - regionCode: {}", regionCode);
        return branchMapper.selectBranchesByRegion(regionCode);
    }

    /**
     * 지점 등록
     */
    public boolean createBranch(BranchDTO branch) {
        log.info("지점 등록: {}", branch);
        int result = branchMapper.insertBranch(branch);
        return result > 0;
    }

    /**
     * 지점 수정
     */
    public boolean updateBranch(BranchDTO branch) {
        log.info("지점 수정: {}", branch);
        int result = branchMapper.updateBranch(branch);
        return result > 0;
    }

    /**
     * 지점 삭제
     */
    public boolean deleteBranch(Integer branchId) {
        log.info("지점 삭제 - branchId: {}", branchId);
        int result = branchMapper.deleteBranch(branchId);
        return result > 0;
    }
}