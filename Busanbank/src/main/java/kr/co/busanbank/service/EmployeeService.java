package kr.co.busanbank.service;

import kr.co.busanbank.dto.EmployeeDTO;
import kr.co.busanbank.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 작성자: AI Assistant
 * 작성일: 2025-01-20
 * 설명: 직원 정보 Service
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    /**
     * 모든 직원 목록 조회
     */
    public List<EmployeeDTO> getAllEmployees() {
        log.info("모든 직원 목록 조회");
        return employeeMapper.selectAllEmployees();
    }

    /**
     * 지점별 직원 목록 조회
     */
    public List<EmployeeDTO> getEmployeesByBranch(Integer branchId) {
        log.info("지점별 직원 조회 - branchId: {}", branchId);
        return employeeMapper.selectEmployeesByBranch(branchId);
    }

    /**
     * 직원 ID로 조회
     */
    public EmployeeDTO getEmployeeById(Integer empId) {
        log.info("직원 조회 - empId: {}", empId);
        return employeeMapper.selectEmployeeById(empId);
    }

    /**
     * 직원 코드로 조회
     */
    public EmployeeDTO getEmployeeByCode(String empCode) {
        log.info("직원 조회 - empCode: {}", empCode);
        return employeeMapper.selectEmployeeByCode(empCode);
    }

    /**
     * 직원 등록
     */
    public boolean createEmployee(EmployeeDTO employee) {
        log.info("직원 등록: {}", employee);
        int result = employeeMapper.insertEmployee(employee);
        return result > 0;
    }

    /**
     * 직원 수정
     */
    public boolean updateEmployee(EmployeeDTO employee) {
        log.info("직원 수정: {}", employee);
        int result = employeeMapper.updateEmployee(employee);
        return result > 0;
    }

    /**
     * 직원 삭제
     */
    public boolean deleteEmployee(Integer empId) {
        log.info("직원 삭제 - empId: {}", empId);
        int result = employeeMapper.deleteEmployee(empId);
        return result > 0;
    }
}