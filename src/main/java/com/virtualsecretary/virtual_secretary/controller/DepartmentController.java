package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.entity.Department;
import com.virtualsecretary.virtual_secretary.service.DepartmentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentController {
    DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<Department>> getAllDepartments() {
        return ApiResponse.<List<Department>>builder()
                .code(200)
                .result(departmentService.getAllDepartments())
                .build();
    }

    @GetMapping("/{departmentId}")
    public ApiResponse<Department> getDepartmentById(@PathVariable long departmentId) {
        return ApiResponse.<Department>builder()
                .code(200)
                .result(departmentService.getDepartmentById(departmentId))
                .build();
    }

    @PostMapping
    public ApiResponse<Department> createDepartment(@RequestBody Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return ApiResponse.<Department>builder()
                .code(200)
                .result(createdDepartment)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> updateDepartment(@PathVariable long id, @RequestBody Department updatedDepartment) {
        Department updated = departmentService.updateDepartment(id, updatedDepartment);
        return ApiResponse.<Department>builder()
                .code(200)
                .result(updated)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable long id) {
        departmentService.deleteDepartment(id);
        return ApiResponse.<Void>builder()
                .code(204).build();
    }
}


