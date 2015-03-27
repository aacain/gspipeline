/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gspipeline;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author aacain
 */
public class Log {

    private final ServerConnection connection;
    private String logFile;

    Log(ServerConnection connection) {
        this.connection = connection;
    }

    public void initLog() throws IOException, CommandExecutionException {
        //get home directory
        StringBuilder directory = new StringBuilder(connection.executeCommand("echo $HOME").trim());
        if (directory.charAt(directory.length() - 1) != '/') {
            directory.append("/");
        }
        String remoteDirectory = directory.append("gspipeline/").toString();
        String absoluteLogDir = directory.append("log/").toString();
        this.logFile = directory.append("default.log").toString();

        //create necessary files
        StringBuilder bldr = new StringBuilder();
        bldr.append("mkdir -p ").append(remoteDirectory).append("\n");
        bldr.append("mkdir -p ").append(absoluteLogDir).append("\n");
        bldr.append("touch ").append(this.logFile).append("\n");
        connection.executeCommand(bldr.toString());

    }

    public void writeLog(String message) throws IOException, CommandExecutionException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        StringBuilder logMessage = new StringBuilder("##################\n");
        logMessage.append(format.format(new Date())).append("\n");
        logMessage.append(message).append("\n");
        logMessage.append("------------------\n");
        connection.executeCommand("/bin/echo -e \'" + logMessage.toString() + "\' >> " + this.logFile);
    }
}
