package com.example.HM.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    /**
     * Mã hóa chuỗi đầu vào sang MD5.
     * @param password Mật khẩu thuần túy
     * @return Chuỗi băm MD5 (hex)
     */
    public static String hashMD5(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi thuật toán mã hóa MD5", e);
        }
    }

    /**
     * So sánh mật khẩu thuần với mật khẩu đã mã hóa.
     * @param rawPassword Mật khẩu chưa mã hóa
     * @param encodedPassword Mật khẩu đã mã hóa (MD5)
     * @return true nếu khớp
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        return hashMD5(rawPassword).equals(encodedPassword);
    }
}
