package com.example.HM.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.HM.service.EmployeeService;
import com.example.HM.dto.AccountDTO;
import com.example.HM.entity.Account;
import com.example.HM.entity.Role;
import com.example.HM.repository.AccountRepository;
import com.example.HM.repository.RoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<AccountDTO> getAllEmployees() {
        return accountRepository.findByRole_RoleName("STAFF")
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO createEmployee(AccountDTO request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Role role = roleRepository.findByRoleName("STAFF")
                .orElseThrow(() -> new RuntimeException("Role STAFF not found"));

        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setPassword(request.getPassword()); // nhớ encode nếu có Security
        account.setStatus(true);
        account.setRole(role);

        Account saved = accountRepository.save(account);

        return mapToDTO(saved);
    }

    private AccountDTO mapToDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .fullName(account.getFirstName() + " " + account.getLastName())
                .roleName(account.getRole().getRoleName())
                .status(account.getStatus())
                .build();
    }
}
