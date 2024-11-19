package run.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author michelle
 */
/*
    Created based on:
        create table if not exists messages
        (
            messageID int not null AUTO_INCREMENT,
            sender varchar(10) not null,
            recipient varchar(10) not null,
            subject varchar(100) not null,
            body varchar(500) not null,
            readStatus boolean not null default FALSE,
            deletedForSender boolean not null default FALSE,
            deletedForRecipient boolean not null default FALSE,
            dateSent timestamp not null,
            PRIMARY KEY(messageID),
            FOREIGN KEY (sender) REFERENCEs users(username) on delete cascade,
            FOREIGN KEY (recipient) REFERENCES users(username) on delete cascade
        );
*/
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
public class Message implements Comparable<Message>{
    // Formatter used by toString - can be shared by ALL Message objects
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss EEE dd MMM yyyy");

    @EqualsAndHashCode.Include
    private int messageID;
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    private boolean readStatus;
    private boolean deletedForSender;
    private boolean deletedForRecipient;
    private LocalDateTime timestamp;

    public Message(String sender, String recipient, String subject, String body){
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }


    // Method to access timestamp information for internationalization usage
    public Date getTimestampAsDate(){
        return Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public String toString(){
        return "Message{" + "messageID=" + messageID + ", sender=" + sender + ", recipient=" + recipient + ", subject=" + subject + ", body=" + body + ", readStatus=" + readStatus + ", deletedForSender=" + deletedForSender + ", deletedForRecipient=" + deletedForRecipient + ", timestamp=" + timestamp.format(FORMATTER) + '}';
    }

    @Override
    public int compareTo(Message m) {
        return timestamp.compareTo(m.timestamp) * -1;
    }
}

