package com.hackathon.backend.security;

import com.hackathon.backend.model.Employee;
import com.hackathon.backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find employee by email, if not found try by loginId (allows logging in with either)
        Employee employee = employeeRepository.findByEmail(username)
                .orElseGet(() -> employeeRepository.findByLoginId(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username)));

        return CustomUserDetails.build(employee);
    }
}
