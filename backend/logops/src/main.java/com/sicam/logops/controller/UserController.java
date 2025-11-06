package com.sicam.logops.controller;

import com.sicam.logops.model.User;
import com.sicam.logops.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String name, @RequestParam String email, @RequestParam String password, @RequestParam List<String> roles) {
        User user = userService.createUser(name, email, password, roles);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestParam String name, @RequestParam String email) {
        User user = userService.updateUser(userId, name, email);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<User> assignRolesToUser(@PathVariable Long userId, @RequestParam List<String> roles) {
        User user = userService.assignRolesToUser(userId, roles);
        return ResponseEntity.ok(user);
    }
}
