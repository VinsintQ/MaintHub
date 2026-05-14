package com.MaintHub.demo.controller;



import com.MaintHub.demo.dto.ChangePasswordRequest;
import com.MaintHub.demo.dto.LoginRequest;
import com.MaintHub.demo.dto.ResetPasswordRequest;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/auth/users")
public class AuthController {
    private UserService userService;

    @Autowired
    private void setUserService(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/register")
    public User createUser(@RequestBody User userObject){
        System.out.println("Calling create user");
        return userService.createUser(userObject);

    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        System.out.println("calling loginUser ----->");;
        return userService.loginUser(loginRequest);
    }


    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request){
        System.out.println("calling change password in controller ========>");
        try {
            userService.changePassword(request.getOldPass(), request.getNewPass());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/register/verify")
    public void validate(@RequestParam String token){
        System.out.println("calling get verify  ========>");
        userService.validate(token);
    }

    @GetMapping("/resetPassword")
    public ResponseEntity<String> passwordReset(@RequestBody ResetPasswordRequest emailAddress){
        System.out.println("calling reset Password from controller ========>");
        try {
            userService.resetPassword(emailAddress.getEmailAddress());
            return ResponseEntity.ok("Password reset email sent. Please check your inbox.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{userId}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> softDeleteUser(@PathVariable Long userId) {
        userService.softDeleteUser(userId);
        return ResponseEntity.ok("softDelete ");
    }

    @PutMapping("/{userId}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> promoteUserToAdmin(@PathVariable Long userId) {
        userService.promoteUserToAdmin(userId);
        return ResponseEntity.ok("User promoted to ADMIN");
    }



}
