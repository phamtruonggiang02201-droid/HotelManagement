package com.example.HM.controller;

import com.example.HM.dto.AccountDTO;
import com.example.HM.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountController {

    @Autowired
    private AccountService accountService;

    /**
     * View danh sách tài khoản
     */
    @GetMapping
    public String viewAccountList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Page<AccountDTO> accounts = accountService.getAllAccounts(search, PageRequest.of(page, size));
        model.addAttribute("accounts", accounts.getContent());
        model.addAttribute("accountPage", accounts);
        model.addAttribute("search", search);
        return "admin/account-list";
    }
}
