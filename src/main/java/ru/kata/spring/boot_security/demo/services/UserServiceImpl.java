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
        System.out.println("=== Getting all users from repository ===");
        List<User> users = userRepository.findAll();
        System.out.println("Repository returned " + users.size() + " users");

        // Принудительно загружаем роли для каждого пользователя
        for (User user : users) {
            user.getRoles().size(); // Это загрузит роли
            System.out.println("User from repository: ID=" + user.getId() + ", Email=" + user.getEmail() +
                    ", Roles=" + user.getRoles().size());
        }
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public User show(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.getRoles().size(); // Это загрузит роли
            return userEntity;
        }
        return null;
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
        Optional<User> user = userRepository.findByEmail(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Инициализируем роли для избежания проблем с ленивой загрузкой
        User userEntity = user.get();
        userEntity.getRoles().size(); // Это загрузит роли
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
                userEntity.getRoles().size(); // Это загрузит роли
                System.out.println("Found current user: " + userEntity.getEmail());
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
}

