package com.example.HM.service;

import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.RegisterRequest;
import com.example.HM.entity.Account;

public interface AccountService {
    AccountDTO register(RegisterRequest request);
    boolean verifyEmail(String token);
    AccountDTO findByUsername(String username);
    Account findAccountByUsername(String username);
    void updateProfile(String username, String fullName);
    void changePassword(String username, String oldPassword, String newPassword);
    void processForgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void updateAvatar(String username, String avatarUrl);
}
