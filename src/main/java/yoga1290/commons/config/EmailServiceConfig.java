package yoga1290.commons.config;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.server.Encoding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Slf4j
@ToString
@Configuration
public class EmailServiceConfig {

    @Getter
    private String email;
    @Getter
    private String displayName;
//    @Getter
    private JavaMailSender mailSender;
    private String password;

    public EmailServiceConfig(
                    @Value("${email-service.display-name:}")
                    String displayName,
                    @Value("${email-service.email:}")
                    String email,
                    @Value("${email-service.password:}")
                    String password) {

        this.email = email;
        this.password = password;
        this.displayName = displayName;

        boolean hasNoDisplayName = displayName == null || displayName.length() == 0;
        if (hasNoDisplayName) {
            this.displayName = this.email;
        }
    }

    @Bean
    @ConditionalOnProperty(
            value = "email-service.enable",
            havingValue = "true")
    public JavaMailSender getMailSender() {
        JavaMailSenderImpl mailSender = null;
        try {
            mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);

            mailSender.setUsername(this.email);
            mailSender.setPassword(this.password);
            mailSender.setDefaultEncoding("UTF-16");
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        } catch(Exception e) {
            e.printStackTrace();

            log.error("EmailServiceConfig", e.getMessage());
        }
        System.out.println("TODO ===========>" + mailSender.toString());
        return mailSender;
    }

}
