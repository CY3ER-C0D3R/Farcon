/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GroupCollaborationPage;
import RemoteControlPage.RemoteClient;
import GroupCollaborationPage.GroupCollaborationPageController;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Client used in communication with the server
 * @author Yuval Stein
 */
public class RemoteGroupClient{
    GroupCollaborationPageController gc;
    
    Socket sock;
    InetAddress serverInetAddr;
    String clientName;
    String clientID;
    ObjectOutputStream ostr;
    ObjectInputStream istr;
    String remote_server_id;
    String remote_server_password;
    String remote_server_ip;
    int remote_server_port;
    boolean vertified_on_server;
    boolean group_running; // is meeting in tact
    
    
    /**
     * Client is made out of two threads - 1.Main thread for regular communication
       with the server. 2. A Thread for getting the images (as byte[] arrays)
       from the server.
     * @param clName client name for future recognition in server
     * @param gc
     * @param RemoteGID
     * @param RemoteGroupPassword
     * @param RemoteGroupIP
     * @param RemoteGroupPort
     */
    public RemoteGroupClient(String clName, String clID, GroupCollaborationPageController gc, String RemoteGID, String RemoteGroupPassword, String RemoteGroupIP, int RemoteGroupPort){
        try {
            clientName = clName;
            clientID = clID;
            vertified_on_server = false;
            group_running = true;
            this.gc = gc;
            this.remote_server_id = RemoteGID;
            this.remote_server_password = RemoteGroupPassword;
            this.remote_server_ip = RemoteGroupIP;
            this.remote_server_port = RemoteGroupPort;
            serverInetAddr = InetAddress.getByName(this.remote_server_ip);
            //serverInetAddr = InetAddress.getByName("172.16.15.140");
            //serverInetAddr = InetAddress.getByName("172.17.20.242");
            
            sock = new Socket(serverInetAddr, this.remote_server_port);
            // get the input and output streams from the socket object
            // to perform read and write operations respectively
            this.ostr = new ObjectOutputStream(sock.getOutputStream());
            //this.gc.setOutputStream(ostr);
            this.istr = new ObjectInputStream(sock.getInputStream());
            System.out.println("Setting Socket list : ");
            System.out.println(this.ostr);
            gc.addToSocketList(sock, this.ostr);
            System.out.println("Connected to server on "
            +serverInetAddr.getHostName()+
            " listening to port "+sock.getPort());
            
            IdentifyOnServer();
            
            if (vertified_on_server) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (group_running) {
                                System.out.println(group_running);
                                int n = istr.read();
                                System.out.println("Remote" + " " + n);
                                byte[] b = new byte[n];
                                istr.read(b);
                                String s = new String(b);
                                UpdateEditor(s);
                            }
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                            System.out.println(group_running);

                        }
                    }
                };
                thread.start();
            }
        }
        catch (Exception ex) {
            ex.getMessage();
        }
    }
    
    public void IdentifyOnServer(){
        String msg = "GID="+this.remote_server_id+";G_Password="+this.remote_server_password+";Client_Name="+this.clientName+";Client_ID="+this.clientID+";";
        try {
            ostr.writeObject(msg);
            Object reply = istr.readObject();
            String[] st = null;
            if(reply instanceof String){
                String status = (String) reply;
                st = status.split(";");
            }
            
            if(st!=null && st[0].equals("Vertified")){
                //server vertified password, and now the communication can begin
                this.vertified_on_server = true;
                System.out.println("Client has connected Successfuly to Remote Host (Vertified).");
                //update file editor with the initial content as sent from the server
                gc.SetText(st[1]);
            }
            else{
                System.out.println("Client hasn't connected to Remote Host (Unvertified).");
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stop(){
        // stop listening on thread, group meeting is over
        System.out.println("Stopping remote group client...");
        this.group_running = false;
        try {
            this.sock.close();
        } catch (IOException ex) {
            Logger.getLogger(RemoteGroupClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//    public void run() {
//        try {
//            while (true) {
//                int n = this.istr.read();
//                System.out.println("Remote" + " " + n);
//                byte[] b = new byte[n];
//                istr.read(b);
//                String s = new String(b);
//                UpdateEditor(s);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    
    public void UpdateData(String content) {
        try {
            System.out.println("Here in updateData in client with content:");
            System.out.println(content);
            ostr.write(content.length());
            ostr.write(content.getBytes());
            ostr.flush();
        } catch (IOException ex) {
            Logger.getLogger(RemoteGroupClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void UpdateEditor(String content) {
        System.out.println("Here in updateEditor in client with content:");
        System.out.println(content);
        
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
        
        if (operation.equals("client-disconnected")){
            System.out.println(cont);
            gc.DisplayNotification("Client Disconnected", String.format("Group Client '%s' disconnected from Meeting.", cont));
        }
        else if (operation.equals("ins")) {
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
