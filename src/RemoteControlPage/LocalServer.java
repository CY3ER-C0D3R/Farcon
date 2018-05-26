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

package RemoteControlPage;

import Main.FXMLDocumentController;
import Common.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author admin
 */
public class LocalServer {
    
    private ServerSocket serverSock; // the Local Server socket
    private Socket clientConnection; // Socket for clients connecting to this Local Server
    private Socket clientsock; // the client on which the local server is running
    private OutputStreamWriter output; // output to write to master server
    private BufferedReader input; // input from master server
    private int port;
    private String ip;
    private String ID;
    private String RC_Password; //clients connecting must support this password in order to connect
    private boolean server_is_open = false; //becomes true only after registered in Master Server
    private boolean allowRemoteControl; // this variable is used to distinguish between presentation mode group meeting and between a remote control session.
    private String status;
    
    /**
     * Main server, runs on client and opens threads for individual communication with each client
     */
    public LocalServer(String username, String rc_password, String id, boolean allowRemoteControl) {
        this.allowRemoteControl = allowRemoteControl;
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        System.out.println("input local server.java");
        System.out.println(input);
        this.ID = id;
        this.RC_Password = rc_password;
        try {
            serverSock = new ServerSocket(0);
            port = serverSock.getLocalPort();
            ip = Context.getInstance().parseIP(InetAddress.getLocalHost().toString());
            if (allowRemoteControl) {  // register a regular remote control server on Master Server
                System.out.println("Registering Local Server on Master Server...");
                String line = "";
                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject parameters = new JSONObject();
                    jsonObject.put("Action", "register-local-server");
                    parameters.put("Username", username);
                    parameters.put("ID", this.ID);
                    parameters.put("RC_Password", this.RC_Password);
                    parameters.put("IP", ip);
                    parameters.put("Port", port);
                    jsonObject.put("Parameters", parameters);
                    //send data to master server
                    System.out.println(jsonObject);
                    output.write(jsonObject.toString() + "\n");
                    output.flush();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(LocalServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {  // register a Presentation Group Meeting server on Master Server
                JSONObject jsonObject = new JSONObject();
                JSONObject parameters = new JSONObject();
                String line = "";
                try {
                    jsonObject.put("Action", "register-group-server");
                    parameters.put("GID", this.ID);
                    parameters.put("G_Password", this.RC_Password);
                    parameters.put("IP", ip);
                    parameters.put("Port", port);
                    parameters.put("Type", "presentation");
                    jsonObject.put("Parameters", parameters);
                    //send data to master server
                    System.out.println(jsonObject);
                    output.write(jsonObject.toString() + "\n");
                    output.flush();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JSONException ex) {
                    Logger.getLogger(LocalServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //todo - further handling
    }
    
    public void setLocalServerIsOpen(boolean server_is_open)
    {
        this.server_is_open = server_is_open;
    }
    
    public void StartLocalServer(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (server_is_open) {
                    System.out.println("Waiting for client connection requests...");
                    try {
                        // For every client connection accepted by the server, spawn a
                        // separate thread to work with the client. The thread closes
                        // when the client explicitly disconnects from the connection
                        // or sends an END message.
                        clientConnection = serverSock.accept();
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                    int clientPort = clientConnection.getPort();
                    String clientIP = clientConnection.getInetAddress().toString();
                    System.out.println("Connection request accepted from client at IP address " + clientIP + " and port " + clientPort);
                    
                    LocalServerHandler newConnThread = new LocalServerHandler(clientConnection, ID, RC_Password, allowRemoteControl);
                    newConnThread.start();
                }
                System.out.println("Local Server Closed.");
            }
        });
        thread.start();
    }
    
    public void StopLocalServer(){
        server_is_open = false;
    }
}
