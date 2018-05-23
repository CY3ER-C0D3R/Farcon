/*
 * Farcon Software
 *
 * This program is a Group Collaboration and
 * Remote Control Software, free of charge,
 * for personal or commercial use.
 *
 * Open source, code written in javafx.
 * Written by: Yuval Stein @CY3ER-C0D3R
 *
 * https://github.com/CY3ER-C0D3R/Farcon
 *
 * 2018 (c) Farcon
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
