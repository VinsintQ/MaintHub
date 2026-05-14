package com.MaintHub.demo.service;



import com.MaintHub.demo.model.SecureToken;
import com.MaintHub.demo.repository.SecureTokenRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultSecureTokenService implements SecureTokenService{
    private static BytesKeyGenerator DEFAULT_TOKEN_GENERATOR= KeyGenerators.secureRandom(12);
    @Value("2800")
    private int tokenValidityInSeconds;

    @Autowired
    SecureTokenRepository secureTokenRepository;

    @Override
    public SecureToken createToken() {
        String tokenValue = new String(Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey()));
        SecureToken secureToken=new SecureToken();
        secureToken.setToken(tokenValue);
        secureToken.setExpireAt(LocalDateTime.now().plusSeconds(tokenValidityInSeconds));
        this.saveSecureToken(secureToken);
        return secureToken;

    }

    @Override
    public void saveSecureToken(SecureToken secureToken) {
        secureTokenRepository.save(secureToken);

    }

    @Override
    public SecureToken findByToken(String token) {
        return secureTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void removeToken(SecureToken token) {
        secureTokenRepository.removeByToken(token.getToken());
    }
}