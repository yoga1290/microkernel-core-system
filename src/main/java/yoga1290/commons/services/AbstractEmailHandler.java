package yoga1290.commons.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.Async;

import javax.mail.Message;

public abstract class AbstractEmailHandler {

    @Getter @Setter
    private String senderPattern;
    @Getter @Setter
    private String recieverPattern;
    @Getter @Setter
    private String topicPattern;

    @Async
    public void filterIncomingMessage(Message message) {

        this.handleMessage(message);
    }

    public abstract void handleMessage(Message message);

}
