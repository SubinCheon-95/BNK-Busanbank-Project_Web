package kr.co.busanbank.mapper;

import kr.co.busanbank.dto.EmployeeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 모든 직원 목록 조회 (재직 중인 직원만)
     */
    List<EmployeeDTO> selectAllEmployees();

    /**
     * 지점별 직원 목록 조회
     */
    List<EmployeeDTO> selectEmployeesByBranch(@Param("branchId") Integer branchId);

    /**
     * 직원 ID로 조회
     */
    EmployeeDTO selectEmployeeById(@Param("empId") Integer empId);

    /**
     * 직원 코드로 조회
     */
    EmployeeDTO selectEmployeeByCode(@Param("empCode") String empCode);

    /**
     * 직원 등록
     */
    int insertEmployee(EmployeeDTO employee);

    /**
     * 직원 수정
     */
    int updateEmployee(EmployeeDTO employee);

    /**
     * 직원 삭제 (soft delete)
     */
    int deleteEmployee(@Param("empId") Integer empId);
}