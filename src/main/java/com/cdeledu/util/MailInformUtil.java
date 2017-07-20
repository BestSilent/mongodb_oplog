package com.cdeledu.util;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by dell on 2017/5/3.
 *
 * @author wql 发送邮件工具类
 */
public class MailInformUtil {

  private String USER;
  private String PASSWORD;
  private String[] EMAIL;
  private String SERVER;
  private String MAIL_SUBJECT;

  /**
   * 初始化
   */
  public MailInformUtil() {
    USER = PropertiesUtil.PropertyGetValue("sendmail.properties", "USER");
    PASSWORD = PropertiesUtil.PropertyGetValue("sendmail.properties", "PASSWORD");
    EMAIL = PropertiesUtil.PropertyGetValue("sendmail.properties", "EMAIL").split(",");
    SERVER = PropertiesUtil.PropertyGetValue("sendmail.properties", "SERVER");
    MAIL_SUBJECT = PropertiesUtil.PropertyGetValue("sendmail.properties", "MAIL_SUBJECT");
  }

  /**
   * @param content 邮件内容
   * @throws Exception 发送邮件方法
   */
  public boolean sendMail(String content) {
    boolean bool = false;
    try {
      final java.util.Properties props = new java.util.Properties();
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.host", SERVER);
      props.put("mail.user", USER);
      props.put("mail.password", PASSWORD);
      Authenticator authenticator = new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          String userName = props.getProperty("mail.user");
          String password = props.getProperty("mail.password");
          return new PasswordAuthentication(userName, password);
        }
      };
      Session session = Session.getDefaultInstance(props, authenticator);
      session.setDebug(true);
      Transport transport = session.getTransport("smtp");
      InternetAddress form = new InternetAddress(props.getProperty("mail.user"));

      MimeMessage message = new MimeMessage(session);
      message.setFrom(form);
      Address[] addressTO = new InternetAddress[EMAIL.length];
      for (int i = 0; i < EMAIL.length; i++) {
        addressTO[i] = new InternetAddress(EMAIL[i]);
      }
      message.setRecipients(Message.RecipientType.TO, addressTO);
      message.setSubject(MAIL_SUBJECT);
      message.setText(content.toString());
      transport.connect(SERVER, USER, PASSWORD);
      transport.sendMessage(message, addressTO);
      transport.close();
      bool = true;
    } catch (NoSuchProviderException e) {
      e.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    }
    return bool;
  }
}
