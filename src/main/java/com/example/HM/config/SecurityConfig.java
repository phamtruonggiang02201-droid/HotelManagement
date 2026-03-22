package com.example.HM.config;

import com.example.HM.security.CustomAuthenticationFailureHandler;
import com.example.HM.security.CustomAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationFailureHandler failureHandler,
                          CustomAuthenticationSuccessHandler successHandler) {
        this.failureHandler = failureHandler;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để đơn giản hóa việc gọi AJAX (Fetch API)
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/login", "/Login", "/register", "/Register", "/verify-email",
                                "/forgot-password", "/reset-password", "/api/forgot-password", "/api/reset-password",
                                "/services/quick-order", "/api/public/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/payment/**").permitAll()
                // Room & Service APIs
                .requestMatchers(HttpMethod.GET, "/api/rooms/**", "/api/services/**", "/api/categories/**", "/api/enumerations/**", "/api/areas/**").permitAll()
                .requestMatchers("/api/rooms/**", "/api/services/**", "/api/areas/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Profile & Account APIs
                .requestMatchers("/api/profile/**").authenticated()
                
                // Protected pages
                .requestMatchers("/dashboard/**", "/dashboard").authenticated()
                .requestMatchers("/bookings/**").authenticated()
                .requestMatchers("/management/accounts/**", "/management/api/accounts/**").hasRole("ADMIN")
                .requestMatchers("/reception/**").hasAnyRole("ADMIN", "MANAGER", "RECEPTION")
                .requestMatchers("/management/assignments/my-schedule", "/management/assignments/api/**").authenticated()
                .requestMatchers("/management/assignments/api/*/status").hasAnyRole("ADMIN", "MANAGER", "RECEPTION")
                .requestMatchers("/feedback/summary").hasAnyRole("ADMIN", "MANAGER", "RECEPTION")
                .requestMatchers("/feedback/api/issues/**", "/feedback/api/refunds/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/feedback/api/**").authenticated()
                .requestMatchers("/management/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_RECEPTION", "ROLE_CHEF", "ROLE_MASSAGE", "ROLE_HOUSEKEEPING")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
