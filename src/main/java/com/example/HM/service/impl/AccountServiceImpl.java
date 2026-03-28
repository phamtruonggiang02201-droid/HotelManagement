package com.example.HM.service.impl;

import com.example.HM.constant.Constants;
import com.example.HM.dto.AccountDTO;
import com.example.HM.dto.AdminAccountRequest;
import com.example.HM.dto.EmployeeResponseDTO;
import com.example.HM.dto.UserRegisterDTO;
import com.example.HM.dto.UpdateProfileRequest;
import com.example.HM.dto.ChangePasswordRequest;
import com.example.HM.entity.Account;
import com.example.HM.entity.Role;
import com.example.HM.entity.PasswordHistory;
import com.example.HM.repository.AccountRepository;
import com.example.HM.repository.PasswordHistoryRepository;
import com.example.HM.repository.RoleRepository;
import com.example.HM.security.SecurityUtils;
import com.example.HM.service.AccountService;
import com.example.HM.service.EmailService;
import com.example.HM.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountDTO> getAllAccounts(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return accountRepository.searchAllAccounts(search.trim(), pageable)
                    .map(this::convertToDTO);
        }
        return accountRepository.findAllByIsDeletedFalse(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> getEmployees(String search, Pageable pageable) {
        List<String> employeeRoles = List.of("ADMIN", "MANAGER", "RECEPTION");
        if (search != null && !search.trim().isEmpty()) {
            return accountRepository.searchEmployees(search.trim(), employeeRoles, pageable)
                    .map(this::convertToEmployeeDTO);
        }
        return accountRepository.findAllByRole_RoleNameInAndIsDeletedFalse(employeeRoles, pageable)
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
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!Pattern.matches(Constants.REGEX_PHONE, request.getPhone())) {
                throw new RuntimeException("Số điện thoại không đúng định dạng!");
            }
            if (accountRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác!");
            }
            account.setPhone(request.getPhone());
        }

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

        // Save to password history
        saveToPasswordHistory(saved, saved.getPassword());

        return convertToDTO(saved);
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        SecureRandom rnd = new SecureRandom();
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
            checkAndSavePasswordHistory(account, request.getPassword());
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        account.setJobTitle(request.getJobTitle());

        // Update personal info
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!Pattern.matches(Constants.REGEX_PHONE, request.getPhone())) {
                throw new RuntimeException("Số điện thoại không đúng định dạng!");
            }
            if (!request.getPhone().equals(account.getPhone()) && accountRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác!");
            }
            account.setPhone(request.getPhone());
        }

        if (request.getDob() != null && !request.getDob().isEmpty()) {
            account.setDob(LocalDate.parse(request.getDob()));
        }
        if (request.getAddress() != null) account.setAddress(request.getAddress());
        if (request.getIdNumber() != null) account.setIdNumber(request.getIdNumber());
        if (request.getIdType() != null) account.setIdType(request.getIdType());
        if (request.getNationality() != null) account.setNationality(request.getNationality());
        // Protection for root admin
        if (Constants.ROOT_ADMIN_USERNAME.equals(account.getUsername())) {
            if (request.getStatus() != null && !request.getStatus()) {
                throw new RuntimeException("Không thể vô hiệu hóa tài khoản root admin!");
            }
            if (!account.getRole().getRoleName().equals(request.getRoleName())) {
                throw new RuntimeException("Không thể thay đổi quyền của tài khoản root admin!");
            }
        }

        accountRepository.save(account);
        return convertToDTO(account);
    }


    @Override
    @Transactional
    public void deleteAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));

        if (Constants.ROOT_ADMIN_USERNAME.equals(account.getUsername())) {
            throw new RuntimeException("Không thể xóa tài khoản root admin!");
        }

        account.setIsDeleted(true);
        accountRepository.save(account);
    }


    @Override
    @Transactional
    public AccountDTO register(UserRegisterDTO request) {
        // 1. Validation
        validateRequest(request);

        if (accountRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (accountRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác!");
            }
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
        account.setPhone(request.getPhone());

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

        // Save initial password to history
        saveToPasswordHistory(saved, saved.getPassword());

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
            if (!Pattern.matches(Constants.REGEX_FULLNAME, trimmedFullName)) {
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

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (!Pattern.matches(Constants.REGEX_PHONE, request.getPhone())) {
                throw new RuntimeException("Số điện thoại không đúng định dạng!");
            }
            if (!request.getPhone().equals(account.getPhone()) && accountRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Số điện thoại đã được sử dụng bởi tài khoản khác!");
            }
            account.setPhone(request.getPhone());
        }

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

        checkAndSavePasswordHistory(account, request.getNewPassword());
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

        checkAndSavePasswordHistory(account, newPassword);
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

        if (Constants.ROOT_ADMIN_USERNAME.equals(account.getUsername()) && !status) {
            throw new RuntimeException("Không thể vô hiệu hóa tài khoản root admin!");
        }

        account.setStatus(status);
        accountRepository.save(account);
    }

    private void validateRequest(UserRegisterDTO request) {
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

    @Override
    public ByteArrayInputStream exportAccountsToExcel() {
        List<Account> accounts = accountRepository.findAll()
                .stream()
                .filter(acc -> Boolean.FALSE.equals(acc.getIsDeleted())) // ✅ chỉ lấy chưa bị xóa
                .collect(java.util.stream.Collectors.toList());
        String[] headers = { "ID", "Username", "Email", "Full Name", "Phone", "Role", "Status" };

        return ExcelHelper.dataToExcel(accounts, "Accounts", headers, (row, acc) -> {
            row.createCell(0).setCellValue(acc.getId());
            row.createCell(1).setCellValue(acc.getUsername());
            row.createCell(2).setCellValue(acc.getEmail());
            row.createCell(3).setCellValue(acc.getFullName());
            row.createCell(4).setCellValue(acc.getPhone() != null ? acc.getPhone() : "");
            row.createCell(5).setCellValue(acc.getRole() != null ? acc.getRole().getRoleName() : "N/A");
            row.createCell(6).setCellValue(acc.getStatus());
        });
    }

    @Override
    @Transactional
    public String importAccountsFromExcel(MultipartFile file) {
        try {
            List<Account> allAccounts = ExcelHelper.excelToData(file.getInputStream(), "Accounts", row -> {
                String username = ExcelHelper.getCellValueAsString(row.getCell(1));
                if (username == null || username.trim().isEmpty()) return null;

                Account acc = new Account();
                acc.setUsername(username.trim());
                acc.setEmail(ExcelHelper.getCellValueAsString(row.getCell(2)));
                String fullName = ExcelHelper.getCellValueAsString(row.getCell(3));
                if (fullName != null && !fullName.isEmpty()) {
                    String[] parts = fullName.trim().split("\\s+", 2);
                    if (parts.length > 1) {
                        acc.setFirstName(parts[0]);
                        acc.setLastName(parts[1]);
                    } else {
                        acc.setFirstName("");
                        acc.setLastName(parts[0]);
                    }
                }
                acc.setPhone(ExcelHelper.getCellValueAsString(row.getCell(4)));

                // Default values
                acc.setPassword(passwordEncoder.encode("123456aA@"));
                acc.setStatus(true);
                acc.setEmailVerified(true);

                String roleName = ExcelHelper.getCellValueAsString(row.getCell(5));
                Role role = roleRepository.findByRoleName(roleName.isEmpty() ? "ROLE_USER" : roleName).orElse(null);
                acc.setRole(role);

                return acc;
            });

            int totalRows = allAccounts.size();
            List<Account> toSave = new ArrayList<>();
            int duplicateCount = 0;

            for (Account acc : allAccounts) {
                boolean exists = accountRepository.existsByUsername(acc.getUsername()) ||
                        (acc.getEmail() != null && !acc.getEmail().isEmpty() && accountRepository.existsByEmail(acc.getEmail())) ||
                        (acc.getPhone() != null && !acc.getPhone().isEmpty() && accountRepository.existsByPhone(acc.getPhone()));

                if (exists) {
                    duplicateCount++;
                } else {
                    toSave.add(acc);
                }
            }

            accountRepository.saveAll(toSave);

            return String.format("Nhập dữ liệu thành công! Đã thêm %d tài khoản mới. Bỏ qua %d tài khoản do trùng dữ liệu (username/email/phone).",
                    toSave.size(), duplicateCount);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
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
                .phone(account.getPhone())
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

    private void checkAndSavePasswordHistory(Account account, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByAccountOrderByCreatedAtDesc(account);

        for (PasswordHistory ph : history) {
            if (passwordEncoder.matches(newPassword, ph.getPassword())) {
                String formattedDate = ph.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                throw new RuntimeException("Mật khẩu này bạn đã sử dụng vào ngày " + formattedDate + ". Vui lòng chọn mật khẩu khác!");
            }
        }

        saveToPasswordHistory(account, passwordEncoder.encode(newPassword));

        // Keep only the latest MAX_PASSWORD_HISTORY entries
        if (history.size() >= Constants.MAX_PASSWORD_HISTORY) {
            List<PasswordHistory> toDelete = history.subList(Constants.MAX_PASSWORD_HISTORY - 1, history.size());
            passwordHistoryRepository.deleteAll(toDelete);
        }
    }

    private void saveToPasswordHistory(Account account, String encodedPassword) {
        PasswordHistory ph = new PasswordHistory();
        ph.setAccount(account);
        ph.setPassword(encodedPassword);
        passwordHistoryRepository.save(ph);
    }
}

