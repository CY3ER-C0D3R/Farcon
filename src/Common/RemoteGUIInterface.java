/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Common;

import java.awt.image.BufferedImage;

/**
 *
 * @author admin
 */
public interface RemoteGUIInterface {
    void setConnected(boolean connected);
    void UpdateScreen(BufferedImage b);
}
