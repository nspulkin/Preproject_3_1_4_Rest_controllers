package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class RestUserController {

    private final UserService userService;

    @Autowired
    public RestUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser() {
        try {
            System.out.println("=== RestUserController.getCurrentUser called ===");
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                System.out.println("Current user found: " + currentUser.getEmail());
                System.out.println("Roles count: " + (currentUser.getRoles() != null ? currentUser.getRoles().size() : "null"));
                if (currentUser.getRoles() != null) {
                    currentUser.getRoles().forEach(role -> System.out.println("  Role: " + role.getName()));
                }
                return ResponseEntity.ok(currentUser);
            } else {
                System.out.println("No current user found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        try {
            User user = userService.getUserByIdForApi(id);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
