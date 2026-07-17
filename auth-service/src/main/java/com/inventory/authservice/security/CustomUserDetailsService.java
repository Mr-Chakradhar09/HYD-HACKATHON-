package com.inventory.authservice.security;

import com.inventory.authservice.entity.User;
import com.inventory.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;


    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



    @Override
    public UserDetails loadUserByUsername(String employeeCode) throws UsernameNotFoundException {
        User user = userRepository.findByEmployee_EmployeeCode(employeeCode).orElseThrow(() -> new UsernameNotFoundException("Employee not found"));
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmployee().getEmployeeCode())
                .password(user.getPassword())
                .authorities(
                        user.getEmployee()
                                .getRoles()
                                .stream()
                                .map(role ->
                                        "ROLE_" +
                                                role.getRoleName().name())
                                .toArray(String[]::new)
                )
                .accountLocked(false)
                .disabled(!user.getEnabled())
                .build();
    }
}