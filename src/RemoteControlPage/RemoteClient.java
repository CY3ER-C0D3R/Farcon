/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RemoteControlPage;
import Common.Context;
import Common.RemoteGUIInterface;
import static RemoteControlPage.LocalServerHandler.shouldExit;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Client used in communication with the server
 * @author Yuval Stein
 */
public class RemoteClient {
    Socket sock;
    InetAddress serverInetAddr;
    String clientName;
    ObjectOutputStream ostr;
    ObjectInputStream istr;
    String remote_server_id;
    String remote_server_password;
    String remote_server_ip;
    int remote_server_port;
    boolean vertified_on_server;
    RemoteControlPaneFXMLController ri;
    
    
    /**
     * Client is made out of two threads - 1.Main thread for regular communication
       with the server. 2. A Thread for getting the images (as byte[] arrays)
       from the server.
     * @param clName client name for future recognition in server
     * @param ri
     */
    public RemoteClient(String clName, RemoteGUIInterface ri1, String RemoteID, String RemotePassword, String RemoteIP, int RemotePort) {
        try {
            this.ri = (RemoteControlPaneFXMLController) ri1;
            clientName = clName;
            this.remote_server_id = RemoteID;
            this.remote_server_password = RemotePassword;
            this.remote_server_ip = RemoteIP;
            this.remote_server_port = RemotePort;
            serverInetAddr = InetAddress.getByName(this.remote_server_ip);
            
            sock = new Socket(serverInetAddr, this.remote_server_port);
            System.out.println("Connected to server on "
            +serverInetAddr.getHostName()+
            " listening to port "+sock.getPort());
            // get the input and output streams from the socket object
            // to perform read and write operations respectively
            this.ostr = new ObjectOutputStream(sock.getOutputStream());
            this.istr = new ObjectInputStream(sock.getInputStream());
            IdentifyOnServer();
            System.out.println("HERE, with: " + vertified_on_server);
            ri.setConnected(vertified_on_server);
            
            if (vertified_on_server) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (true) {
                            try {
                                Object o = istr.readObject();

                                if (o instanceof byte[]) {
                                    InputStream in = new ByteArrayInputStream((byte[]) o);
                                    BufferedImage b = ImageIO.read(in);
                                    ri.UpdateScreen(b);
                                }
                            } catch (IOException | ClassNotFoundException ex) {
                                ri.DisplayNotification("Host Disconnected", String.format("Session ended with %s.", remote_server_id));
                                ri.exitRemoteControl();
                                ////// Yuval: change the following....
                                Context.getInstance().UpdateStatusBar("Session ended", true);
                                break;
                            }
                        }
                    }
                };
                thread.start();
                System.out.println("Here with client name: " + clientName);
                SendCommand("Name=" + clientName + ";");
            }
            else{
                //update display - Authentication Failed, change after 5 seconds
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Context.getInstance().UpdateStatusBar("Authentication Failed", true);
                    }
                });
            }
        }
        catch (Exception ex) {
            ex.getMessage();
        }
    }
    
    public void IdentifyOnServer(){
        String msg = "ID="+this.remote_server_id+";RC_Password="+this.remote_server_password;
        try {
            ostr.writeObject(msg);
            Object reply = istr.readObject();
            if(reply.equals("Vertified")){
                //server vertified password, and now the communication can begin
                this.vertified_on_server = true;
                System.out.println("Client has connected Successfuly to Remote Host (Vertified).");
            }
            else{
                this.vertified_on_server = false;
                System.out.println("Client hasn't connected to Remote Host (Unvertified).");
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param cmd command to send to the server. Can be any object that implements
     * Serialization. 
     */
    public void SendCommand(Object cmd){
        if(vertified_on_server) {
            try {
                ostr.writeObject(cmd);
                RecvReply();
            } catch (IOException ex) {
                synchronized (LocalServerHandler.class) {
                    if (!shouldExit && vertified_on_server) {
                        shouldExit = true;
                        System.out.println("Client disconnected");
                        try
                        {
                            sock.shutdownInput();
                            sock.shutdownOutput();
                            sock.close();
                        }
                        catch(Exception ex2)
                        {
                        }
                        ri.DisplayNotification("Host Disconnected", String.format("Session ended with %s.", remote_server_id));
                        ri.exitRemoteControl();
                        ////// Yuval: change the following....
                        Context.getInstance().UpdateStatusBar("Session ended", true);
                    }
                }
            }
        }
    }
    
    /**
     * Function receives replies from the server to make sure connection is still
     * ongoing. For future error management
     */
    public void RecvReply(){
        if (vertified_on_server) {
            String inMsg = "";
            if (sock.isClosed()) {
                System.out.println("Server closed the connection...");
            } else if (sock.isInputShutdown()) {
                System.out.println("Input stream is down...");
            } else if (sock.isOutputShutdown()) {
                System.out.println("Output stream is down ...");
            }
        }
    }
    
    public void CloseSocket(){
        try {
            this.sock.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
