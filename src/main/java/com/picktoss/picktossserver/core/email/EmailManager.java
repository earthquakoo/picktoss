package com.picktoss.picktossserver.core.email;

public interface EmailManager {
    public void sendEmail(String recipient, String subject, String content);
}
