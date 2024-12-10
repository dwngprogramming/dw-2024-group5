package com.nlu.app.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.ResourceBundle;

public class EmailService {

  private String host;
  private String port;
  private String username;
  private String password;

  // Constructor để khởi tạo cấu hình từ file config.properties
  public EmailService() {
    loadMailConfigFromProperties();
  }

  // Phương thức tải cấu hình mail từ file config.properties
  private void loadMailConfigFromProperties() {
    ResourceBundle bundle = ResourceBundle.getBundle(
        "config");  // Đảm bảo file config.properties ở đúng vị trí (src/main/resources)

    // Lấy thông tin từ file config.properties
    this.host = bundle.getString("mail.host");
    this.port = bundle.getString("spring.mail.port");
    this.username = bundle.getString("spring.mail.username");
    this.password = bundle.getString("spring.mail.password");
  }

  // Phương thức gửi email
  public void sendEmail(String subject, String body) {
    Properties properties = new Properties();
    properties.put("mail.smtp.host", host); // Host
    properties.put("mail.smtp.port", port); // Port
    properties.put("mail.smtp.auth", "true"); // Xác thực
    properties.put("mail.smtp.starttls.enable", "true"); // TLS
    properties.put("mail.smtp.connectiontimeout", "5000");
    properties.put("mail.smtp.timeout", "5000");
    properties.put("mail.smtp.writetimeout", "5000");

    // Tạo session
    Session session = Session.getInstance(properties, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    });

    try {
      // Tạo message
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(username));
      message.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse("luunguuenthanhvu123@gmail.com"));
      message.setSubject(subject);
      message.setText(body);

      // Gửi email
      Transport.send(message);
      System.out.println("Email đã được gửi thành công");
    } catch (MessagingException e) {
      e.printStackTrace();
      System.out.println("Gửi email thất bại: " + e.getMessage());
    }
  }
}
