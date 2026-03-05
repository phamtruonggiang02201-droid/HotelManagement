package com.example.HM.service.impl;

import com.example.HM.common.Constants;
import com.example.HM.dto.*;
import com.example.HM.entity.Account;
import com.example.HM.entity.Role;
import com.example.HM.repository.AccountRepository;
import com.example.HM.repository.RoleRepository;
import com.example.HM.service.AccountService;
import com.example.HM.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.HM.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.time.LocalDate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getEmployees(Pageable pageable) {
        List<String> employeeRoles = List.of("ADMIN", "MANAGER", "RECEPTION");
        return accountRepository.findAllByRole_RoleNameIn(employeeRoles, pageable)
                .map(this::convertToEmployeeDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountById(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        return convertToDTO(account);
    }

    @Override
    @Transactional
    public AccountDTO createAccountByAdmin(AdminAccountRequest request) {
        // Validation
        validateAdminAccountRequest(request, true);

        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());

        // Split full name
        String fullName = request.getFullName().trim();
        int firstSpaceIndex = fullName.indexOf(" ");
        if (firstSpaceIndex != -1) {
            account.setFirstName(fullName.substring(0, firstSpaceIndex));
            account.setLastName(fullName.substring(firstSpaceIndex + 1));
        } else {
            account.setFirstName(fullName);
            account.setLastName("");
        }

        account.setEmailVerified(true); // Admin created accounts are verified by default
        account.setStatus(true); 
        account.setJobTitle(request.getJobTitle());
        
        // Map personal info
        if (request.getPhone() != null) account.setPhone(request.getPhone());
        if (request.getDob() != null && !request.getDob().isEmpty()) {
            account.setDob(LocalDate.parse(request.getDob()));
        }
        if (request.getAddress() != null) account.setAddress(request.getAddress());
        if (request.getIdNumber() != null) account.setIdNumber(request.getIdNumber());
        if (request.getIdType() != null) account.setIdType(request.getIdType());
        if (request.getNationality() != null) account.setNationality(request.getNationality());

        // Set Role
        Role role = roleRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Quyền " + request.getRoleName() + " không tồn tại!"));
        account.setRole(role);

        // Generate Random Password
        String randomPassword = generateRandomPassword();
        account.setPassword(passwordEncoder.encode(randomPassword));

        Account saved = accountRepository.save(account);

        // Send Email to Employee
        emailService.sendEmployeeCredentialsEmail(
                saved.getEmail(), 
                saved.getUsername(), 
                randomPassword, 
                request.getFullName()
        );

        return convertToDTO(saved);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    @Override
    @Transactional
    public AccountDTO updateAccountByAdmin(String id, AdminAccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        // Validation
        validateAdminAccountRequest(request, false);
        
        // Custom check for email/username uniqueness if changed
        if (!account.getUsername().equals(request.getUsername()) && accountRepository.existsByUsername(request.getUsername())) {
             throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (!account.getEmail().equals(request.getEmail()) && accountRepository.existsByEmail(request.getEmail())) {
             throw new RuntimeException("Email đã được sử dụng!");
        }

        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());

        // Update name
        String fullName = request.getFullName().trim();
        int firstSpaceIndex = fullName.indexOf(" ");
        if (firstSpaceIndex != -1) {
            account.setFirstName(fullName.substring(0, firstSpaceIndex));
            account.setLastName(fullName.substring(firstSpaceIndex + 1));
        } else {
            account.setFirstName(fullName);
            account.setLastName("");
        }

        // Update role
        if (request.getRoleName() != null && !request.getRoleName().isEmpty()) {
            Role role = roleRepository.findByRoleName(request.getRoleName())
                    .orElseThrow(() -> new RuntimeException("Quyền " + request.getRoleName() + " không tồn tại!"));
            account.setRole(role);
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        account.setJobTitle(request.getJobTitle());

        // Update personal info
        if (request.getPhone() != null) account.setPhone(request.getPhone());
        if (request.getDob() != null && !request.getDob().isEmpty()) {
            account.setDob(LocalDate.parse(request.getDob()));
        }
        if (request.getAddress() != null) account.setAddress(request.getAddress());
        if (request.getIdNumber() != null) account.setIdNumber(request.getIdNumber());
        if (request.getIdType() != null) account.setIdType(request.getIdType());
        if (request.getNationality() != null) account.setNationality(request.getNationality());
        if (request.getStatus() != null) account.setStatus(request.getStatus());

        accountRepository.save(account);
        return convertToDTO(account);
    }

    @Override
    @Transactional
    public void deleteAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        accountRepository.delete(account);
    }

    @Override
    @Transactional
    public AccountDTO register(RegisterRequest request) {
        // 1. Validation
        validateRequest(request);

        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // 2. Create Account
        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEmail(request.getEmail());
        
        // Split full name into first/last name (User requested style)
        // Full Name: "Nguyễn Đức Minh" -> firstName: "Nguyễn", lastName: "Đức Minh"
        String fullName = request.getFullName().trim();
        int firstSpaceIndex = fullName.indexOf(" ");
        if (firstSpaceIndex != -1) {
            account.setFirstName(fullName.substring(0, firstSpaceIndex)); // Họ
            account.setLastName(fullName.substring(firstSpaceIndex + 1));  // Tên lót và tên gọi
        } else {
            account.setFirstName(fullName);
            account.setLastName("");
        }
        
        account.setEmailVerified(false);
        account.setVerificationToken(UUID.randomUUID().toString());
        account.setVerificationTokenExpiry(java.time.LocalDateTime.now().plusMinutes(10)); // Hết hạn nhanh sau 10 phút
        account.setStatus(false); // Mặc định KHÓA tài khoản cho đến khi verify email

        // 3. Set Role (Default: CUSTOMER)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Quyền CUSTOMER không tồn tại trong hệ thống!"));
        account.setRole(customerRole);

        Account saved = accountRepository.save(account);

        // 4. Send Verification Email
        String verifyUrl = "http://localhost:8080/verify-email?token=" + saved.getVerificationToken();
        emailService.sendRegistrationEmail(
                saved.getEmail(), 
                saved.getUsername(), 
                request.getFullName(),
                verifyUrl
        );

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        Account account = accountRepository.findByVerificationToken(token)
                .orElse(null);
        
        if (account == null) {
            return false;
        }

        // Kiểm tra hết hạn token
        if (account.getVerificationTokenExpiry() != null && 
            account.getVerificationTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            return false;
        }

        account.setEmailVerified(true);
        account.setStatus(true); // MỞ KHÓA tài khoản sau khi verify thành công
        account.setVerificationToken(null);
        account.setVerificationTokenExpiry(null);
        accountRepository.save(account);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO findByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại!"));
        return convertToDTO(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account findAccountByUsername(String username) {
        return findAccountEntity(username);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getCurrentProfile() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) return null;
        return findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getCurrentAccount() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) return null;
        return findAccountByUsername(username);
    }

    // Helper method to find entity internally
    private Account findAccountEntity(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại!"));
    }

    @Override
    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new RuntimeException("Bạn cần đăng nhập để thực hiện thao tác này!");
        
        Account account = findAccountEntity(username);
        
        if (request.getFullName() != null) {
            String trimmedFullName = request.getFullName().trim();
            if (!Pattern.matches(   Constants.REGEX_FULLNAME, trimmedFullName)) {
                throw new RuntimeException("Họ tên chỉ được chứa chữ cái và khoảng trắng (2-50 ký tự)!");
            }

            int firstSpaceIndex = trimmedFullName.indexOf(" ");
            if (firstSpaceIndex != -1) {
                account.setFirstName(trimmedFullName.substring(0, firstSpaceIndex));
                account.setLastName(trimmedFullName.substring(firstSpaceIndex + 1));
            } else {
                account.setFirstName(trimmedFullName);
                account.setLastName("");
            }
        }
        
        if (request.getIdNumber() != null) account.setIdNumber(request.getIdNumber());
        if (request.getIdType() != null) account.setIdType(request.getIdType());
        if (request.getNationality() != null) account.setNationality(request.getNationality());
        
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new RuntimeException("Bạn cần đăng nhập để thực hiện thao tác này!");

        Account account = findAccountEntity(username);
        
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }
        
        if (!Pattern.matches(Constants.REGEX_PASSWORD, request.getNewPassword())) {
            throw new RuntimeException("Mật khẩu mới không đúng định dạng (tối thiểu 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt)!");
        }
        
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void processForgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại trong hệ thống!"));

        String token = UUID.randomUUID().toString();
        account.setResetToken(token);
        account.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15)); // Hết hạn sau 15 phút
        accountRepository.save(account);

        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        String fullName = account.getFirstName() + " " + account.getLastName();
        emailService.sendForgotPasswordEmail(email, resetUrl, fullName.trim());
    }

    @Override
    @Transactional
    public void updateAvatar(String avatarUrl) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new RuntimeException("Bạn cần đăng nhập để thực hiện thao tác này!");
        
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        account.setAvatar(avatarUrl);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Account account = accountRepository.findByResetToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("Mã xác nhận không hợp lệ hoặc đã hết hạn!"));

        if (account.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn!");
        }

        if (!Pattern.matches(Constants.REGEX_PASSWORD, newPassword)) {
            throw new RuntimeException("Mật khẩu không đúng định dạng (tối thiểu 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số, 1 ký tự đặc biệt)!");
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        account.setResetToken(null);
        account.setResetTokenExpiry(null);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateStatus(String id, boolean status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
        account.setStatus(status);
        accountRepository.save(account);
    }

    private void validateRequest(RegisterRequest request) {
        if (!Pattern.matches(Constants.REGEX_FULLNAME, request.getFullName())) {
            throw new RuntimeException("Họ tên chỉ được chứa chữ cái và khoảng trắng (2-50 ký tự)!");
        }
        if (!Pattern.matches(Constants.REGEX_USERNAME, request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập từ 3-20 ký tự, chỉ chứa chữ cái, số, dấu chấm (.) hoặc gạch dưới (_)");
        }
        if (!Pattern.matches(Constants.REGEX_EMAIL, request.getEmail())) {
            throw new RuntimeException("Email không đúng định dạng!");
        }
        if (!Pattern.matches(Constants.REGEX_PASSWORD, request.getPassword())) {
            throw new RuntimeException("Mật khẩu tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt (@$!%*?&)");
        }
        if (request.getConfirmPassword() == null || !request.getConfirmPassword().equals(request.getPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }
    }

    private AccountDTO convertToDTO(Account account) {
        if (account == null) return null;
        return AccountDTO.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .fullName((account.getFirstName() != null ? account.getFirstName() : "") + " " + 
                         (account.getLastName() != null ? account.getLastName() : ""))
                .avatar(account.getAvatar())
                .roleName(account.getRole() != null ? account.getRole().getRoleName() : null)
                .roleDescription(account.getRole() != null ? account.getRole().getDescription() : null)
                .status(account.getStatus())
                .emailVerified(account.getEmailVerified())
                .idNumber(account.getIdNumber())
                .idType(account.getIdType())
                .nationality(account.getNationality())
                .createdAt(account.getCreatedAt())
                .jobTitle(account.getJobTitle())
                .build();
    }

    private EmployeeResponseDTO convertToEmployeeDTO(Account account) {
        if (account == null) return null;
        return EmployeeResponseDTO.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName((account.getFirstName() != null ? account.getFirstName() : "") + " " + 
                         (account.getLastName() != null ? account.getLastName() : ""))
                .phone(account.getPhone())
                .address(account.getAddress())
                .jobTitle(account.getJobTitle())
                .roleName(account.getRole() != null ? account.getRole().getRoleName() : null)
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    private void validateAdminAccountRequest(AdminAccountRequest request, boolean isCreate) {
        if (!Pattern.matches(Constants.REGEX_USERNAME, request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập không đúng định dạng!");
        }
        if (!Pattern.matches(Constants.REGEX_EMAIL, request.getEmail())) {
            throw new RuntimeException("Email không đúng định dạng!");
        }
        if (!Pattern.matches(Constants.REGEX_FULLNAME, request.getFullName())) {
            throw new RuntimeException("Họ tên không đúng định dạng!");
        }
        if (isCreate) {
            // Password is now generated automatically for admin-created accounts
            if (accountRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            }
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng!");
            }
        }
    }
}
