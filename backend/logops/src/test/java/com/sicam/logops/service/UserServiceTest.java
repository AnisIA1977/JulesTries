package com.sicam.logops.service;

import com.sicam.logops.model.Role;
import com.sicam.logops.model.User;
import com.sicam.logops.repository.RoleRepository;
import com.sicam.logops.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sicam.logops.config.SecurityConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {SecurityConfig.class})
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");
    }

    @Test
    void testCreateUser() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(null);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(role);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User createdUser = userService.createUser("John Doe", "john.doe@example.com", "password", Collections.singletonList("ROLE_USER"));

        assertEquals("John Doe", createdUser.getName());
        assertEquals(1, createdUser.getRoles().size());
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(user);

        assertThrows(RuntimeException.class, () -> {
            userService.createUser("John Doe", "john.doe@example.com", "password", Collections.singletonList("ROLE_USER"));
        });
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updatedUser = userService.updateUser(1L, "John Smith", "john.smith@example.com");

        assertEquals("John Smith", updatedUser.getName());
        assertEquals("john.smith@example.com", updatedUser.getEmail());
    }

    @Test
    void testAssignRolesToUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(new Role(2L, "ROLE_ADMIN", Collections.emptyList()));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updatedUser = userService.assignRolesToUser(1L, Collections.singletonList("ROLE_ADMIN"));

        assertEquals(1, updatedUser.getRoles().size());
        assertEquals("ROLE_ADMIN", updatedUser.getRoles().iterator().next().getName());
    }
}
