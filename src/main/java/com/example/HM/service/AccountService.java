package com.example.HM.service;

import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.ChangePasswordRequest;
import com.example.HM.dto.RegisterRequest;
import com.example.HM.dto.UpdateProfileRequest;
import com.example.HM.dto.AdminAccountRequest;
import com.example.HM.entity.Account;

import com.example.HM.dto.EmployeeResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {

    Page<AccountDTO> getAllAccounts(Pageable pageable);
    Page<EmployeeResponseDTO> getEmployees(Pageable pageable);
    AccountDTO getAccountById(String id);
    AccountDTO createAccountByAdmin(AdminAccountRequest request);
    AccountDTO updateAccountByAdmin(String id, AdminAccountRequest request);
    void deleteAccount(String id);
    AccountDTO register(RegisterRequest request);
    boolean verifyEmail(String token);
    AccountDTO findByUsername(String username);
    Account findAccountByUsername(String username);
    AccountDTO getCurrentProfile();
    Account getCurrentAccount();
    void updateProfile(UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);
    void processForgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void updateStatus(String id, boolean status);
    void updateAvatar(String avatarUrl);
}
