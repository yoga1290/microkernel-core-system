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
// use the true flag to indicate you need a multipart message
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(emailTo);

// use the true flag to indicate the text included is HTML
        helper.setText(htmlMsg, true);

        emailServiceConfig.getMailSender().send(message);


// let's include the infamous windows Sample file (this time copied to c:/)
//        FileSystemResource res = new FileSystemResource(new File("c:/Sample.jpg"));
//        helper.addInline("identifier1234", res);

        System.out.println("TODO: =============> " + emailServiceConfig.getEmail());
        System.out.println("TODO: =============> " + emailServiceConfig.getDisplayName());
//        MimeMessage mimeMessage = emailServiceConfig.getMailSender().createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-16");
//        //mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
//        helper.setText(htmlMsg, true); // Use this or above line.
//        helper.setTo(emailTo);
//        helper.setSubject(subject);
//        helper.setFrom(this.emailServiceConfig.getEmail(), this.emailServiceConfig.getDisplayName());
//        emailServiceConfig.getMailSender().send(mimeMessage);


//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//        simpleMailMessage.setText(htmlMsg);
//        simpleMailMessage.setTo(emailTo);
//        simpleMailMessage.setSubject(subject);
//        simpleMailMessage.setFrom(String.format("%s <%s>",
//                                    this.emailServiceConfig.getEmail(),
//                                    this.emailServiceConfig.getDisplayName()));
//        emailServiceConfig.getMailSender().send(simpleMailMessage);
    }

}
