package com.example.HM.service;

import com.example.HM.dto.AccountDTO;
import java.util.List;

public interface EmployeeService {

    List<AccountDTO> getAllEmployees();

    AccountDTO createEmployee(AccountDTO request);

}
