/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OnlineChatPage;

/**
 *
 * @author admin
 */
public interface OnlineChatInterface {
    public void sendMessageToServer(String message, String messageInfo);
    public void receiveMessageFromServer(String sender_username, String message, String messageInfo);
}
