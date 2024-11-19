package run.persistence;

import run.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author michelle
 */
public class MessageDaoImpl extends MySQLDao implements MessageDao {
    public MessageDaoImpl(String propertiesFile) {
        super(propertiesFile);
    }

    public MessageDaoImpl(Connection conn){
        super(conn);
    }

    private static Message mapRow(ResultSet rs) throws SQLException {
        // Get all components
        int messageID = rs.getInt("messageID");
        String senderName = rs.getString("sender");
        String recipient = rs.getString("recipient");
        String subject = rs.getString("subject");
        String body = rs.getString("body");
        boolean readStatus = rs.getBoolean("readStatus");
        boolean deletedForSender = rs.getBoolean("deletedForSender");
        boolean deletedForRecipient = rs.getBoolean("deletedForRecipient");
        LocalDateTime timestamp = rs.getTimestamp("dateSent").toLocalDateTime();

        // Create a Message object
        return new Message(messageID, senderName, recipient, subject, body, readStatus, deletedForSender,
            deletedForRecipient, timestamp);
    }

    /**
     * Retrieve all <code>Messages</code> in the database sent by a specific
     * <code>User</code>.
     *
     * @param senderName The username of the <code>User</code> whose sent
     *                   <code>Messages</code> are being retrieved
     *
     * @return An <code>ArrayList</code> of <code>Message</code> objects sent by
     *         the supplied username. This <code>ArrayList</code> will be empty
     *         if there were no sent <code>Message</code> entries found for the
     *         supplied username.
     */
    @Override
    public ArrayList<Message> getSentMessagesForUser(String senderName) {
        ArrayList<Message> sentMessages = new ArrayList<>();

        Connection con = this.getConnection();
        // Select all undeleted sent messages for a specific user
        String query = "SELECT * FROM messages WHERE sender = ? AND deletedForSender = FALSE";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, senderName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Make a Message & add it to the list.
                    Message m = mapRow(rs);
                    // Add the new message to the list
                    sentMessages.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println(LocalDateTime.now() + ": An SQLException occurred in getSentMessagesForUser()");
            System.out.println("Error: " + e.getMessage());
        }
        this.freeConnection(con);
        return sentMessages;
    }


    /**
     * Retrieve all <code>Messages</code> in the database received by a specific
     * <code>User</code>.
     *
     * @param recipientName The username of the <code>User</code> whose received
     *                      <code>Messages</code> are being retrieved
     *
     * @return An <code>ArrayList</code> of <code>Message</code> objects
     *         received by the supplied username. This <code>ArrayList</code>
     *         will be empty if there were no received <code>Message</code>
     *         entries found for the supplied username.
     */
    @Override
    public ArrayList<Message> getReceivedMessagesForUser(String recipientName) {
        ArrayList<Message> receivedMessages = new ArrayList<>();
        Connection con = this.getConnection();

        // Select all undeleted received messages for a specific user
        String query = "SELECT * FROM messages WHERE recipient = ? AND deletedForRecipient = FALSE";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, recipientName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = mapRow(rs);
                    receivedMessages.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println(LocalDateTime.now() + ": An SQLException occurred in getReceivedMessagesForUser()");
            System.out.println("Error: " + e.getMessage());
        }
        this.freeConnection(con);

        return receivedMessages;
    }

    /**
     * Get a specific <code>Message</code> based on its ID number.
     *
     * @param messageID The ID number of the <code>Message</code> to be
     *                  retrieved.
     *
     * @return A <code>Message</code> object matching the ID number supplied. If
     *         no match is found, the method returns null.
     */
    @Override
    public Message getMessageById(int messageID) {
        Connection con = this.getConnection();
        String query = "SELECT * FROM messages WHERE messageID = ?";

        Message m = null;
        try(PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, messageID);

            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    m = mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println(LocalDateTime.now() + ": An SQLException occurred in getMessageById()");
            System.out.println("Error: " + e.getMessage());
        }
        this.freeConnection(con);
        return m;
    }

    /**
     * Add a new <code>Message</code> to the database. This adds a new
     * <code>Message</code> to the database with the current time as its
     * timestamp
     *
     * @param sender    Sender of the <code>Message</code> (a username)
     * @param recipient Recipient of the <code>Message</code> (a username)
     * @param subject   The subject line for the <code>Message</code>
     * @param body      The body/content of the <code>Message</code>
     *
     * @return >0 if the <code>Message</code> was added to the database. This
     *         value will be the ID of the newly-added <code>Message</code>.<br>
     * -1 if the <code>Message</code> could not be added as the sender and
     * recipient are not friends.<br>
     * -2 if the <code>Message</code> could not be added as either the sender or
     * the recipient do not exist.<br>
     * 0 if the <code>Message</code> could not be added for any other reason.
     */
    @Override
    public int sendMessage(String sender, String recipient, String subject, String body) {
        // Confirm that the sender and recipient users exist in the system before
        // trying to send them a message
        UserDaoImpl userDao = new UserDaoImpl(getPropertiesFile());
        if (userDao.findUserByUsername(sender) != null && userDao.findUserByUsername(recipient) != null) {
            // Check if there is a Friendship established between the sender and recipient
            FriendshipDaoImpl friendshipDao = new FriendshipDaoImpl(getPropertiesFile());
            // If a Friendship is found between the two, continue to try and send the new message
            if (friendshipDao.checkFriendshipStatus(sender, recipient) != null) {
                Connection con = this.getConnection();

                int returnValue = 0;
                String query = "INSERT INTO messages(sender, recipient, subject, body, dateSent) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP())";
                try(PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, sender);
                    ps.setString(2, recipient);
                    ps.setString(3, subject);
                    ps.setString(4, body);

                    ps.executeUpdate();

                    try(ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            returnValue = generatedKeys.getInt(1);
                        }
                    }
                } catch(SQLIntegrityConstraintViolationException e){
                    System.err.println(LocalDateTime.now() + ": An integrity constraint failed while adding a " +
                            "Message" + "." + ".");
                    System.out.println("Error: " + e.getMessage());
                }catch(SQLException e){
                    System.err.println(LocalDateTime.now() + ": An SQLException occurred while adding a Message." +
                            ".");
                    System.out.println("Error: " + e.getMessage());
                }
                this.freeConnection(con);
                return returnValue;
            } else { // No friendship exists between the sender and recipient, so return -1 as error code{
                return -1;
            }
        } else { // Either the sender or the recipient don't exist in the syste, so return -2 as error code
            return -2;
        }
    }

    /**
     * Mark a specific <code>Message</code> as read in the database. If the
     * <code>Message</code> matching the supplied ID was not sent to the
     * specified recipient, the read status of the <code>Message</code> is not
     * amended.
     *
     * @param messageID The ID number of the <code>Message</code> to be marked
     *                  as read
     * @param recipient The username of the recipient <code>User</code> on the
     *                  <code>Message</code> to be marked as read.
     *
     * @return True if the <code>Message</code> can be marked as read, false
     *         otherwise.
     */
    @Override
    public boolean markMessageAsRead(int messageID, String recipient) {
        Connection con = null;
        PreparedStatement ps = null;
        int rowsAffected = 0;

        try {
            con = this.getConnection();

            String query = "UPDATE messages SET readStatus = TRUE WHERE messageID = ? AND recipient = ?";
            ps = con.prepareStatement(query);
            ps.setInt(1, messageID);
            ps.setString(2, recipient);

            rowsAffected = ps.executeUpdate();
        } catch(SQLException e){
            System.err.println(LocalDateTime.now() + ": An SQLException occurred in markMessageAsRead().");
            System.out.println("Error: " + e.getMessage());
        }
        return rowsAffected == 1;
    }

    /**
     * Mark a specific <code>Message</code> as deleted for the sender in the
     * database. If the <code>Message</code> matching the supplied ID was not
     * sent by the specified sender, the deleted for sender status of the
     * <code>Message</code> is not amended.
     *
     * @param messageID The ID number of the <code>Message</code> to be marked
     *                  as deleted for the sender.
     * @param sender    The username of the sending <code>User</code> on the
     *                  <code>Message</code> to be marked as deleted for the
     *                  sender.
     *
     * @return True if the <code>Message</code> can be marked as deleted for the
     *         sender, false otherwise.
     */
    @Override
    public boolean deleteMessageForSender(int messageID, String sender) {
        Connection con = this.getConnection();

        int rowsAffected = 0;
        String query = "UPDATE messages SET deletedForSender = TRUE WHERE messageID = ? AND sender = ?";
        try (PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, messageID);
            ps.setString(2, sender);

            rowsAffected = ps.executeUpdate();
        } catch(SQLException e){
            System.err.println(LocalDateTime.now() + ": An SQLException occurred in deleteMessageForSender().");
            System.out.println("Error: " + e.getMessage());
        }
        return rowsAffected == 1;
    }


    /**
     * Searches for messages received by a specific user containing  search term .
     * The search results are ordered  in descending order.
     * This method first checks if the user exists in the system. If the user does not exist, an empty list is returned.
     * If the user exists, theres a  query to find all messages where the recipient matches the
     * username and the subject or body contains the search term.
     *
     *
     * @param username  username of the user whose messages are being searched.
     * @param searchTerm The term to search for
     * @return A list of messages that match the search term, or an empty list if no messages match or if the user does not exist.
     * @throws SQLException If there is an error
     */


    @Override
    public List<Message> searchMessages(String username, String searchTerm) {
        // Validate that the user exists in the system
        UserDaoImpl userDao = new UserDaoImpl(getPropertiesFile());
        if (userDao.findUserByUsername(username) == null) {
            return Collections.emptyList(); // Return an empty list if the user doesn't exist
        }

        // Prepare the query to search for messages
        String query = "SELECT * FROM messages WHERE recipient = ? AND (subject LIKE ? OR body LIKE ?) ORDER BY dateSent DESC";
        List<Message> messages = new ArrayList<>();

        // Execute the query and retrieve results
        try (Connection con = this.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, "%" + searchTerm + "%");
            ps.setString(3, "%" + searchTerm + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Create the message object and add it to the list
                    Message message = new Message(
                            rs.getInt("messageID"),
                            rs.getString("sender"),
                            rs.getString("recipient"),
                            rs.getString("subject"),
                            rs.getString("body"),
                            rs.getBoolean("readStatus"),
                            rs.getBoolean("deletedForSender"),
                            rs.getBoolean("deletedForRecipient"),
                            rs.getTimestamp("dateSent").toLocalDateTime()
                    );
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            log.error("Error searching messages: ", e);
        }

        return messages;
    }



    /**
     * Gets a specific message by its ID from the database.
     *
     * this method gets a message based on its id , if it does not find it , it returns null
     *
     *
     * @param id The ID of the message .
     * @return iteturns the message with the specific id
     */
    @Override
    public Message getMessageById(int id) {
        // Initialize the Message object to be returned
        Message message = null;

        // SQL query to select a message by its ID
        String query = "SELECT * FROM messages WHERE messageID = ?";

        // Establish a database connection
        try (Connection con = this.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            // Set the message ID parameter in the query
            ps.setInt(1, id);

            // Execute the query and process the results
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // If a message with the given ID is found, construct the Message object
                    message = new Message(
                            rs.getInt("messageID"),
                            rs.getString("sender"),
                            rs.getString("recipient"),
                            rs.getString("subject"),
                            rs.getString("body"),
                            rs.getBoolean("readStatus"),
                            rs.getBoolean("deletedForSender"),
                            rs.getBoolean("deletedForRecipient"),
                            rs.getTimestamp("dateSent").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            log.error("Error retrieving message by ID: ", e);
        }

        // Return the message object, or null if no message was found
        return message;
    }



    /**
     * Mark a specific <code>Message</code> as deleted for the recipient in the
     * database. If the <code>Message</code> matching the supplied ID was not
     * received by the specified recipient, the deleted for recipient status of
     * the <code>Message</code> is not amended.
     *
     * @param messageID The ID number of the <code>Message</code> to be marked
     *                  as deleted for the recipient.
     * @param recipient The username of the receiving <code>User</code> on the
     *                  <code>Message</code> to be marked as deleted for the
     *                  recipient.
     *
     * @return True if the <code>Message</code> can be marked as deleted for the
     *         recipient, false otherwise.
     */
    @Override
    public boolean deleteMessageForRecipient(int messageID, String recipient) {
        Connection con = this.getConnection();
        int rowsAffected = 0;

        String query = "UPDATE messages SET deletedForRecipient = TRUE WHERE messageID = ? AND recipient = ?";
        try(PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, messageID);
            ps.setString(2, recipient);

            rowsAffected = ps.executeUpdate();
        } catch(SQLException e){
            System.err.println(LocalDateTime.now() + ": An SQLException occurred in deleteMessageForRecipient().");
            System.out.println("Error: " + e.getMessage());
        }
        this.freeConnection(con);
        return rowsAffected == 1;
    }

    @Override
    public List<Message> searchMessages(String username, String searchTerm) {
        return List.of();
    }

    public static void main(String[] args) {
        MessageDaoImpl messagesDao = new MessageDaoImpl("database.properties");

        //********************Testing sending a message*************************
        // Test the send message method:
        int resultOfValidSend = messagesDao.sendMessage("Charles", "Rick",
                "Testing the send method",
                "Just need to check if the send message method is working!");
        System.out.println("Valid message sent? : " + resultOfValidSend
                + " (This should give a number > 0. This value is the ID of the new message)");
        System.out.println();

        int resultOfNoFriendshipSend = messagesDao.sendMessage("Charles", "Michelle",
                "Testing the send method", "Just need to check if the send message "
                + "method is working! This should NOT work");
        System.out.println("No Friendship sending message ? : " + resultOfNoFriendshipSend + " (This should be -1)");
        System.out.println();

        int resultOfInvalidUserSend = messagesDao.sendMessage("Charles", "James",
                "Testing the send method where a user doesn't exist", "Just need "
                + "to check if the send message method is working! "
                + "This should NOT work");
        System.out.println("Invalid user sending message ? : " + resultOfInvalidUserSend + " (This should be -2)");
        System.out.println();

        //*****************Testing marking a message as read********************
        boolean markedAsRead = messagesDao.markMessageAsRead(5, "Rick");
        System.out.println("Message marked as read? : " + markedAsRead);
        System.out.println();

        //*****************Testing marking a message as read********************
        boolean markedAsDeletedForSender = messagesDao.deleteMessageForSender(8, "Charles");
        System.out.println("Message marked as deleted for sender? : " + markedAsDeletedForSender);
        System.out.println();

        //*****************Testing marking a message as read********************
        boolean markedAsDeletedForRecipient = messagesDao.deleteMessageForRecipient(3, "Steph");
        System.out.println("Message marked as deleted for recipient? : " + markedAsDeletedForRecipient);
        System.out.println();

        //*****************Testing getting all sent message*********************
        ArrayList<Message> sentMessages = messagesDao.getSentMessagesForUser("Rick");
        System.out.println("Messages sent by Rick: ");
        for (Message m : sentMessages) {
            System.out.println(m);
            System.out.println("Displaying timestamp information as a Date: " + m.getTimestampAsDate());
        }
        System.out.println();

        //***************Testing getting all received message*******************
        ArrayList<Message> receivedMessages = messagesDao.getReceivedMessagesForUser("Rick");
        System.out.println("Messages received by Rick: ");
        for (Message m : receivedMessages) {
            System.out.println(m);
            System.out.println("Displaying timestamp information as a Date: " + m.getTimestampAsDate());
        }
        System.out.println();

        System.out.println("Messages received by Rick (in order of date received, from newest to oldest): ");
        Collections.sort(receivedMessages);
        for (Message m : receivedMessages) {
            System.out.println(m);
        }
        System.out.println();

        //****************Testing getting a specific message********************
        Message m = messagesDao.getMessageById(5);
        System.out.println("Message matching id 5 : " + m);
    }
}
