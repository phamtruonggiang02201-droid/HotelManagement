package com.example.HM.service.impl;

import com.example.HM.entity.Account;
import com.example.HM.repository.AccountRepository;
import com.example.HM.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = "Khách hàng";
        if (account.getRole() != null) {
            roleName = account.getRole().getDescription() != null ? account.getRole().getDescription() : account.getRole().getRoleName();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + account.getRole().getRoleName()));
        }

        String fullName;
        if ((account.getFirstName() == null || account.getFirstName().isEmpty()) && 
            (account.getLastName() == null || account.getLastName().isEmpty())) {
            fullName = account.getUsername(); // Default to username if name is missing
        } else {
            fullName = (account.getFirstName() != null ? account.getFirstName() : "") + " " + 
                       (account.getLastName() != null ? account.getLastName() : "");
            fullName = fullName.trim();
        }

        return new CustomUserDetails(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                account.getEmailVerified() != null && account.getEmailVerified(),
                true,
                true,
                account.getStatus(),
                authorities,
                fullName,
                roleName,
                account.getAvatar()
        );
    }
}
