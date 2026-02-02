package com.example.HM.service;

import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.ChangePasswordRequest;
import com.example.HM.dto.RegisterRequest;
import com.example.HM.dto.UpdateProfileRequest;
import com.example.HM.entity.Account;

import java.util.List;

public interface AccountService {

    List<AccountDTO> getAllAccounts();
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
    void updateAvatar(String avatarUrl);
}
