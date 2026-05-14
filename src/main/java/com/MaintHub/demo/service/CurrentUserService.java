package com.MaintHub.demo.service;

import com.MaintHub.demo.exception.UserNotFoundException;
import com.MaintHub.demo.model.RoleName;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAddress(email);
        if (user == null) {
            throw new UserNotFoundException("Authenticated user not found");
        }
        return user;
    }

    public boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    public boolean isAdmin(User user) {
        return hasRole(user, RoleName.ROLE_ADMIN);
    }
}
