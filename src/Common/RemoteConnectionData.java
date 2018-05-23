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

package Common;

/**
 *
 * @author admin
 */
public class RemoteConnectionData {
    private String remote_ip;
    private int remote_port;
    private String remote_ID;
    private String remote_Password;
    private String type;
    private String client_name;
    
    public RemoteConnectionData(String remote_ip, int remote_port, String remote_ID, String remote_Password){
        this.remote_ip = remote_ip;
        this.remote_port = remote_port;
        this.remote_ID = remote_ID;
        this.remote_Password = remote_Password;
        this.type = ""; //no type needed
        this.client_name = "anonymous"; // no name for client
    }
    
    public RemoteConnectionData(String remote_ip, int remote_port, String remote_ID, String remote_Password, String type, String clientName){
        // this overload method is used for group meetings
        this.remote_ip = remote_ip;
        this.remote_port = remote_port;
        this.remote_ID = remote_ID;
        this.remote_Password = remote_Password;
        this.type = type;
        this.client_name = clientName;
    }
        
    public String getRemote_ip() {
        return remote_ip;
    }

    public void setRemote_ip(String remote_ip) {
        this.remote_ip = remote_ip;
    }

    public int getRemote_port() {
        return remote_port;
    }

    public void setRemote_port(int remote_port) {
        this.remote_port = remote_port;
    }

    public String getRemote_ID() {
        return remote_ID;
    }

    public void setRemote_ID(String remote_ID) {
        this.remote_ID = remote_ID;
    }

    public String getRemote_Password() {
        return remote_Password;
    }

    public void setRemote_Password(String remote_Password) {
        this.remote_Password = remote_Password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }  

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    @Override
    public String toString() {
        return "RemoteConnectionData{" + "remote_ip=" + remote_ip + ", remote_port=" + remote_port + ", remote_ID=" + remote_ID + ", remote_Password=" + remote_Password + ", type=" + type + ", client_name=" + client_name + '}';
    }
}
