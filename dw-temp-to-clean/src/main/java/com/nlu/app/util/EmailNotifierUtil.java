package com.nlu.app.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.ResourceBundle;

public class EmailNotifierUtil {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("config");

    public static void sendEmail(String[] recipients, String subject, String body) {
        // Cấu hình thông tin email server
        String host = BUNDLE.getString("email.host");
        String port = BUNDLE.getString("email.port");
        String username = BUNDLE.getString("email.username");
        String password = BUNDLE.getString("email.password");

        // Thiết lập properties cho JavaMail
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        // Tạo phiên làm việc
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Tạo email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            // Thêm danh sách người nhận
            for (String recipient : recipients) {
                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            }
            message.setSubject(subject);
            message.setText(body);

            // Gửi email
            Transport.send(message);
            System.out.println("Email sent successfully.");
        } catch (MessagingException e) {
            System.out.println("Failed to send email.");
            e.printStackTrace();
        }
    }
}
