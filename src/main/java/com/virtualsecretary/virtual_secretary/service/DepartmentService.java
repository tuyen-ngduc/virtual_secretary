package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.entity.Department;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.DepartmentRepository;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import com.virtualsecretary.virtual_secretary.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class DepartmentService {
    DepartmentRepository departmentRepository;
    UserRepository userRepository;
    MeetingRepository meetingRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")

    public Department getDepartmentById(long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByDepartmentCode(department.getDepartmentCode())) {
            throw new IndicateException(ErrorCode.DEPARTMENT_CODE_ALREADY_EXISTS);
        }
        return departmentRepository.save(department);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Department updateDepartment(long id, Department updatedDepartment) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        if (!existingDepartment.getDepartmentCode().equals(updatedDepartment.getDepartmentCode()) &&
                departmentRepository.existsByDepartmentCode(updatedDepartment.getDepartmentCode())) {
            throw new IndicateException(ErrorCode.DEPARTMENT_CODE_ALREADY_EXISTS);
        }

        existingDepartment.setDepartmentCode(updatedDepartment.getDepartmentCode());
        existingDepartment.setName(updatedDepartment.getName());

        return departmentRepository.save(existingDepartment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteDepartment(long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IndicateException(ErrorCode.DEPARTMENT_NOT_EXISTED));

        userRepository.updateDepartmentInUserToNull(id);
        meetingRepository.updateDepartmentInMeetingToNull(id);
        departmentRepository.deleteById(id);
    }
}
