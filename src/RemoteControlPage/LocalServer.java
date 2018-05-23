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
    private FXMLDocumentController f;
    
    private ServerSocket serverSock; // the Local Server socket
    private Socket clientConnection; // Socket for clients connecting to this Local Server
    private Socket clientsock; // the client on which the local server is running
    private OutputStreamWriter output; // output to write to master server
    private BufferedReader input; // input from master server
    private int port;
    private String ID;
    private String RC_Password; //clients connecting must support this password in order to connect
    //private boolean server_is_open = false; //becomes true only after registered in Master Server
    private String status;
    
    /**
     * Main server, runs on client and opens threads for individual communication with each client
     */
    public LocalServer(FXMLDocumentController f, String username, String rc_password, String id) {
        this.f = f;
        
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
            //register on masterserver
            System.out.println("Registering Local Server on Master Server...");
            //m.RegisterIPPORT(username, rc_password, id, serverSock.getInetAddress().toString(), port);

            String line = "";
            try {
                JSONObject jsonObject = new JSONObject();
                JSONObject parameters = new JSONObject();
                jsonObject.put("Action", "register-local-server");
                parameters.put("Username", username);
                parameters.put("ID", this.ID);
                parameters.put("RC_Password", this.RC_Password);
                parameters.put("IP", Context.getInstance().parseIP(InetAddress.getLocalHost().toString()));
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
//                    status = "";
//                    JSONObject jsonObject = null;
//                    try {
//                        jsonObject = new JSONObject(line);
//                        status = jsonObject.getString("Status");
//                    } catch (JSONException ex) {
//                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    //update status bar accordingly
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            f.updateStatusBar(status, false);
//                        }
//                    });
            //System.out.println("Local Server Registered. Started server module...");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //todo - further handling
    }
    
    public void StartLocalServer(){
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (f.LocalServerIsOpen) {
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
                    
                    LocalServerHandler newConnThread = new LocalServerHandler(f, clientConnection, ID, RC_Password);
                    newConnThread.start();
                }
                System.out.println("Local Server Closed.");
            }
        });
        thread.start();
    }
}
