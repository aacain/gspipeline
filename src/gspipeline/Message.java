/*
 * Shows messages to user.
 */

package gspipeline;

import javax.swing.JOptionPane;

/**
 *
 * @author aacain
 */
public class Message {
    
    /**Show a JOptionPane displaying error message.
     * 
     * @param message 
     */
    public static void error(String message){
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show a JOptionPane displaying error message.
     * @param message 
     */
    public static void warning(String message){
        JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

}
