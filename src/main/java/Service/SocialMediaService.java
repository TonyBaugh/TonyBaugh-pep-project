package Service;


import java.util.List;
import DAO.SocialMediaDAO;
import Model.Account;
import Model.Message;

public class SocialMediaService {
    // DAO instance variable
    SocialMediaDAO smDAO;

    // No args constructor for initializing DAO
    public SocialMediaService() {
        smDAO = new SocialMediaDAO();
    }

    // Constructor for mocking the DAO if a DAO is passed in.
    public SocialMediaService(SocialMediaDAO smDAO) {
        this.smDAO = smDAO;
    }

    // Add new account
    public Account addAccount(Account account) {
        System.out.println("SocialMediaService is verifying \n" + account + "\n before using addAccount method from the DAO\n");

        // List of current usernames
        List<String> userNames = retrieveAllUsernames();
        System.out.println("list of usernames: " + userNames);        

        if (account.password.length() >= 4 && account.username.length() > 0 && !(userNames.contains(account.getUsername()))) {
            return SocialMediaDAO.addAccount(account);
        } else {
            return null;
        }
        
    }

    // Retrieve all accounts
    public List<Account> retrieveAll () {        
        return SocialMediaDAO.retrieveAll();
    }

    // Verify user login
    public Account userLogin (Account account) {
        return SocialMediaDAO.userLogin(account);
    }

    // Post new message
    public Message postMessage (Message message) {
        if (retrieveAccountById(message.getPosted_by()) == null || message.getMessage_text().length() > 255 || message.getMessage_text() == "") {
            return null;
        } else {
            return SocialMediaDAO.postMessage(message);
        }
    }

    // Retrieve all messages 
    public List<Message> retrieveAllMessages() {
        return SocialMediaDAO.retrieveAllMessages();
    }
    
    // Retrieve account by ID
    public Account retrieveAccountById(int accountId) {
        return SocialMediaDAO.retrieveAccountById(accountId);
    }

    // Retrieve all usernames
    public List<String> retrieveAllUsernames() {
        return SocialMediaDAO.retrieveAllUsernames();
    }

    // Delete message by ID
    public void deleteMessagebyId(int messageId) {
        SocialMediaDAO.deleteMessageById(messageId);
    }

    // Retrieve message by ID
    public Message retrieveMessageById(int messageId) {
        Message message = SocialMediaDAO.retrieveMessageById(messageId);
        if (message == null) {
            return null;
        } else if (message.getMessage_text().length() > 255 || message.getMessage_text() == "") {
            return null;
        } else {
            return SocialMediaDAO.retrieveMessageById(messageId);
        }
    }

    // Update message by ID
    public Message updateMessageById(String newMessageText, int messageId) {
        System.out.println("retrieveMessagesById from the updateMessage service call: \n" + SocialMediaDAO.retrieveMessageById(messageId));
        if (SocialMediaDAO.retrieveMessageById(messageId) == null || newMessageText.length() == 0 || newMessageText.length() > 255) {            
            return null;        
        } else {                        
            return SocialMediaDAO.updateMessageById(newMessageText, messageId);
        }
    }

    // Retrieve Messages by account ID
    public List<Message> retrieveMessagesByUser(int accountID) {
        return SocialMediaDAO.retrieveMessagesByUser(accountID);
    }

}
