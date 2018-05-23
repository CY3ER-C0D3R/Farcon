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
import Main.main;
import Main.FXMLDocumentController;
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
import com.sun.glass.ui.Application;
//import com.sun.glass.ui.Pixels;
import com.sun.glass.ui.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.stage.Screen;
import javax.imageio.ImageIO;
import com.sun.glass.ui.Application;
import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.FXRobotFactory;
import com.sun.javafx.robot.FXRobotImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;



/**
 * Client Handler Class, handles each client individually, handles the commands
 * from the client and returns important data as well as the screen image.
 * @author admin
 */
public class LocalServerHandler extends Thread {
    
    final static public int MOUSE_LEFT_BTN   = 1;
    final static public int MOUSE_RIGHT_BTN  = 2;
    final static public int MOUSE_MIDDLE_BTN = 4;
    
    private FXMLDocumentController f;

    Socket sock;
    private String server_ID;
    private String server_RC_Password;
    private ObjectOutputStream ostr;
    private ObjectInputStream istr;
    int clientPort;
    String clientIP;
    String clientName;
    Robot robot;
    GraphicsDevice gd;
    Thread thread;
    boolean computerIsOn;
    private boolean clientVertified;
    
    static boolean shouldExit = false;
    
    /**
     * Handler Function is the main function in Handler class and is responsible
     * mainly for sending the image to the client every 100 milliseconds.
     * @param newSocket Socket Object for each client
     */
    public LocalServerHandler(FXMLDocumentController f, Socket newSocket, String server_ID, String server_RC_Password) {
        this.f = f;
        sock = newSocket;
        this.server_ID = server_ID;
        this.server_RC_Password = server_RC_Password;
        computerIsOn = true;
        clientVertified = false;
        try {
            // get the input and output streams from the socket object
            // to perform read and write operations respectively
            this.ostr = new ObjectOutputStream(sock.getOutputStream());
            this.istr = new ObjectInputStream(sock.getInputStream());
            clientPort = sock.getPort();
            clientIP = sock.getInetAddress().toString();
            //vertify client (make sure password is correct before communication
            VertifyClient();
            if (clientVertified) {
                //update display - all is good
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Context.getInstance().UpdateStatusBar("Connected", true);
                        //f.updateStatusBar("Connected", true);
                    }
                });
                robot = main.getRobot();
                gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                System.out.println("Ready to communicate with client at IP address " + clientIP + " and port " + clientPort);
                thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (computerIsOn) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            BufferedImage b = Capture();

