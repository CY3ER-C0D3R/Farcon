/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupCollaborationPage;
import RemoteControlPage.LocalServer;
import RemoteControlPage.LocalServerHandler;
import Common.Context;
import java.awt.*;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Client Handler Class, handles each client individually, handles the commands
 * from the client and returns important data as well as the screen image.
 * @author admin
 */
public class LocalCollaborationGroupServerHandler extends Thread {
    private GroupCollaborationPageController gc;

    private String initial_content;
    
    Socket sock;
    private String server_GID;
    private String server_group_password;
    private ObjectOutputStream ostr;
    private ObjectInputStream istr;
    int clientPort;
    String clientIP;
    String clientName;
    String clientID;
    Thread thread;
    boolean computerIsOn;
    private boolean clientVertified;
    
    static boolean shouldExit = false;

    /**
     * Handler Function is the main function in Handler class and is responsible
     * mainly for sending the image to the client every 100 milliseconds.
     * @param newSocket Socket Object for each client
     * @param server_GID
     * @param server_group_password
     */
    public LocalCollaborationGroupServerHandler(Socket newSocket, String server_GID, String server_group_password, String initial_content) {
        this.gc = Context.getInstance().getGc();
        this.initial_content = initial_content;
        
        this.sock = newSocket;
        this.server_GID = server_GID;
        this.server_group_password = server_group_password;
        computerIsOn = true;
        clientVertified = false;
        try {
            // get the input and output streams from the socket object
            // to perform read and write operations respectively
            this.ostr = new ObjectOutputStream(sock.getOutputStream());
            //gc.setOutputStream(ostr);
            this.istr = new ObjectInputStream(sock.getInputStream());
            clientPort = sock.getPort();
            clientIP = sock.getInetAddress().toString();
            // add to client hashmap
            this.gc.addToSocketList(sock, ostr);
            //vertify client (make sure password is correct before communication
            //update display - "Awaiting Authentication"
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    // todo - create status bar in gd.
                    //gd.updateStatusBar("Awaiting Authentication...", false);
                    //Context.getInstance().DisplayNotification("Awaiting Authentication", "Client is connecting to your collaboration session, please wait.."); //delete this later, status bar is enough
                }
            });
            VertifyClient();
            if (clientVertified) {
                //update display - all is good
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // todo - create status bar in gd.
                        //gd.updateStatusBar("Connected", true);
                        Context.getInstance().DisplayNotification("Client Connect", "Client has connecting to your collaboration session successfully."); //delete this later, status bar is enough
                    }
                });
                // send client the initial_content so he can update his gui
                System.out.println("Ready to communicate with client at IP address " + clientIP + " and port " + clientPort);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (computerIsOn) {
                            try {
                                System.out.println("here");
                                int n = istr.read();
                                System.out.println("Local" + " " + n);
                                byte[] b = new byte[n];
                                istr.read(b);
                                String s = new String(b);
                                UpdateEditor(s);

//                                //update window for clients
//                                //update local window
//                                String content = new String(Files.readAllBytes(Paths.get(sharedFilename)));
//                                FE.SetText(content);
//                                //update remote window
//                                ostr.write(content.length());
//                                ostr.write(content.getBytes());
                            } catch (Exception ex) {
                                synchronized (LocalCollaborationGroupServerHandler.class) {
                                    if (!shouldExit) {
                                        shouldExit = true;
                                        // delete client from group clients list
                                        gc.removeFromSocketList(sock);
                                        // display message that client disconnected
                                        gc.DisplayNotification("Client Disconnected", String.format("Group Client '%s' disconnected from Meeting.", clientName));
                                        // update other clients that this client disconnected
                                        gc.UpdateData(String.format("client-disconnected;%s;_;",clientName), sock);
                                        System.out.println(ex.getMessage());
                                        System.out.println(gc.clientSockets);
                                        // update master server that client disconnected from the group meeting
                                        JSONObject jsonObject = new JSONObject();
                                        JSONObject parameters = new JSONObject();
                                        JSONObject clientData = new JSONObject();
                                        String line = "";
                                        try {
                                            jsonObject.put("Action", "update-group-data");
                                            parameters.put("GID", server_GID);
                                            parameters.put("remove-clientID", clientID);  
                                            // no client to add, clientData=null
                                            parameters.put("add-clientData", clientData);
                                            jsonObject.put("Parameters", parameters);
                                            //send data to master server
                                            System.out.println(jsonObject);
                                            gc.SendMessage(jsonObject);
                                        } catch (JSONException e) {
                                            Logger.getLogger(LocalServer.class.getName()).log(Level.SEVERE, null, e);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                });
                thread.start();
            }
            //update display - Authentication Failed, change after 5 seconds
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    // todo - create status bar in gd.
                    //gd.updateStatusBar("Authentication Failed", true);
                    //Context.getInstance().DisplayNotification("Authentication Failed", "Client couldn't connect to your collaboration session."); //delete this later, status bar is enough
                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void VertifyClient(){
        // client connecting must send a first string of "GID=...;G_Password=;Client_Name=;Client_ID=;"
        System.out.println("Verifying Client");
        try {
            Object msg = istr.readObject();
            if (msg instanceof String) {
                String inMsg = (String) msg;
                System.out.println("String recieved: " + inMsg);
                String gid, group_password;
                StringTokenizer inStrTok = new StringTokenizer(inMsg, ";", false);
                String msgCode = inStrTok.nextToken();
                gid = msgCode.split("=")[1];
                msgCode = inStrTok.nextToken();
                group_password = msgCode.split("=")[1];
                msgCode = inStrTok.nextToken();
                clientName = msgCode.split("=")[1];
                msgCode = inStrTok.nextToken();
                clientID = msgCode.split("=")[1];
                System.out.println("Client Name connected is: " + clientName);
                System.out.println("GID Recieved: " + gid);
                System.out.println("My ID: " + this.server_GID);
                System.out.println(gid.equals(this.server_GID));
                System.out.println("Group Password Recieved: " + group_password);
                System.out.println("My Remote Control Password: " + this.server_group_password);
                System.out.println(group_password.equals(this.server_group_password));
                if(gid.equals(this.server_GID) && group_password.equals(this.server_group_password)) //client is vertified
                {
                    this.clientVertified = true;
                    System.out.println("Client Vertified successfuly on Local Collaboration Server");
                    // return a successful message to the client
                    this.ostr.writeObject("Vertified;"+this.initial_content);
                    // update the master server new client joined group meeting
                    JSONObject jsonObject = new JSONObject();
                    JSONObject parameters = new JSONObject();
                    JSONObject clientData = new JSONObject();
                    String line = "";
                    try {
                        jsonObject.put("Action", "update-group-data");
                        parameters.put("GID", server_GID);
                        parameters.put("remove-clientID", "");  // no client to remove
                        clientData.put("Username", clientName);
                        clientData.put("IP", clientIP);
                        clientData.put("Port", clientPort);
                        clientData.put("ID", clientID);
                        clientData.put("RC_Password", "");  // password here is irrelavant
                        parameters.put("add-clientData", clientData);
                        jsonObject.put("Parameters", parameters);
                        //send data to master server
                        System.out.println(jsonObject);
                        gc.SendMessage(jsonObject);
                    } catch (JSONException ex) {
                        Logger.getLogger(LocalServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
                else // wrong detail entered by client (incorrect password)
                {
                    this.clientVertified = false;
                    System.out.println("Client has not been Vertified on Local Server (Wrong Password Entered).");
                    // return a unsuccessful message to the client
                    this.ostr.writeObject("Not Vertified;");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
    public void UpdateData(String content) {
        try {
            ostr.write(content.length());
            ostr.write(content.getBytes());
            ostr.flush();
        } catch (IOException ex) {
            Logger.getLogger(RemoteGroupClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void UpdateEditor(String content) {
        // update all other clients as well, except for the client who made the 
        // changes
        this.gc.UpdateData(content, this.sock);
        String[] st = content.split(";");
        String operation = st[0];
        String cont = st[1];
        String position = st[2];
        //String[] position = new String[2];
        // position[0] = st[2]; //row
        // position[1] = st[3]; //col
        System.out.println(String.format("operation: %s cont: %s", st[0], st[1]));
        //System.out.println(String.format("row: %s col: %s", st[2], st[3]));
        System.out.println(operation.equals("ins"));
        
//                System.out.println(String.format("operation: %s cont: %s", st[0], st[1]));
//                System.out.println(String.format("row: %s col: %s", st[2], st[3]));
//                System.out.println(operation.equals("ins"));

        if (operation.equals("ins")) {
            this.gc.AddChar(cont, position);
        } else {
            System.out.println("deleting");
            System.out.println(cont.equals(""));
            //System.out.println(st[2]);
            //System.out.println(st[3]);
            this.gc.DelChar(cont, position);
        }
    }
}