/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupCollaborationPage;

import RemoteControlPage.LocalServer;
import Common.RemoteConnectionData;
import Main.FXMLDocumentController;
import Common.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author admin
 */
public class LocalGroupServer {
    private GroupCollaborationPageController gc;
    
    private ServerSocket serverSock; // the Local Server socket
    private Socket clientConnection; // Socket for clients connecting to this Local Server
    private HashMap<Socket, ObjectOutputStream> groupClients = new HashMap<>();
    private Socket clientsock; // the client on which the local server is running
    private OutputStreamWriter output; // output to write to master server
    private BufferedReader input; // input from master server
    private String ip;
    private int port;
    private String GID;
    private String group_Password; //clients connecting must support this password in order to connect
    private String type; // type can be collaboration / presentation 
    private String selectedFileName;
    //private boolean server_is_open = false; //becomes true only after registered in Master Server
    private String status;
    
    /**
     * Main server, runs on client and opens threads for individual communication with each client
     * @param group_id
     * @param group_password
     * @param type
     */
    public LocalGroupServer(String group_id, String group_password) {
        this.gc = Context.getInstance().getGc();
        
        this.clientsock = Context.getInstance().getClientsock();
        this.output = Context.getInstance().getOutput();
        this.input = Context.getInstance().getInput();
        try {
            this.ip = Context.getInstance().parseIP(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException ex) {
            Logger.getLogger(LocalGroupServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.GID = group_id;
        this.group_Password = group_password;
        //this.type = type;
        System.out.println("GROUP ID AND PASSWORD");
        System.out.println(group_id);
        System.out.println(group_password);
        //System.out.println(type);
        try {
            serverSock = new ServerSocket(0);
            port = serverSock.getLocalPort();
            //register on masterserver
            System.out.println("Am I here? lmao alen more random stufff");
            System.out.println("Registering Local Group Server on Master Server...");
            JSONObject jsonObject = new JSONObject();
            JSONObject parameters = new JSONObject();
            String line = "";
            try {
                jsonObject.put("Action", "register-group-server");
                parameters.put("GID", this.GID);
                parameters.put("G_Password", this.group_Password);
                parameters.put("IP", ip);
                parameters.put("Port", port);
                parameters.put("Type", "collaboration");
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // add ip and port later, when starting the server
                Context.getInstance().setRemote_data(new RemoteConnectionData(ip, port,
                        GID, group_Password, "collaboration", "host"));
                //g.startCollaborationMeetingWithFileSelection();
            }
        });
    }

    public void StartLocalGroupServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //gc = Context.getInstance().getGc();
                selectedFileName = Context.getInstance().getSelectedFileName();

                String content = "";
                try {
                    System.out.println("Getting File from: " + selectedFileName);
                    System.out.println("File Content: ");
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~start-content~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    content = new String(Files.readAllBytes(Paths.get(selectedFileName))); // current text in the file, send to client who connects
                    System.out.println(content);
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~end-content~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    gc.SetText(content); // update myself first with the content
                } catch (IOException ex) {
                    Logger.getLogger(LocalGroupServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (gc.getLocalGroupServerIsOpen()) {  
                    System.out.println(String.format("Waiting for client connection requests (%s Group Server)...", type));
                    try {
                        // For every client connection accepted by the server, spawn a
                        // separate thread to work with the client. The thread closes
                        // when the client explicitly disconnects from the connection
                        // or sends an END message.
                        clientConnection = serverSock.accept();
                    } catch (IOException ex) {
                        Logger.getLogger(LocalServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    int clientPort = clientConnection.getPort();
                    String clientIP = clientConnection.getInetAddress().toString();
                    System.out.println("Connection request accepted from client at IP address " + clientIP + " and port " + clientPort);
                    // get updated content
                    content = gc.GetText();
                    // open thread according to the type of the server , here it is the collaboration server
                    LocalCollaborationGroupServerHandler newConnThread = new LocalCollaborationGroupServerHandler(clientConnection, GID, group_Password, content);
                    newConnThread.start();
                }
                System.out.println("Local collaboration Group Server Closed.");
                gc.stopMeeting();
            }
        });
        thread.start();
    }
}
