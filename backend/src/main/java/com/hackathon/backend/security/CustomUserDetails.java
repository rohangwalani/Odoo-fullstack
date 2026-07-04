package com.hackathon.backend.security;

import com.hackathon.backend.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private Long id;
    private String loginId;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Long companyId;

    public CustomUserDetails(Long id, String loginId, String email, String password,
                             Collection<? extends GrantedAuthority> authorities, Long companyId) {
        this.id = id;
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.companyId = companyId;
    }

    public static CustomUserDetails build(Employee employee) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + employee.getRole().name());

        return new CustomUserDetails(
                employee.getId(),
                employee.getLoginId(),
                employee.getEmail(),
                employee.getPassword(),
                Collections.singletonList(authority),
                employee.getCompany().getId()
        );
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public Long getCompanyId() { return companyId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
