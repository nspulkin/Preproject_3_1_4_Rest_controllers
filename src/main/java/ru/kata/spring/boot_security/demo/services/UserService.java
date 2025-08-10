package ru.kata.spring.boot_security.demo.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService extends UserDetailsService {
    List<User> index();

    User show(int id);

    void save(User user);

    void update(int id, User updatedUser);

    void delete(int id);

    Optional<User> findByEmail(String email);

    // Методы для работы с текущим пользователем
    User getCurrentUser();

    // Методы для создания и управления пользователями
    User createUser(User user);

    User createUserWithRoles(String firstName, String lastName, String password, int age, String email, Set<String> roleNames);

    User updateUser(int id, String firstName, String lastName, String password, String email, int age, Set<Role> roles);

    User updateUserWithRoles(int id, String firstName, String lastName, String password, int age, String email, Set<String> roleNames);

    void deleteUser(int id);

    // Методы для работы с ролями
    Set<Role> processRoleNames(Set<String> roleNames);

    // Методы для REST API
    List<User> getAllUsersForApi();

    User getUserByIdForApi(int id);

    User createUserForApi(String firstName, String lastName, String password, int age, String email, Set<String> roleNames);

    User updateUserForApi(int id, String firstName, String lastName, String password, int age, String email, Set<String> roleNames);

    boolean deleteUserForApi(int id);

    List<Role> getAllRolesForApi();

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
