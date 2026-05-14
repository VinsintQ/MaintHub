package com.MaintHub.demo.service;



import com.MaintHub.demo.dto.LoginRequest;
import com.MaintHub.demo.dto.LoginResponse;
import com.MaintHub.demo.exception.InformationExistException;
import com.MaintHub.demo.exception.InformationNotFoundException;
import com.MaintHub.demo.mailing.AccountPasswordResetEmailContext;
import com.MaintHub.demo.mailing.AccountVerificationEmailContext;
import com.MaintHub.demo.mailing.EmailService;
import com.MaintHub.demo.model.Role;
import com.MaintHub.demo.model.RoleName;
import com.MaintHub.demo.model.SecureToken;
import com.MaintHub.demo.model.User;
import com.MaintHub.demo.repository.RoleRepository;
import com.MaintHub.demo.repository.UserRepository;
import com.MaintHub.demo.security.JWTUtils;
import com.MaintHub.demo.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
@Service
public class UserService {
    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private MyUserDetails myUserDetails;

    @Autowired
    EmailService emailService;

    @Autowired
    private SecureTokenService secureTokenService;


    private UserService(UserRepository userRepository,RoleRepository roleRepository, @Lazy PasswordEncoder passwordEncoder,JWTUtils jwtUtils,
                        @Lazy AuthenticationManager authenticationManager,
                        @Lazy MyUserDetails myUserDetails){
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
        this.jwtUtils =jwtUtils;
        this.authenticationManager=authenticationManager;
        this.myUserDetails = myUserDetails;
        this.roleRepository=roleRepository;

    }

    public User createUser(User userObject){
        System.out.println("service calling create user");
        if (!userRepository.existsByEmailAddress(userObject.getEmailAddress())){
            userObject.setPassword(passwordEncoder.encode(userObject.getPassword()));

            Role userRole = roleRepository.findByName(RoleName.ROLE_STAFF)
                    .or(() -> roleRepository.findByName(RoleName.ROLE_USER))
                    .orElseThrow(() -> new RuntimeException("ROLE_STAFF does not exist"));
            userObject.getRoles().add(userRole);
            User result = userRepository.save(userObject);
            sendConfirmationEmail(userObject);
            return result;
        }else {
            throw new InformationExistException("User already exist");
        }
    }


    public void sendConfirmationEmail(User user) {
        SecureToken secureToken = secureTokenService.createToken();
        secureToken.setUser(user);
        secureTokenService.saveSecureToken(secureToken);
        AccountVerificationEmailContext context = new AccountVerificationEmailContext();
        context.init(user);
        context.setToken(secureToken.getToken());
        context.buildVerificationUrl("http://localhost:8080/", secureToken.getToken());

        System.out.println("sending email to " + user.getEmailAddress());
        emailService.sendMail(context);
    }
    public User findUserByEmailAddress(String email){
        return userRepository.findUserByEmailAddress(email);
    }

    public ResponseEntity<?> loginUser(LoginRequest loginRequest){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),loginRequest.getPassword()
        );
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            myUserDetails =(MyUserDetails) authentication.getPrincipal();
            if (!myUserDetails.isActive()) {
                throw new RuntimeException("Account is deactivated");
            }
            final String JWT = jwtUtils.generateJwtToken(myUserDetails);
            return ResponseEntity.ok(new LoginResponse(JWT));
        } catch (DisabledException e) {
            return ResponseEntity.ok(new LoginResponse("Error : Account is not verified. Please check your email."));
        } catch (Exception e) {
            return ResponseEntity.ok(new LoginResponse("Error : Username or password is incorrect"));
        }
    }


    public void changePassword(String oldPassword, String newPassword) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailAddress(email);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void validate(String token) {
        SecureToken secureToken = secureTokenService.findByToken(token);
        User user = secureToken.getUser();
        user.setAccountVerified(true);
        userRepository.save(user);
    }

    public void resetPassword(String emailAddress) {
        SecureToken secureToken = secureTokenService.createToken();
        User user = userRepository.findUserByEmailAddress(emailAddress);
        System.out.println("service found user ====> " + user.getUserName());
        secureToken.setUser(user);
        secureTokenService.saveSecureToken(secureToken);
        AccountPasswordResetEmailContext context = new AccountPasswordResetEmailContext();
        context.init(user);
        context.setToken(secureToken.getToken());
        context.buildResetUrl("http://localhost:8080/", secureToken.getToken());

        System.out.println("sending email to " + user.getEmailAddress());
        emailService.sendMail(context);
    }

    public void resetPasswordActivator(String token, String newPassword) {
        SecureToken secureToken = secureTokenService.findByToken(token);
        if (secureToken == null) {
            throw new RuntimeException("Invalid or expired token");
        }
        User user = secureToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        try {
            secureTokenService.removeToken(secureToken);
        } catch (Exception e) {
            System.out.println("Token removal failed (non-critical): " + e.getMessage());
        }
    }

    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InformationNotFoundException("User not found"));

        user.setActive(false);
        userRepository.save(user);
    }

    public void promoteUserToAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));


        boolean alreadyAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN);

        if (alreadyAdmin) {
            throw new RuntimeException("User is already an ADMIN");
        }

        user.getRoles().add(adminRole);
        userRepository.save(user);
    }

}
