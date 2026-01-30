/**
 * App: Picture Model
 * Package: com.picturemodel.infrastructure.security
 * File: CredentialEncryptionService.java
 * Version: 0.1.0
 * Turns: 5
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-01-30T02:03:52Z
 * Exports: CredentialEncryptionService
 * Description: class CredentialEncryptionService for CredentialEncryptionService responsibilities. Methods: CredentialEncryptionService - constructor; encrypt - encrypt; decrypt - decrypt.
 */

package com.picturemodel.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Service;

/**
 * Service for encrypting and decrypting credentials using Jasypt.
 *
 * @author Claude (AI Coding Agent)
 */
@Service
@Slf4j
public class CredentialEncryptionService {

    private final StringEncryptor encryptor;

    public CredentialEncryptionService(StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    /**
     * Encrypt a plain text credentials string.
     *
     * @param plainText the plain text to encrypt
     * @return encrypted string
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            return encryptor.encrypt(plainText);
        } catch (Exception e) {
            log.error("Error encrypting credentials", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt an encrypted credentials string.
     *
     * @param encryptedText the encrypted text to decrypt
     * @return plain text string
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            return encryptor.decrypt(encryptedText);
        } catch (Exception e) {
            log.error("Error decrypting credentials", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
