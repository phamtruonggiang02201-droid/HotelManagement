package com.example.HM.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final String id;
    private final String fullName;
    private final String roleName;
    private final String avatar;

    public CustomUserDetails(String id, String username, String password, boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities,
                             String fullName, String roleName, String avatar) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
        this.fullName = fullName;
        this.roleName = roleName;
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "CustomUserDetails [" +
                "Username=" + getUsername() +
                ", fullName=" + fullName +
                ", roleName=" + roleName +
                ", Enabled=" + isEnabled() +
                ", Authorities=" + getAuthorities() +
                "]";
    }
}
