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

    /*
     *                          ***** REGISTER NEW ACCOUNT *****
     * The registration will be successful if and only if:
       - The username is not blank
       - The password is at least 4 characters long, 
       - An Account with that username does not already exist.
       - The response body should contain a JSON of the Account, including its account_id. 
     */
    public Account addAccount(Account account) {
        // List of current usernames
        List<String> userNames = retrieveAllUsernames();        

        // Verify username, password, and account before attempting to add account
        if (account.password.length() >= 4 && account.username.length() > 0 && !(userNames.contains(account.getUsername()))) {
            return SocialMediaDAO.addAccount(account);
        } else {
            return null;
        }
        
    }
 
    /*
     *                          ***** VERIFY USER LOGIN *****
     * The login will be successful if and only if:
         - The username and password provided in the request body JSON match a real account existing on the database. 
         - If successful, the response body should contain a JSON of the account in the response body, including its account_id. 
     */    
    public Account userLogin (Account account) {
        //Check if password and username fields are valid lengths
        if (account.getUsername().length() < 1 || account.getPassword().length() < 4) {
            return null;
        } else {
            return SocialMediaDAO.userLogin(account);
        }
    }

    /*
     *                          ***** POST NEW MESSAGE *****
     * The creation of the message will be successful if and only if: 
         - The message_text is not blank
         - The message_text  is not over 255 characters
         - Posted_by refers to a real, existing user. 
         - If successful, the response body should contain a JSON of the message, including its message_id. 
     */    
    public Message postMessage (Message message) {        
        // Verify message_text and posted_by user before attempting to post message
        if (retrieveAccountById(message.getPosted_by()) == null || message.getMessage_text().length() > 255 || message.getMessage_text() == "") {
            return null;
        } else {
            return SocialMediaDAO.postMessage(message);
        }
    }

    /*
     *                          ***** RETRIEVE ALL MESSAGES *****
     */
    public List<Message> retrieveAllMessages() {
        return SocialMediaDAO.retrieveAllMessages();
    }
    
    /*
     *                          ***** RETRIEVE ACCOUNT BY ID *****
     */
    public Account retrieveAccountById(int accountId) {        
        return SocialMediaDAO.retrieveAccountById(accountId);
    }

    /*
     *                          ***** RETRIEVE ALL USERNAMES *****
     */
    public List<String> retrieveAllUsernames() {
        return SocialMediaDAO.retrieveAllUsernames();
    }

    /*
     *                          ***** DELETE MESSAGE BY ID *****
     */
    public void deleteMessagebyId(int messageId) {
        SocialMediaDAO.deleteMessageById(messageId);
    }

    /*
     *                          ***** RETRIEVE MESSAGE BY ID *****
     */
    public Message retrieveMessageById(int messageId) {        
        return SocialMediaDAO.retrieveMessageById(messageId);    
    }

    /*
     *                          ***** UPDATE MESSAGE BY ID *****
     * The update of a message should be successful if and only if: 
       - The message id already exists
       - The new message_text is not blank and is not over 255 characters. 
     */
    public Message updateMessageById(String newMessageText, int messageId) {        
        // Check for message existence and proper length. 
        if (SocialMediaDAO.retrieveMessageById(messageId) == null || newMessageText.length() == 0 || newMessageText.length() > 255) {            
            return null;        
        } else {                        
            return SocialMediaDAO.updateMessageById(newMessageText, messageId);
        }
    }

    /*
     *                          ***** RETRIEVE ALL MESSAGES FROM A SINGLE USER *****     
     */
    public List<Message> retrieveMessagesByUser(int accountID) {
        return SocialMediaDAO.retrieveMessagesByUser(accountID);
    }

}
