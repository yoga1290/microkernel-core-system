package yoga1290.coresystem.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yoga1290.coresystem.config.EmailListenerConfig;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailListenerService implements IService {

    private List<AbstractEmailHandler> handlerList;
    private EmailListenerConfig emailListenerConfig;

    public EmailListenerService(EmailListenerConfig emailListenerConfig) {
        boolean enabled = emailListenerConfig.isEnable();
        if (enabled) {
            this.handlerList = new ArrayList<>();
            this.emailListenerConfig = emailListenerConfig;
            startListening();
        }
    }

    public void registerHandler(AbstractEmailHandler emailHandler) {
        handlerList.add(emailHandler);
    }

    @Async
    public void startListening() {
        startListening(false);
    }
    @Async
    public void startListening(boolean keepAlive) {
        try {
            Store store = this.emailListenerConfig.getStore();

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Create a new thread to keep the connection alive
//            Thread keepAliveThread = new Thread(new KeepAliveRunnable(inbox), "IdleConnectionKeepAlive");
//            keepAliveThread.start();

            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.RECENT), false));
            System.out.println("messages.length---" + messages.length);
            for (Message message : messages) {
                try {
                    InternetAddress sender = (InternetAddress) message.getFrom()[0];

//                    System.out.println("Sender name : " + sender.getPersonal());
//                    System.out.println("Sender email : " + sender.getAddress());

                    InternetAddress toEmail = (InternetAddress) message.getAllRecipients()[0];
//                    System.out.println("toEmail name : " + toEmail.getPersonal());
//                    System.out.println("toEmail email : " + toEmail.getAddress());

                    for (AbstractEmailHandler emailHandler: handlerList) {
                        boolean hasSenderPattern = emailHandler.getSenderPattern() != null;
                        boolean hasReceiverPattern = emailHandler.getRecieverPattern() != null;
                        boolean hasTopicPattern = emailHandler.getTopicPattern() != null;
                        boolean hasAnyFilter = hasSenderPattern || !hasReceiverPattern || hasTopicPattern;

                        boolean hasMatchSender = hasSenderPattern && sender.getAddress().matches(emailHandler.getSenderPattern());
                        boolean hasMatchReceiver = hasReceiverPattern && toEmail.getAddress().matches(emailHandler.getRecieverPattern());
                        boolean hasMatchTopic = hasTopicPattern && message.getSubject().matches(emailHandler.getTopicPattern());

//                        System.out.println("hasAnyFilter "+ hasAnyFilter);
//                        System.out.println("hasMatchSender "+ hasMatchSender);
//                        System.out.println("hasMatchReceiver "+ hasMatchReceiver);
                        if (hasAnyFilter) {
                            boolean hasAnyMatch = hasMatchSender || hasMatchReceiver || hasMatchTopic;
                            if (hasAnyMatch) {
                                emailHandler.handleMessage(message);
                            }
                        } else {
                            emailHandler.handleMessage(message);
                        }
                    }

                } catch (Exception e) {}
            }
//        inbox.addMessageCountListener(new MessageCountAdapter() {
//            @Override
//            public void messagesAdded(MessageCountEvent event) {
//                // Process the newly added messages
//                Message[] messages = event.getMessages();
//                for (Message message : messages) {
//                    try {
//                        // Implement your email processing logic here
//                        String subj = message.getSubject();
//                        String from = message.getFrom().toString();
//                        String from = message.getNonMatchingHeaders().toString();
//
//                        for (AbstractEmailHandler emailHandler : handlerList) {
//                            emailHandler.handleMessage(message); //todo
//                        }
//
//                        System.out.println("New email received: " + message.getSubject());
//                    } catch (MessagingException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

            // Start the IDLE Loop
//            while (!Thread.interrupted() && keepAlive) {
//                try {
//                    System.out.println("Starting IDLE");
////                    inbox.idle();
//                } catch (MessagingException e) {
//                    System.out.println("Messaging exception during IDLE");
//                    e.printStackTrace();
//                    inbox.close(false);
//                    store.close();
//                    throw new RuntimeException(e);
//                }
//            }

            // Interrupt and shutdown the keep-alive thread
//            if (keepAliveThread.isAlive()) {
//                keepAliveThread.interrupt();
//            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


//    class KeepAliveRunnable implements Runnable {
//        private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes
//        private Folder folder;
//        public KeepAliveRunnable(Folder folder) {
//            this.folder = folder;
//        }
//
//        @Override
//        public void run() {
//            while (!Thread.interrupted()) {
//                try {
//                    Thread.sleep(KEEP_ALIVE_FREQ);
//
//                    // Perform a NOOP to keep the connection alive
//                    System.out.println("Performing a NOOP to keep the connection alive");
//                    folder.doCommand(protocol -> {
//                        protocol.simpleCommand("NOOP", null);
//                        return null;
//                    });
//                } catch (InterruptedException e) {
//                    // Ignore, just aborting the thread...
//                } catch (MessagingException e) {
//                    // Shouldn't really happen...
//                    System.out.println("Unexpected exception while keeping alive the IDLE connection");
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

}
