package com.sicam.logops.service;

import com.sicam.logops.model.Role;
import com.sicam.logops.model.User;
import com.sicam.logops.repository.RoleRepository;
import com.sicam.logops.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String name, String email, String password, List<String> roleNames) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("User with email " + email + " already exists.");
        }

        List<Role> roles = roleNames.stream()
                .map(roleRepository::findByName)
                .toList();

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .build();
        return userRepository.save(user);
    }

    public User updateUser(Long userId, String name, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public User assignRolesToUser(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Role> roles = roleNames.stream()
                .map(roleRepository::findByName)
                .toList();
        user.setRoles(roles);
        return userRepository.save(user);
    }
}
