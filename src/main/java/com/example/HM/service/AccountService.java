package com.example.HM.service;

import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.AdminAccountRequest;
import com.example.HM.dto.EmployeeResponseDTO;
import com.example.HM.dto.UserRegisterDTO;
import com.example.HM.dto.UpdateProfileRequest;
import com.example.HM.dto.ChangePasswordRequest;
import com.example.HM.entity.Account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface AccountService {

    Page<AccountDTO> getAllAccounts(String search, Pageable pageable);
    Page<EmployeeResponseDTO> getEmployees(String search, Pageable pageable);
    AccountDTO getAccountById(String id);
    AccountDTO createAccountByAdmin(AdminAccountRequest request);
    AccountDTO updateAccountByAdmin(String id, AdminAccountRequest request);
    void deleteAccount(String id);
    AccountDTO register(UserRegisterDTO request);
    boolean verifyEmail(String token);
    AccountDTO findByUsername(String username);
    Account findAccountByUsername(String username);

    // Excel Operations
    ByteArrayInputStream exportAccountsToExcel();
    void importAccountsFromExcel(MultipartFile file);

    AccountDTO getCurrentProfile();
    Account getCurrentAccount();
    void updateProfile(UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);
    void processForgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void updateStatus(String id, boolean status);
    void updateAvatar(String avatarUrl);
}
