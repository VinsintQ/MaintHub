package com.MaintHub.demo.dto.response;

import com.MaintHub.demo.model.User;
import lombok.Data;

import java.util.List;

@Data
public class UserSummaryResponse {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;

    public static UserSummaryResponse from(User user) {
        if (user == null) {
            return null;
        }
        UserSummaryResponse response = new UserSummaryResponse();
        response.setId(user.getId());
        response.setFullName(user.getUserName());
        response.setEmail(user.getEmailAddress());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList());
        return response;
    }
}
