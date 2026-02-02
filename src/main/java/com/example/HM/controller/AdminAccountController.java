package com.example.HM.controller;

import com.example.HM.entity.Account;
import com.example.HM.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountController {

    @Autowired
    private AccountService accountService;

    /**
     * View danh sách tài khoản
     */
    @GetMapping
    public String viewAccountList(Model model) {
        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "admin/account-list";
    }
}
