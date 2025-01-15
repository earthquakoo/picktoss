package com.picktoss.picktossserver.domain.admin.service;

import com.picktoss.picktossserver.domain.admin.entity.Admin;
import com.picktoss.picktossserver.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCreateService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAdmin(String name, String rawPassword) {
        Admin admin = Admin.createAdmin(name, passwordEncoder.encode(rawPassword));

        adminRepository.save(admin);
    }
}
