package yoga1290.commons.services;

import org.slf4j.MDC;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yoga1290.commons.config.EmailServiceConfig;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService implements IService {

    private EmailServiceConfig emailServiceConfig;

    public EmailService(EmailServiceConfig emailServiceConfig) {
        this.emailServiceConfig = emailServiceConfig;
    }

    @Async
    public void sendHTMLEmail(String emailTo, String subject, String htmlMsg, Map contextMDC) throws MessagingException {
        MDC.setContextMap(contextMDC);

        MimeMessage message = emailServiceConfig.getMailSender().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(emailTo);
        helper.setSubject(subject);
        helper.setText(htmlMsg, true);
        emailServiceConfig.getMailSender().send(message);
    }

}
