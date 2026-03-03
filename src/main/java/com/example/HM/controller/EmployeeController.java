package com.example.HM.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.HM.service.EmployeeService;
import com.example.HM.dto.AccountDTO;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/management")
public class EmployeeController {

    private final EmployeeService employeeService;

    // View list page
    @GetMapping("/employee")
    public String employeeIndex() {
        return "management/employee/index";
    }

    // View create page
    @GetMapping("/employee/new")
    public String newEmployeeForm() {
        return "management/employee/detail";
    }

    // API get list
    @GetMapping("/api/employee")
    @ResponseBody
    public List<AccountDTO> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // API create
    @PostMapping("/api/employee")
    @ResponseBody
    public ResponseEntity<?> createEmployee(@RequestBody AccountDTO request) {
        try {
            AccountDTO employee = employeeService.createEmployee(request);
            return ResponseEntity.ok(
                    Map.of("message", "Employee created successfully.",
                            "employee", employee)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
