/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gspipeline;

/**
 *
 * @author aacain
 */
public class CommandExecutionException extends Throwable{

    public CommandExecutionException(String message) {
        super(message);
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecutionException(Throwable cause) {
        super(cause);
    }

}
