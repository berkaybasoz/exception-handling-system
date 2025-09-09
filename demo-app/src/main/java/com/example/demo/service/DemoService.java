package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DemoService {
    
    public void throwException(String type) {
        switch (type.toLowerCase()) {
            case "runtime":
                throw new RuntimeException("Bu bir test RuntimeException'dır");
            case "illegal-argument":
                throw new IllegalArgumentException("Geçersiz parametre gönderildi");
            case "null-pointer":
                String nullString = null;
                int length = nullString.length(); // NullPointerException
                break;
            case "array-index":
                int[] array = {1, 2, 3};
                int value = array[10]; // ArrayIndexOutOfBoundsException
                break;
            default:
                throw new UnsupportedOperationException("Desteklenmeyen exception tipi: " + type);
        }
    }
    
    public void processUser(Long id, Map<String, Object> userData) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID geçersiz: " + id);
        }
        
        if (userData == null || userData.isEmpty()) {
            throw new IllegalArgumentException("User data boş olamaz");
        }
        
        if (!userData.containsKey("name")) {
            throw new IllegalArgumentException("User data'da 'name' alanı eksik");
        }
        
        if (id == 999) {
            throw new RuntimeException("User ID 999 test hatası için kullanılır");
        }
        
        // Normal işlem simülasyonu
        System.out.println("User işlendi: ID=" + id + ", Data=" + userData);
    }
    
    public void simulateDatabaseError() {
        // Database connection error simülasyonu
        throw new RuntimeException("Veritabanı bağlantısı kurulamadı: Connection timeout after 30 seconds");
    }
    
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email adresi boş olamaz");
        }
        
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Geçersiz email formatı: " + email);
        }
        
        if (email.equals("test@banned.com")) {
            throw new SecurityException("Bu email adresi yasaklı: " + email);
        }
        
        // Normal validation simülasyonu
        System.out.println("Email geçerli: " + email);
    }
}