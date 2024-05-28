package yoga1290.commons.services;

import lombok.Setter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yoga1290.commons.config.EmailServiceConfig;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
public class EmailService implements IService {

    private EmailServiceConfig emailServiceConfig;

    public EmailService(EmailServiceConfig emailServiceConfig) {
        this.emailServiceConfig = emailServiceConfig;
    }

    @Async
    public void sendHTMLEmail(String emailTo, String subject, String htmlMsg, Map contextMDC) throws MessagingException, UnsupportedEncodingException {
        MDC.setContextMap(contextMDC);
        MimeMessage mimeMessage = emailServiceConfig.getMailSender().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        //mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
        helper.setText(htmlMsg, true); // Use this or above line.
        helper.setTo(emailTo);
        helper.setSubject(subject);
        helper.setFrom(this.emailServiceConfig.getEmail(), this.emailServiceConfig.getDisplayName());
        emailServiceConfig.getMailSender().send(mimeMessage);
    }

}
