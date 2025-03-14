package Controller;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.SocialMediaService;
import Util.ConnectionUtil;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SocialMediaController {

    SocialMediaService smService = new SocialMediaService();
    /**
     * Javalin app initialization and endpoint path definitions.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        ConnectionUtil.resetTestDatabase();
        Javalin app = Javalin.create(config -> {
            // Some QOL configs from our configuration lesson.
            config.routing.ignoreTrailingSlashes = true; // treat '/path' and '/path/' as the same path
            config.routing.treatMultipleSlashesAsSingleSlash = true; // treat '/path//subpath' and '/path/subpath' as the same path    
        });        
        app.post("/login", this::loginHandler);
        app.post("/register", this::registrationHandler);
        app.post("/messages", this::messageCreationHandler);
        app.get("/messages", this::retrieveAllMessagesHandler);
        app.get("/messages/{message_id}", this::retrieveMessageByIdHandler);
        app.delete("/messages/{message_id}", this::deleteMessageHandler);
        app.patch("/messages/{message_id}", this::updateMessageHandler);
        app.get("/accounts/{account_id}/messages", this::retrieveMessagesByUser);
        return app;   
    }

 
    /*##1: Process New User Registrations
     * As a user, I should be able to create a new Account on the endpoint POST localhost:8080/register. 
     * The body will contain a representation of a JSON Account, but will not contain an account_id.
        - The response status should be 200 OK, which is the default. 
        - The new account should be persisted to the database.
        - If the registration is not successful, the response status should be 400. (Client error)
     */
    private void registrationHandler(Context ctx) throws JsonProcessingException {
        // Jackson JSON mapper to convert JSON body to Account.class object
        ObjectMapper om = new ObjectMapper();

        // New account object to hold the account from the post body        
        Account account = om.readValue(ctx.body(), Account.class);        

        // Attemp to add account, validation checks in service layer
        Account newAccount = smService.addAccount(account);

        // If service layer validation fails, newAccount is null
        if (newAccount == null) {
            ctx.status(400);
        } else {
            ctx.json(newAccount);
            System.out.println("New Account: " +  newAccount);
        }
        
        System.out.println("Register New User Status: " + ctx.status());
    }



    /*##2: Process User Logins
     * As a user, I should be able to verify my login on the endpoint POST localhost:8080/login. The request body will contain a JSON 
       representation of an Account, not containing an account_id.

        - The response status should be 200 OK, which is the default.
        - If the login is not successful, the response status should be 401. (Unauthorized)
     */
    private void loginHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        Account account = om.readValue(ctx.body(), Account.class);

        // Try to verify account
        Account verifiedAccount = smService.userLogin(account);

        if (verifiedAccount == null) {
            ctx.status(401);
        } else {
            ctx.json(verifiedAccount);            
            System.out.println("Verified Account: " + verifiedAccount);
        }
        
        System.out.println("User Login Status: " + ctx.status());

    }


    /*##3: Process New Message Creation
    * As a user, I should be able to submit a new post on the endpoint POST localhost:8080/messages. 
      The request body will contain a JSON representation of a message, which should be persisted to the database, but will 
      not contain a message_id.

      - The response status should be 200, which is the default. The new message should be persisted to the database.
      - If the creation of the message is not successful, the response status should be 400. (Client error)
    */
    private void messageCreationHandler(Context ctx) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        // Map http body to Message class object
        Message message = om.readValue(ctx.body(), Message.class);        
        
        // Attempt to post Message
        Message postedMessage = smService.postMessage(message);

        if (postedMessage == null) {
            ctx.status(400);
        } else {
            ctx.json(postedMessage);
            System.out.println("Posted Message: " + postedMessage);
        }
        
        System.out.println("Posted Message Status: " + ctx.status());
    }
      
      
      
    /*##4: Retrieve all messages
    * As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages.

      - The response body should contain a JSON representation of a list containing all messages retrieved from the database. 
      - It is expected for the list to simply be empty if there are no messages. 
      - The response status should always be 200, which is the default.
    */

    private void retrieveAllMessagesHandler(Context ctx) {        
        List<Message> messageList = smService.retrieveAllMessages();
        ctx.json(messageList);
        System.out.println("Messages List: ");
        for (Message message : messageList) {
            System.out.println(message);
        }
        System.out.println("Retrieve All Messages Status: " + ctx.status());
    }




    /*##5: Retrieve a message by its id
     * As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/messages/{message_id}.

        - The response body should contain a JSON representation of the message identified by the message_id. 
        - It is expected for the response body to simply be empty if there is no such message. 
        - The response status should always be 200, which is the default.
     */

    private void retrieveMessageByIdHandler(Context ctx) throws JsonProcessingException{
       
        // Retrieve message ID from api path parameter
        int message_id = Integer.parseInt(ctx.pathParam("message_id"));        

        Message retrievedMessage = smService.retrieveMessageById(message_id);

        // If no message with that ID return status 200
        if (retrievedMessage == null) {
            ctx.status(200);
        // Else return the Message object 
        } else {
            ctx.json(retrievedMessage);
            System.out.println("Retrieved Message: " + retrievedMessage);
        }
        
        System.out.println("Retrieve Message by ID Status: " + ctx.status());
    }



     /*##6: Delete a message by its id
      * As a User, I should be able to submit a DELETE request on the endpoint DELETE localhost:8080/messages/{message_id}.

        - The deletion of an existing message should remove an existing message from the database. 
        - If the message existed, the response body should contain the now-deleted message. 
        - The response status should be 200, which is the default.
        - If the message did not exist, the response status should be 200, but the response body should be empty.         
      */

    private void deleteMessageHandler(Context ctx) throws JsonProcessingException {
        
        // Retrieve message ID
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        if (smService.retrieveMessageById(messageId) == null) {
            // If message id doesn't exist, return 200 to keep delete idempotent
            ctx.status(200);            
        } else {

            // Retrieve message
            Message message = smService.retrieveMessageById(messageId);
            
            // return deleted message in body 
            ctx.json(message);

            // Delete message
            smService.deleteMessagebyId(messageId);
            System.out.println("Deleted Message: " + message);            
        }
        System.out.println("Delete Message by ID Status: " + ctx.status());

    }


      /*##7: Update message text by it's id
       * As a user, I should be able to submit a PATCH request on the endpoint PATCH localhost:8080/messages/{message_id}. 
           The request body should contain a new message_text values to replace the message identified by message_id. 
           The request body can not be guaranteed to contain any other information.

        If the update is successful, the response body should contain:
        - The full updated message (including message_id, posted_by, message_text, and time_posted_epoch) 
        - The response status should be 200, which is the default. The message existing on the database should have the updated message_text.

        If the update of the message is not successful for any reason, the response status should be 400. (Client error)
       */
    private void updateMessageHandler(Context ctx) throws JsonProcessingException{
        // Retrieve message ID
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.readTree(ctx.body());
        String newMessageText = jsonNode.get("message_text").asText();
        
        // Attempt to update message
        Message updatedMessage = smService.updateMessageById(newMessageText, messageId);

        if (updatedMessage == null) {
            // If message id doesn't exist or fails validation, return 400
            ctx.status(400);          
        } else {                       

            // return updated message in body 
            ctx.json(updatedMessage);
            ctx.status(200);
            System.out.println("Updated Message: " + updatedMessage);            
        }
        System.out.println("Update Message by ID Status: " + ctx.status());
    }



    /*##8: Retrieve all messages written by a particular user
      * As a user, I should be able to submit a GET request on the endpoint GET localhost:8080/accounts/{account_id}/messages.

      - The response body should contain a JSON representation of a list containing all messages posted by a particular user, which 
        is retrieved from the database. It is expected for the list to simply be empty if there are no messages. 
      
      - The response status should always be 200, which is the default.
     */

    private void retrieveMessagesByUser(Context ctx) {
        // Retrieve account ID
        int accountID = Integer.parseInt(ctx.pathParam("account_id"));

        // Retrieve list of messages
        List<Message> messageList = smService.retrieveMessagesByUser(accountID);
        System.out.println("Message list received: ");
        for (Message message : messageList) {
            System.out.println(message);
        }
        ctx.json(messageList);
        ctx.status(200);
        System.out.println("Retrieve messages by user Status: " + ctx.status());

    }
}