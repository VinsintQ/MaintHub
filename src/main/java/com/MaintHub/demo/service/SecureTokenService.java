package com.MaintHub.demo.service;


import com.MaintHub.demo.model.SecureToken;

public interface SecureTokenService {

    SecureToken createToken();
    void saveSecureToken(SecureToken secureToken);
    SecureToken findByToken(String token);
    void removeToken(SecureToken token);

}