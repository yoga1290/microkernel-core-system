package yoga1290.commons.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

@Configuration
public class EmailListenerConfig {

    @Getter
    @Value("${email-service.imap.enable:false}")
    private boolean enable;

    @Value("${email-service.imap.host:imap.gmail.com}")
    private String emailHost;

    @Value("${email-service.imap.port:993}")
    private String emailPort;

    @Value("${email-service.email:}")
    private String emailUsername;

    @Value("${email-service.password:}")
    private String emailPassword;

    @Bean
    public Session mailSession() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", emailHost);
        props.setProperty("mail.imaps.port", emailPort);

//        DEBUG IMAPS: mail.imap.fetchsize: 16384
//        DEBUG IMAPS: mail.imap.ignorebodystructuresize: false
//        DEBUG IMAPS: mail.imap.statuscachetimeout: 1000
//        DEBUG IMAPS: mail.imap.appendbuffersize: -1
//        DEBUG IMAPS: mail.imap.minidletime: 10

        props.setProperty("mail.imaps.minidletime", "1");

        // Create a new session with the properties
        Session session = Session.getInstance(props);
        session.setDebug(true); // Enable debug mode for troubleshooting

        return session;
    }

    public Store getStore() throws MessagingException {
        Store store = mailSession().getStore("imaps");
        store.connect(emailUsername, emailPassword);
        return store;
    }

}
