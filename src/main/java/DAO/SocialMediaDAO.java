package DAO;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import Model.Account;
import Model.Message;
import Util.ConnectionUtil;

public class SocialMediaDAO {
    
    // Instance variables
    private static SocialMediaDAO smDAO = null;
    private static long epochSeconds = Instant.now().getEpochSecond();

    // Default constructor
    public SocialMediaDAO() {        
    }

    // Static block to initialize new DAO
    static SocialMediaDAO instance() {
        if (smDAO == null) {
            smDAO = new SocialMediaDAO();
        }
        return smDAO;
    }

    /*
     * Add new account
    */
    public static Account addAccount(Account account) {
        Connection conn = ConnectionUtil.getConnection();      

        try {
            String sql = "INSERT INTO account (username, password) VALUES (?, ?);";

            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());

            int rowsAffected = ps.executeUpdate();
            System.out.printf("%d rows affected!\n Account added: \n", rowsAffected);
            
            // Return new account details
            return userLogin(account);

        }
        catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        // If failed to add account return null
        return null;
    }

    /*
     * Retrieve a list of all accounts ( Test Feature ) 
     */

    public static List<Account> retrieveAll() {
        Connection conn = ConnectionUtil.getConnection();
        List<Account> accountList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM account;";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                Account account = new Account(rs.getInt("account_id"), rs.getString("username"), 
                                              rs.getString("password"));
                accountList.add(account);
            }
        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        System.out.println("Account list returned, size: " + accountList.size());
        System.out.println(accountList);
        
        // Return list of accounts
        return accountList;
    }

    /*
     * Retrieve Account by ID ( For validations )
     */

     public static Account retrieveAccountById(int account_id) {
        Connection conn = ConnectionUtil.getConnection();
        
        try {
            String sql = "SELECT * FROM account WHERE account_id=?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, account_id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {            
                Account retrievedAccount = new Account(account_id, rs.getString(1), rs.getString(2));
                return retrievedAccount;
            }
            
        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        // If Account retrieval fails return null
        return null;
     }

    /*
     *  Retrieve all usernames ( For registration validation )
     */
    public static List<String> retrieveAllUsernames() {
        Connection conn = ConnectionUtil.getConnection();
        List<String> userList = new ArrayList<>();
        try {
            String sql = "Select username FROM account;";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                userList.add(rs.getString("username"));
            }

        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }

        // Return list of users
        return userList;
    }   

    
    /*
     * Verify user login credentials
     */
    public static Account userLogin(Account account) {
        Connection conn = ConnectionUtil.getConnection();        
        try {
            String sql = "Select account_id, username, password FROM account WHERE username=? AND password=?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPassword());
            ResultSet rs = ps.executeQuery();
            
            // If there is a matching result, return the account details
            if (rs.next()) {
                return new Account(rs.getInt(1), rs.getString(2), rs.getString(3));
            }

        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        // If no matching result, return null
        return null;
    }

    /*
     * Post a new message
     */

    public static Message postMessage(Message message) {
        Connection conn = ConnectionUtil.getConnection();

        try {
            String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES(?, ?, ?);";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, message.getPosted_by());            
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, epochSeconds);
            
            int rowsAffected = ps.executeUpdate();
            System.out.printf("%d rows affected!\n Message added: \n", rowsAffected);

            // Retrieve auto-generated message_id for return body
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newMessageID = keys.getInt(1);
                return new Message(newMessageID, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
            }             

        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }

        // If failed to insert message return null
        return null;
    }

    /*
     * Retrieve All Messages
     */

    public static List<Message> retrieveAllMessages() {
        Connection conn = ConnectionUtil.getConnection();

        List<Message> messageList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM message;";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {                
                Message newMessage = new Message(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getLong(4));
                messageList.add(newMessage);
            }
            return messageList;
        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        return messageList;
    }

    public static Message retrieveMessageById(int message_id) {
        Connection conn = ConnectionUtil.getConnection();
        
        try {
            String sql = "SELECT posted_by, message_text, time_posted_epoch FROM message WHERE message_id=?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, message_id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {            
                Message newMessage = new Message(message_id, rs.getInt(1), rs.getString(2), rs.getLong(3));
                return newMessage;
            }
            
        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        // If message retrieval fails return null
        return null;
    }

    public static void deleteMessageById(int message_id) {
        Connection conn = ConnectionUtil.getConnection();

        try {
            
            String sql = "DELETE FROM message WHERE message_id=?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, message_id);

            int rowsAffected = ps.executeUpdate();
            System.out.printf("%d rows affected!\n Message deleted: \n", rowsAffected);            

        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
    }

    public static Message updateMessageById(String message_text, int message_id) {
        Connection conn = ConnectionUtil.getConnection();

        try {
            
            String sql = "UPDATE message SET message_text=? WHERE message_id=?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, message_text);
            ps.setInt(2, message_id);

            int rowsAffected = ps.executeUpdate();
            System.out.printf("%d rows affected!\n Message updated: \n", rowsAffected);

            
            
            // Return updated message            
            return retrieveMessageById(message_id);

        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        // If update fails, return null
        return null;
    }

    // Retrieve messages by user
    public static List<Message> retrieveMessagesByUser(int accountID) {
        Connection conn = ConnectionUtil.getConnection();
        List<Message> messageList = new ArrayList<>();

        try {
            String sql = "SELECT * FROM message INNER JOIN account ON message.posted_by = account.account_id where message.posted_by=?;";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, accountID);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Message newMessage = new Message(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getLong(4));
                messageList.add(newMessage);
            }
            return messageList;
        } catch (SQLException sqle) {
            System.out.println("Error: " + sqle.getMessage());
            sqle.printStackTrace();
            // Return empty list if no messages added
            return messageList;
        }        
    }

}
