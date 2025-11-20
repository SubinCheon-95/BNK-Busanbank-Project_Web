package kr.co.busanbank.controller;

import kr.co.busanbank.dto.BranchDTO;
import kr.co.busanbank.dto.EmployeeDTO;
import kr.co.busanbank.service.BranchService;
import kr.co.busanbank.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/branch")
public class BranchController {

    private final BranchService branchService;
    private final EmployeeService employeeService;

    /**
     * 모든 지점 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<BranchDTO>> getAllBranches() {
        log.info("API: 모든 지점 목록 조회");
        List<BranchDTO> branches = branchService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    /**
     * 지점별 직원 목록 조회
     * URL: /api/branch/{branchId}/employees
     */
    @GetMapping("/{branchId}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByBranch(@PathVariable Integer branchId) {
        log.info("API: 지점별 직원 조회 - branchId: {}", branchId);
        List<EmployeeDTO> employees = employeeService.getEmployeesByBranch(branchId);
        return ResponseEntity.ok(employees);
    }

    /**
     * 지점 상세 조회
     */
    @GetMapping("/{branchId}")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable Integer branchId) {
        log.info("API: 지점 상세 조회 - branchId: {}", branchId);
        BranchDTO branch = branchService.getBranchById(branchId);
        return ResponseEntity.ok(branch);
    }

    /**
     * 직원 상세 조회
     */
    @GetMapping("/employee/{empId}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Integer empId) {
        log.info("API: 직원 상세 조회 - empId: {}", empId);
        EmployeeDTO employee = employeeService.getEmployeeById(empId);
        return ResponseEntity.ok(employee);
    }
}