                            try {
                                ByteArrayOutputStream os = new ByteArrayOutputStream();
                                ImageIO.write(b, "jpg", os);
                                os.flush();
                                byte[] imgBytes = os.toByteArray();

                                ostr.writeObject(imgBytes);
                            } catch (IOException ex) {
                               System.out.println("Client disconnected");
                               //f.DisplayNotification("Client Disconnected", String.format("Client (%s,%d) disconnected from server.", clientIP, clientPort));
                               break;
                            }
                        }
                    }
                };
                thread.start();
            }
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void VertifyClient(){
        // client connecting must send a first string of "ID=...;RC_Password="
        System.out.println("Verifying Client");
        try {
            Object msg = istr.readObject();
            if (msg instanceof String) {
                String inMsg = (String) msg;
                System.out.println("String recieved: " + inMsg);
                String id, rc_password;
                StringTokenizer inStrTok = new StringTokenizer(inMsg, ";", false);
                String msgCode = inStrTok.nextToken();
                id = msgCode.split("=")[1];
                msgCode = inStrTok.nextToken();
                rc_password = msgCode.split("=")[1];
                if(id.equals(this.server_ID) && rc_password.equals(this.server_RC_Password)) //client is vertified
                {
                    this.clientVertified = true;
                    System.out.println("Client Vertified successfuly on Local Server");
                    // return a successful message to the client
                    this.ostr.writeObject("Vertified");
                }
                else // wrong detail entered by client (incorrect password)
                {
                    this.clientVertified = false;
                    System.out.println("Client has not been Vertified on Local Server (Wrong Password Entered).");
                    // return a unsuccessful message to the client
                    this.ostr.writeObject("Not Vertified");

                    //update display - Authentication Failed, change after 5 seconds
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Context.getInstance().UpdateStatusBar("Authentication Failed", true);
                        }
                    });
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * It is this run method of the individual thread object that
     * performs the actual communication with the client socket.
     */
    public void run() {
        Object msg;
        String inMsg = "";
        do {
            try {
                msg = istr.readObject();
                if (msg instanceof String) {
                    inMsg = (String) msg;
                    System.out.println("Message Recieved: " + inMsg);
                    StringTokenizer inStrTok = new StringTokenizer(inMsg, ";",
                            false);
                    String msgCode = inStrTok.nextToken();
                    if (msgCode.startsWith("Name=")) {
                        this.clientName = msgCode.split("=")[1];
                    } else if (msgCode.equals("Event=Mouse")) {
                        HandleMouse(inStrTok);
                        //ostr.writeObject("Event=ok"); //todo
                    } else if (msgCode.equals("Event=Key")){
                        HandleKey(inStrTok);
                    } else if (msgCode.equals("BufferImage")) {
                        /*ostr.writeObject("Event=sendingImg");
                    ostr.writeObject(Capture());
                    ostr.writeObject("Event=ok"); //todo*/
                    } else if(msgCode.equals("Shutdown")){
                        ShutDownComputer();
                    }
                } else { //keyEvent object
                    /*if (msg instanceof Integer)
                    {
                        int e = (int)msg;
                        HandleKeyBoard(e);
                    }*/
                }
            }
            catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                System.exit(-1);
            }
            catch (IOException ex) {
                synchronized (LocalServerHandler.class) {
                    if (!shouldExit && clientVertified) {
                        shouldExit = true;
                        System.out.println("Client disconnected");
                        f.DisplayNotification("Client Disconnected", String.format("Client %s disconnected from server.", clientName));
                        break;
                    }
                }
            }
        } while (!inMsg.equals("END") && computerIsOn);
        System.out.println("Client requested termination from IP address "
                             +clientIP+" and port "+clientPort);
        try {
            sock.shutdownInput();
            sock.shutdownOutput();
            sock.close();
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     *
     * @param p point which represents a x or y coordinate
     * @param isX boolean variable to determine if dot is x or y
     * @return calculation of the new location of the dot according to the size
     * of the screen
     */
    public int CalculateRelativePosition(double p, boolean isX){
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        if(isX) //dot is on the x scale
            return (int)(p * width);
        //dot is on the y scale
        return (int) (p * height);
    }
    
    //protected abstract void getScreenCapture(int x, int y, int width, int height, int[] data);
    
//    public final Image getScreenCapture(int x, int y, int width, int height, boolean scaleToFit) {
//        Application.checkEventThread();
//        Screen primaryScreen = Screen.getPrimary();
//        double outputScaleX = primaryScreen.getVisualBounds().getWidth();
//        double outputScaleY = primaryScreen.getVisualBounds().getHeight();
//        int data[];
//        int dw, dh;
//        if (outputScaleX == 1.0f && outputScaleY == 1.0f) {
//            // No scaling with be necessary regardless of if "scaleToFit" is set or not.
//            data = new int[width * height];
//            getScreenCapture(x, y, width, height, data);
//            dw = width;
//            dh = height;
//        } else {
//            // Compute the absolute pixel bounds that the requested size will fill given
//            // the display's scale.
//            int pminx = (int) Math.floor(x * outputScaleX);
//            int pminy = (int) Math.floor(y * outputScaleY);
//            int pmaxx = (int) Math.ceil((x + width) * outputScaleX);
//            int pmaxy = (int) Math.ceil((y + height) * outputScaleY);
//            int pwidth = pmaxx - pminx;
//            int pheight = pmaxy - pminy;
//            int tmpdata[] = new int[pwidth * pheight];
//            getScreenCapture(pminx, pminy, pwidth, pheight, tmpdata);
//            if (!scaleToFit) {
//                data = tmpdata;
//                dw = pwidth;
//                dh = pheight;
//            } else {
//                // We must resize the image to fit the requested bounds. This means
//                // resizing the pixel data array which we accomplish using bilinear (?)
//                // interpolation.
//                data = new int[width * height];
//                int index = 0;
//                for (int iy = 0; iy < height; iy++) {
//                    double rely = ((y + iy + 0.5f) * outputScaleY) - (pminy + 0.5f);
//                    int irely = (int) Math.floor(rely);
//                    int fracty = (int) ((rely - irely) * 256);
//                    for (int ix = 0; ix < width; ix++) {
//                        double relx = ((x + ix + 0.5f) * outputScaleX) - (pminx + 0.5f);
//                        int irelx = (int) Math.floor(relx);
//                        int fractx = (int) ((relx - irelx) * 256);
//                        //data[index++]
//                           //     = GlassRobot.interp(tmpdata, irelx, irely, pwidth, pheight, fractx, fracty);
//                    }
//                }
//                dw = width;
//                dh = height;
//            }
//        }
//        return null;
//    }

//    private Image convertFromGlassPixels(Pixels glassPixels) {
//        int width = glassPixels.getWidth();
//        int height = glassPixels.getHeight();
//        WritableImage image = new WritableImage(width, height);
//
//        int bytesPerComponent = glassPixels.getBytesPerComponent();
//        if (bytesPerComponent == INT_BUFFER_BYTES_PER_COMPONENT) {
//            IntBuffer intBuffer = (IntBuffer) glassPixels.getPixels();
//            writeIntBufferToImage(intBuffer, image);
//        }
//
//        return image;
//    }
    
    /**
     *
     * @return BufferedImage Object of the current screen display
     */
    public BufferedImage Capture(){
        try{
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle r = new Rectangle(size);
            BufferedImage screenImg = new java.awt.Robot().createScreenCapture(r);
            //BufferedImage screenImg = (BufferedImage)getScreenCapture(r.x, r.y, r.width, r.height, true);
            //BufferedImage screenImg = robot.createScreenCapture(new Rectangle(size));
            return screenImg;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     *
     * @param press if true simulates a mouse left press
     * @param release if true simulates a mouse left release
     */
    public void leftClick(boolean press, boolean release) {
        //press and release should both be true if mouse was clicked
        //change these booleans according to desired action
        
//        FXRobot fxRobot = FXRobotFactory.createRobot(main.getScene());
//        fxRobot.setAutoWaitForIdle(false);
//        fxRobot.mouseClick(MouseButton.PRIMARY);
        
        System.out.println("HERE pressing left button data:");
        System.out.println(String.format("%s %s %d", press, release, MOUSE_LEFT_BTN));
        if(press)
            robot.mousePress(MOUSE_LEFT_BTN);
        if(release)
            robot.mouseRelease(MOUSE_LEFT_BTN);
    }

    /**
     *
     * @param press if true simulates a mouse middle press
     * @param release if true simulates a mouse middle release
     */
    public void middleClick(boolean press, boolean release) {
        //press and release should both be true if mouse was clicked
        //change these booleans according to desired action
        
//        if(press)
//            robot.mousePress(InputEvent.BUTTON2_MASK);
//        if(release)
//            robot.mouseRelease(InputEvent.BUTTON2_MASK);
//        
        if(press)
            robot.mousePress(MOUSE_MIDDLE_BTN);
        if(release)
            robot.mouseRelease(MOUSE_MIDDLE_BTN);
    }

    /**
     *
     * @param press if true simulates a mouse right press
     * @param release if true simulates a mouse right release
     */
    public void rightClick(boolean press, boolean release) {
        //press and release should both be true if mouse was clicked
        //change these booleans according to desired action

//        if(press)
//            robot.mousePress(InputEvent.BUTTON3_MASK);
//        if(release)
//            robot.mouseRelease(InputEvent.BUTTON3_MASK);
        
        if(press)
            robot.mousePress(MOUSE_RIGHT_BTN);
        if(release)
            robot.mouseRelease(MOUSE_RIGHT_BTN);
    }

    /**
     * Function takes care of all mouse related actions
     * @param inStrTok generator which gives the next piece of the string each time
     */
    public void HandleMouse(StringTokenizer inStrTok){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String type = inStrTok.nextToken().split("=")[1];
                    String parameters = inStrTok.nextToken().split("=")[1];
                    double[] position = new double[2];
                    String place = (inStrTok.nextToken().split("=")[1]); //place is "x:y"
                    position[0] = Double.parseDouble(place.split(":")[0]);
                    position[1] = Double.parseDouble(place.split(":")[1]);
                    int x = CalculateRelativePosition(position[0], true);
                    int y = CalculateRelativePosition(position[1], false);

                    //show cursor at position
                    //PointerInfo a = MouseInfo.getPointerInfo();
                    //Point pt = a.getLocation();
                    //Cursor.setLocation (pt.x, pt.y);
                    if ("Clicked".equals(type)) {
                        robot.mouseMove(x, y);
                        if ("Left".equals(parameters)) //left mouse button clicked
                        {
                            leftClick(true, true);
                        } else if ("Right".equals(parameters)) //right mouse button clicked
                        {
                            rightClick(true, true);
                        } else if ("Middle".equals(parameters)) //middle mouse button clicked
                        {
                            middleClick(true, true);
                        } else {
                            //todo, error managment
                        }
                    } else if ("Pressed".equals(type)) {
                        System.out.println(String.format("Mouse Pressing at %d %d",x,y));
                        robot.mouseMove(x, y);
                        if ("Left".equals(parameters)) //left mouse button Pressed
                        {
                            leftClick(true, false);
                            //robot.mousePress(1);
                        } else if ("Right".equals(parameters)) //right mouse button Pressed
                        {
                            rightClick(true, false);
                        } else if ("Middle".equals(parameters)) //middle mouse button Pressed
                        {
                            middleClick(true, false);
                        } else {
                            //todo, error managment
                        }
                    } else if ("Released".equals(type)) {
                        robot.mouseMove(x, y);
                        if ("Left".equals(parameters)) //left mouse button Released
                        {
                            System.out.println("Here mouse released");
                            leftClick(false, true);
                            //robot.mouseRelease(1);
                        } else if ("Right".equals(parameters)) //right mouse button Released
                        {
                            rightClick(false, true);
                        } else if ("Middle".equals(parameters)) //middle mouse button Released
                        {
                            middleClick(false, true);
                        } else {
                            //todo, error managment
                        }
                    } else if ("Dragged".equals(type)) {
                        if ("Left".equals(parameters)) //left mouse button Pressed
                        {
                            leftClick(true, false);
                        } else if ("Right".equals(parameters)) //right mouse button Pressed
                        {
                            rightClick(true, false);
                        } else if ("Middle".equals(parameters)) //middle mouse button Pressed
                        {
                            middleClick(true, false);
                        } else {
                            //todo, error managment
                        }
                        robot.mouseMove(x, y);
                    } else if ("Moved".equals(type)) {
                        robot.mouseMove(x, y);
                    } else if ("WheelMoved".equals(type)) {
                        robot.mouseMove(x, y);
                        robot.mouseWheel(Integer.parseInt(parameters));
                    } else //no event happened, error management
                    {
                    }
                } catch (Exception ex) {
                    Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void HandleKey(StringTokenizer inStrTok) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Here in handle key:");
                    System.out.println(inStrTok);
                    
                    String type = inStrTok.nextToken().split("=")[1];  // pressed or released
                    int code = Integer.parseInt(inStrTok.nextToken().split("=")[1]);  // key code 
                    // the rest are modifiers
                    boolean isAltDown = Boolean.parseBoolean(inStrTok.nextToken().split("=")[1]);
                    boolean isCtrlDown = Boolean.parseBoolean(inStrTok.nextToken().split("=")[1]);
                    boolean isMetaDown = Boolean.parseBoolean(inStrTok.nextToken().split("=")[1]);
                    boolean isShiftDown = Boolean.parseBoolean(inStrTok.nextToken().split("=")[1]);
                    
                    // press all the modifiers before pressing the key 
                    if(isAltDown)
                        robot.keyPress(KeyCode.ALT.impl_getCode());
                    if(isCtrlDown)
                        robot.keyPress(KeyCode.CONTROL.impl_getCode());
                    if(isMetaDown)
                        robot.keyPress(KeyCode.META.impl_getCode());
                    if(isShiftDown)
                        robot.keyPress(KeyCode.SHIFT.impl_getCode());
                    
                    // press or release the key
                    if(type.equals("Pressed"))
                        robot.keyPress(code);
                    else // Release
                        robot.keyRelease(code);
                    
                    // release all modifiers
                    robot.keyRelease(KeyCode.ALT.impl_getCode());
                    robot.keyRelease(KeyCode.CONTROL.impl_getCode());
                    robot.keyRelease(KeyCode.META.impl_getCode());
                    robot.keyRelease(KeyCode.SHIFT.impl_getCode());
                    
                } catch (Exception ex) {
                    Logger.getLogger(LocalServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    } 

    /**
     * Function shuts down the computer if requested
     * @throws IOException
     */
    public void ShutDownComputer() throws IOException {
        String shutdownCommand = "";
        String operatingSystem = System.getProperty("os.name");
        if ("Linux".equals(operatingSystem) || "Mac OS X".equals(operatingSystem)) {
            shutdownCommand = "shutdown -h now";
        } else if (operatingSystem.toLowerCase().contains("Windows".toLowerCase())) {
            shutdownCommand = "shutdown.exe -s -t 0";
        } else {
            throw new RuntimeException("Unsupported operating system.");
        }
        computerIsOn = false;

        Runtime.getRuntime().exec(shutdownCommand);
        System.exit(0);
    }
}