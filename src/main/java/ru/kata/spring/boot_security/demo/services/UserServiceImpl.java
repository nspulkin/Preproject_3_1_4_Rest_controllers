package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(@Lazy UserRepository userRepository, @Lazy RegistrationService registrationService, @Lazy RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> index() {
        try {
            System.out.println("=== UserServiceImpl.index() called ===");
            List<User> users = userRepository.findAll();
            System.out.println("Found " + users.size() + " users from repository");

            // Загружаем роли для каждого пользователя, чтобы избежать проблем с ленивой загрузкой
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                System.out.println("Processing user " + i + ": " + user.getEmail());

                if (user.getRoles() != null) {
                    System.out.println("  User " + i + " roles before initialization: " + user.getRoles().size());
                    user.getRoles().size(); // Это загрузит роли
                    System.out.println("  User " + i + " roles after initialization: " + user.getRoles().size());

                    // Проверяем каждую роль
                    user.getRoles().forEach(role -> {
                        System.out.println("    Role: " + role.getName() + " (ID: " + role.getId() + ")");
                    });
                } else {
                    System.out.println("  User " + i + " has no roles");
                }
            }

            return users;
        } catch (Exception e) {
            System.err.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User show(int id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            System.out.println("=== UserServiceImpl.show() called for ID: " + id + " ===");

            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                User userEntity = user.get();
                System.out.println("Found user: " + userEntity.getEmail());

                if (userEntity.getRoles() != null) {
                    System.out.println("  User roles before initialization: " + userEntity.getRoles().size());
                    userEntity.getRoles().size(); // Это загрузит роли
                    System.out.println("  User roles after initialization: " + userEntity.getRoles().size());

                    // Проверяем каждую роль
                    userEntity.getRoles().forEach(role -> {
                        System.out.println("    Role: " + role.getName() + " (ID: " + role.getId() + ")");
                    });
                } else {
                    System.out.println("  User has no roles");
                }

                return userEntity;
            }
            System.out.println("No user found with ID: " + id);
            return null;
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        System.out.println("=== Saving user ===");
        System.out.println("User before save: ID=" + user.getId() + ", Email=" + user.getEmail());
        User savedUser = userRepository.save(user);
        System.out.println("User after save: ID=" + savedUser.getId() + ", Email=" + savedUser.getEmail());
    }

    @Override
    @Transactional
    public void update(int id, User updatedUser) {
        if (updatedUser == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user or ID");
        }
        updatedUser.setId(id);
        userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Теперь ищем только по email, так как убрали поле name
        System.out.println("=== UserServiceImpl.loadUserByUsername() called for username: " + username + " ===");

        Optional<User> user = userRepository.findByEmail(username);

        if (user.isEmpty()) {
            System.out.println("No user found with email: " + username);
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Инициализируем роли для избежания проблем с ленивой загрузкой
        User userEntity = user.get();
        System.out.println("Found user: " + userEntity.getEmail());

        if (userEntity.getRoles() != null) {
            System.out.println("  User roles before initialization: " + userEntity.getRoles().size());
            userEntity.getRoles().size(); // Это загрузит роли
            System.out.println("  User roles after initialization: " + userEntity.getRoles().size());

            // Проверяем каждую роль
            userEntity.getRoles().forEach(role -> {
                System.out.println("    Role: " + role.getName() + " (ID: " + role.getId() + ")");
            });
        } else {
            System.out.println("  User has no roles");
        }

        return userEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Getting current user for username: " + currentUsername);

            Optional<User> user = findByEmail(currentUsername);
            if (user.isPresent()) {
                User userEntity = user.get();
                System.out.println("Found current user: " + userEntity.getEmail());

                if (userEntity.getRoles() != null) {
                    System.out.println("  Current user roles before initialization: " + userEntity.getRoles().size());
                    userEntity.getRoles().size(); // Это загрузит роли
                    System.out.println("  Current user roles after initialization: " + userEntity.getRoles().size());

                    // Проверяем каждую роль
                    userEntity.getRoles().forEach(role -> {
                        System.out.println("    Role: " + role.getName() + " (ID: " + role.getId() + ")");
                    });
                } else {
                    System.out.println("  Current user has no roles");
                }

                return userEntity;
            }

            System.out.println("No current user found for: " + currentUsername);
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public User createUser(User user) {
        System.out.println("=== UserServiceImpl.createUser called ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));

        if (user == null) {
            System.err.println("User is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            Optional<User> existingUser = findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                System.err.println("User with email '" + user.getEmail() + "' already exists");
                throw new IllegalArgumentException("User already exists");
            }

            System.out.println("Calling registrationService.register");
            registrationService.register(user);
            System.out.println("User created successfully");
            return user;
        } catch (Exception e) {
            System.err.println("Exception in createUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public User createUserWithRoles(String firstName, String lastName, String password, int age, String email, Set<String> roleNames) {
        System.out.println("=== UserServiceImpl.createUserWithRoles called ===");

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Проверяем, что пользователь с таким email не существует
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User with email '" + email + "' already exists");
        }

        // Создаем нового пользователя
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);
        user.setPassword(password);

        // Обрабатываем роли
        Set<Role> userRoles = processRoleNames(roleNames);
        user.setRoles(userRoles);

        // Создаем пользователя через RegistrationService
        return createUser(user);
    }

    @Override
    @Transactional
    public User updateUser(int id, String firstName, String lastName, String password, String email, int age, Set<Role> roles) {
        System.out.println("=== UserServiceImpl.updateUser called ===");
        System.out.println("ID: " + id);
        System.out.println("Email: " + email);
        System.out.println("Password provided: " + (password != null && !password.trim().isEmpty()));

        if (id <= 0 || email == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        User existingUser = show(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        System.out.println("Existing user found: " + existingUser.getEmail());

        // Обновляем существующего пользователя вместо создания нового
        existingUser.setFirstName(firstName);
        existingUser.setLastName(lastName);
        existingUser.setEmail(email);
        existingUser.setAge(age);

        // Обрабатываем пароль - если новый пароль предоставлен, шифруем его
        if (password != null && !password.trim().isEmpty()) {
            System.out.println("Encoding new password");
            existingUser.setPassword(registrationService.encodePassword(password));
            System.out.println("Password encoded successfully");
        } else {
            System.out.println("No new password provided, keeping existing password");
        }
        // Если пароль не предоставлен, оставляем старый пароль

        // Устанавливаем роли
        if (roles != null && !roles.isEmpty()) {
            System.out.println("Setting " + roles.size() + " roles");
            existingUser.setRoles(roles);
        } else {
            System.out.println("No roles provided, keeping existing roles");
        }

        // Сохраняем обновленного пользователя
        System.out.println("Saving updated user");
        userRepository.save(existingUser);
        System.out.println("User updated successfully");
        return existingUser;
    }

    @Override
    @Transactional
    public User updateUserWithRoles(int id, String firstName, String lastName, String password, int age, String email, Set<String> roleNames) {
        System.out.println("=== UserServiceImpl.updateUserWithRoles called ===");

        if (id <= 0 || email == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        // Обрабатываем роли
        Set<Role> userRoles = processRoleNames(roleNames);

        // Вызываем основной метод обновления
        return updateUser(id, firstName, lastName, password, email, age, userRoles);
    }

    @Override
    @Transactional
    public void deleteUser(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        User existingUser = show(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        delete(id);
    }

    @Override
    @Transactional
    public Set<Role> processRoleNames(Set<String> roleNames) {
        Set<Role> userRoles = new HashSet<>();

        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
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

        return userRoles;
    }

    // Методы для REST API
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsersForApi() {
        try {
            System.out.println("=== Getting all users for API ===");
            List<User> users = index();
            System.out.println("Found " + users.size() + " users");
            return users;
        } catch (Exception e) {
            System.err.println("Error getting users for API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByIdForApi(int id) {
        try {
            System.out.println("=== Getting user by ID for API: " + id + " ===");
            User user = show(id);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + id);
            }
            return user;
        } catch (Exception e) {
            System.err.println("Error getting user by ID for API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public User createUserForApi(String firstName, String lastName, String password, int age, String email, Set<String> roleNames) {
        try {
            System.out.println("=== Creating user for API ===");
            System.out.println("Email: " + email);
            System.out.println("First Name: " + firstName);
            System.out.println("Last Name: " + lastName);
            System.out.println("Age: " + age);
            System.out.println("Roles: " + roleNames);

            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }

            return createUserWithRoles(firstName, lastName, password, age, email, roleNames);
        } catch (Exception e) {
            System.err.println("Error creating user for API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public User updateUserForApi(int id, String firstName, String lastName, String password, int age, String email, Set<String> roleNames) {
        try {
            System.out.println("=== Updating user for API ===");
            System.out.println("User ID: " + id);
            System.out.println("Email: " + email);
            System.out.println("Password provided: " + (password != null && !password.trim().isEmpty()));

            return updateUserWithRoles(id, firstName, lastName, password, age, email, roleNames);
        } catch (Exception e) {
            System.err.println("Error updating user for API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteUserForApi(int id) {
        try {
            System.out.println("=== Deleting user for API: " + id + " ===");
            deleteUser(id);
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting user for API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRolesForApi() {
        try {
            System.out.println("=== Getting all roles for API ===");
            List<Role> roles = roleRepository.findAll();
            System.out.println("Found " + roles.size() + " roles");
            return roles;
        } catch (Exception e) {
            System.err.println("Error getting roles for API: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}

