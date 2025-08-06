package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.CreateUserDto;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class RestAdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RestAdminController(UserService userService, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            System.out.println("=== Getting all users ===");
            List<User> users = userService.index();
            System.out.println("Found " + users.size() + " users:");

            List<UserDto> userDtos = users.stream().map(UserDto::new).collect(Collectors.toList());

            for (UserDto userDto : userDtos) {
                System.out.println("User ID: " + userDto.getId() + ", Email: " + userDto.getEmail() + ", Name: " + userDto.getFirstName() + " " + userDto.getLastName() + ", Roles: " + userDto.getRoles());
            }
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            System.err.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable int id) {
        try {
            User user = userService.show(id);
            if (user != null) {
                return ResponseEntity.ok(new UserDto(user));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserDto createUserDto) {
        try {
            System.out.println("=== Creating new user ===");
            System.out.println("Email: " + createUserDto.getEmail());
            System.out.println("First Name: " + createUserDto.getFirstName());
            System.out.println("Last Name: " + createUserDto.getLastName());
            System.out.println("Age: " + createUserDto.getAge());
            System.out.println("Roles: " + createUserDto.getRoles());

            // Проверяем, что пользователь с таким email не существует
            if (userService.findByEmail(createUserDto.getEmail()).isPresent()) {
                System.out.println("User with email '" + createUserDto.getEmail() + "' already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Создаем нового пользователя
            User user = new User();
            user.setFirstName(createUserDto.getFirstName());
            user.setLastName(createUserDto.getLastName());
            user.setAge(createUserDto.getAge());
            user.setEmail(createUserDto.getEmail());

            // Шифруем пароль
            if (createUserDto.getPassword() != null && !createUserDto.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
                System.out.println("Password encoded successfully");
            } else {
                System.out.println("Password is null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Обрабатываем роли
            Set<Role> userRoles = new HashSet<>();
            if (createUserDto.getRoles() != null) {
                for (String roleName : createUserDto.getRoles()) {
                    if (roleName != null && !roleName.trim().isEmpty()) {
                        Role existingRole = roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
                        userRoles.add(existingRole);
                        System.out.println("Added role: " + roleName);
                    }
                }
            }

            // Если роли не выбраны, устанавливаем роль USER по умолчанию
            if (userRoles.isEmpty()) {
                Role defaultRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
                userRoles.add(defaultRole);
                System.out.println("Added default USER role");
            }

            user.setRoles(userRoles);
            userService.save(user);
            System.out.println("User saved successfully with ID: " + user.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new UserDto(user));
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable int id, @RequestBody CreateUserDto updateUserDto) {
        try {
            System.out.println("=== RestAdminController.updateUser called ===");
            System.out.println("User ID: " + id);
            System.out.println("Email: " + updateUserDto.getEmail());
            System.out.println("Password provided: " + (updateUserDto.getPassword() != null && !updateUserDto.getPassword().trim().isEmpty()));

            // Получаем существующего пользователя для проверки пароля
            User existingUser = userService.show(id);
            if (existingUser == null) {
                System.out.println("User not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            System.out.println("Existing user found: " + existingUser.getEmail());

            // Обрабатываем пароль - передаем новый пароль как есть, если он предоставлен
            String password = null; // По умолчанию не меняем пароль
            if (updateUserDto.getPassword() != null && !updateUserDto.getPassword().trim().isEmpty()) {
                password = updateUserDto.getPassword(); // Передаем незашифрованный пароль
                System.out.println("New password will be encoded");
            } else {
                System.out.println("No new password provided, keeping existing");
            }

            // Обрабатываем роли
            Set<Role> userRoles = new HashSet<>();
            if (updateUserDto.getRoles() != null) {
                for (String roleName : updateUserDto.getRoles()) {
                    if (roleName != null && !roleName.trim().isEmpty()) {
                        Role existingRole = roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
                        userRoles.add(existingRole);
                    }
                }
            }

            // Если роли не выбраны, устанавливаем роль USER по умолчанию
            if (userRoles.isEmpty()) {
                Role defaultRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
                userRoles.add(defaultRole);
            }

            System.out.println("Calling userService.updateUser");
            userService.updateUser(id, updateUserDto.getFirstName(), updateUserDto.getLastName(), password, updateUserDto.getEmail(), updateUserDto.getAge(), userRoles);

            User updatedUser = userService.show(id);
            System.out.println("User updated successfully");
            return ResponseEntity.ok(new UserDto(updatedUser));
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        try {
            List<Role> roles = roleRepository.findAll();
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/debug/users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        try {
            Map<String, Object> debugInfo = new HashMap<>();

            // Получаем всех пользователей напрямую из репозитория
            List<User> allUsers = userService.index();
            debugInfo.put("totalUsers", allUsers.size());

            List<Map<String, Object>> usersInfo = new ArrayList<>();
            for (User user : allUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("lastName", user.getLastName());
                userInfo.put("age", user.getAge());
                userInfo.put("rolesCount", user.getRoles().size());
                userInfo.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
                usersInfo.add(userInfo);
            }
            debugInfo.put("users", usersInfo);

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
        }
    }
}
