/**
 *
 */
package gspipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

/**
 *
 * @author aacain
 */
public class ServerConnection {

    SSHClient client;
    String host;
    String username;
    String password;
    Log log;
    static ServerConnection instance;
    

    public ServerConnection(SSHClient client) {
        this.client = client;
    }

    /**
     * Creates a connection and stores it as a static variable. To get this
     * connection use the {@link #getCurrentConnection()}. For more advanced
     * connection types, create and connect to a new SSHClient
     * {@link net.schmizz.sshj.SSHClient}, establish that connection {@link net.schmizz.sshj.SSHClient#connect(java.net.InetAddress)
     * }, and set the current instance {@link #setCurrentInstance(gspipeline.ServerConnection)
     * }.
     *
     * Make sure you close the connection when finished {@link #disconnect() }.
     *
     * @param host
     * @param username
     * @param password
     * @return the current connection or null
     * @throws IOException
     */
    public static ServerConnection createConnection(String host, String username, String password) throws IOException {
        try {
            SSHClient ssh = createSSH(host, username, password);
            ServerConnection connection = new ServerConnection(ssh);
            startLog(connection);
            setCurrentInstance(connection);

        } catch (IOException ex) {
            setCurrentInstance(null);
            Message.error("Could not create connection.\n" + ex.toString());
            throw ex;
        } catch (CommandExecutionException ex) {
            setCurrentInstance(null);
            Message.error("Could not create log files.\n" + ex.toString());
            throw new IOException(ex);
        }
        return getCurrentConnection();
    }
    
    private static Log startLog(ServerConnection connection) throws IOException, CommandExecutionException{
        Log log = new Log(connection);
        log.initLog();
        connection.setLog(log);
        return log;
    }

    /**
     * Sets the current connection.
     *
     * @param connection
     */
    public static void setCurrentInstance(ServerConnection connection) {
        instance = connection;
    }

    /**
     * Gets the current connection. Make sure you close the connection when
     * finished {@link #disconnect() }.
     *
     * @return the current connection or null.
     */
    public static ServerConnection getCurrentConnection() {
        return instance;
    }

    /**
     * Disconnects the connection.
     *
     * @throws IOException
     */
    public void disconnect() throws IOException {
        client.disconnect();
    }

    /**
     *
     * @param host
     * @param username
     * @param password
     * @return
     * @throws IOException
     */
    static SSHClient createSSH(String host, String username, String password) throws IOException {
        final SSHClient ssh = new SSHClient();
        try {
            ssh.getTransport().setHeartbeatInterval(10);
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(host);
            ssh.authPassword(username, password);//authPublickey(System.getProperty("aacain"));
            return ssh;
        } catch (IOException ex) {
            ssh.disconnect();
            throw ex;
        }
    }

    String executeCommand(String command) throws IOException, CommandExecutionException {
        final Session session = client.startSession();
        String result = null;
        String error = null;
        try {
            final Session.Command cmd = session.exec(command);
            result = IOUtils.readFully(cmd.getInputStream()).toString();
            error = IOUtils.readFully(cmd.getErrorStream()).toString();
            cmd.join(5, TimeUnit.SECONDS);
            if(cmd.getExitStatus() != 0){
                Message.error("Could not execute command.\n" + error);
                throw new CommandExecutionException(error);
            }
            System.out.println(result);
            System.err.println(error);
            System.out.println("Exit status: " + cmd.getExitStatus());
        } catch (Exception ex) {
            Message.error("Could not execute command.\n" + ex.toString());
        } finally {
            session.close();
        }

        return result;
    }

    /**
     * Creates a remote file with the given contents.
     *
     * @param filename
     * @param contents
     * @return
     * @throws IOException
     */
    public String createRemoteFile(String filename, String contents) throws IOException, CommandExecutionException {
        StringBuilder command = new StringBuilder();
        command.append("/bin/echo -e \'").append(contents).append("\' > ").append(filename);
        return executeCommand(command.toString());
    }

    /**
     * Creates a file to submit all jobs
     *
     * @param workingDirectory - the working directory. The command cd
     * workingDirectory is added to file.
     * @param filename - the name of the submit file.
     * @param commands - commands to include in the submit file. This is a list
     * of all the qsub commands for each job.
     * @param moduleLoad - modules to load. Null excepted.
     * @return stdIO of creating the file
     * @throws IOException
     */
    public String createSubmitFile(String workingDirectory, String filename, List<String> commands, String moduleLoad) throws IOException, CommandExecutionException {
        StringBuilder contents = new StringBuilder();
        contents.append("cd ").append(workingDirectory).append("\n");
        contents.append(moduleLoad).append("\n");

        StringBuilder command = new StringBuilder();
        command.append("cd ").append(workingDirectory).append("\n");
        command.append("cd ").append(workingDirectory).append("\n");
        command.append("/bin/echo -e \'").append(contents).append("\' > ").append(filename);
        return executeCommand(command.toString());
    }

    /**
     * List files by search term. Uses ls command to search (not regex {@link #listFilesByRegex(java.lang.String, java.lang.String) )
     * Example input: directory = "/users/me/", searchString = "*.fasta
     * Example command: ls /users/me/*.fasta
     *
     * @param directory
     * @param pattern - file name pattern
     * @return - list of files matching pattern.
     */
    public List<String> listFilesByPattern(String directory, String pattern) throws IOException, CommandExecutionException {
        StringBuilder bldr = new StringBuilder();
        bldr.append("ls ").append(directory);
        if (!directory.endsWith("/")) {
            bldr.append("/");
        }
        bldr.append(pattern);
        String result = executeCommand(bldr.toString());
        String[] split = result.split("\n");
        List<String> files = new ArrayList<String>(split.length);
        for (int i = 0; i < split.length; i++) {
            files.add(split[i]);
        }
        return files;
    }

    /**
     * List all the files in a directory.
     *
     * @param directory
     * @return the file names
     * @throws IOException
     */
    public List<String> listFiles(String directory) throws IOException, CommandExecutionException {
        StringBuilder bldr = new StringBuilder();
        bldr.append("ls ").append(directory);
        String result = executeCommand(bldr.toString());
        String[] split = result.split("\n");
        List<String> files = new ArrayList<String>(split.length);
        if (!directory.endsWith("/")) {
            directory = directory + "/";
        }
        for (int i = 0; i < split.length; i++) {
            files.add(directory + split[i]);
        }
        return files;
    }

    /**
     * Lists the files in the directory by the regex (by piping the grep command
     * into ls) To use the ls search pattern method see {@link #listFilesByPattern(java.lang.String, java.lang.String)
     *
     * @param directory
     * @param regex
     * @return List of file names matching regex in directory
     */
    public List<String> listFilesByRegex(String directory, String regex) throws IOException, CommandExecutionException {
        StringBuilder bldr = new StringBuilder();
        bldr.append("ls ").append(directory);
        bldr.append(" | grep ").append(regex);
        String result = executeCommand(bldr.toString());
        String[] split = result.split("\n");
        List<String> files = new ArrayList<String>(split.length);
        if (!directory.endsWith("/")) {
            directory = directory + "/";
        }
        for (int i = 0; i < split.length; i++) {
            files.add(directory + split[i]);
        }
        return files;
    }

    /**
     * Creates the pbs scriptfile to be submitted using the qsub command.
     * Recursively makes the script directory if it does not exist.
     *
     * @param workingDirectory - the working directory for these jobs
     * @param scriptFolder - the script folder either relative to working
     * directory or absolute path.
     * @param scriptFile - the unique name of the scriptFile
     * @param commands - the commands to be executed.
     * @param moduleLoad - the command to load modules. Example: module load
     * blast
     * @return the IO of create file command
     * @throws IOException
     */
    public String createScriptFile(String workingDirectory, String scriptFolder, String scriptFile, List<String> commands, String moduleLoad) throws IOException, CommandExecutionException {
        StringBuilder contents = new StringBuilder();
        contents.append("cd $PBS_O_WORKDIR\n");
        if (moduleLoad != null && !moduleLoad.isEmpty()) {
            contents.append(moduleLoad).append("\n");
        }

        for (String line : commands) {
            contents.append(line).append("\n");
        }

        StringBuilder command = new StringBuilder();
        command.append("mkdir -p ").append(workingDirectory).append("\n");
        command.append("cd ").append(workingDirectory).append("\n");
        command.append("mkdir -p ").append(scriptFolder).append("\n");
        command.append("cd ").append(scriptFolder).append("\n");
        command.append("/bin/echo -e \'").append(contents).append("\' > ").append(scriptFile);
        return executeCommand(command.toString());
    }

    /**
     * Creates a single submit command for a qsub job.
     *
     *
     * @param jobName - name of Qsub job
     * @param scriptDirectory - path of the relative (to working directory) or
     * absolute directory in which the script file is contained.
     * @param scriptFile - name of the script file in the scriptDirectory
     * @param additionalQsubArgs - additional qsub arguments (do not include
     * waiton command here) null excepted. Example entry in list: -q NameOfQueue
     * @param waitOnCommand - the qsub waitOn command (null excepted). Example:
     * -W depend=afterany:$JOBID
     * @param assignToVariable - assigns the jobId to a variable (JOBID=$(qsub
     * script")). Use in conjuction with waitOn for another job. Null excepted.
     * @return the single command to submit this job.
     */
    public String createSubmitCommand(String jobName, String scriptDirectory, String scriptFile, List<String> additionalQsubArgs, String waitOnCommand, String assignToVariable) {
        StringBuilder command = new StringBuilder();
        if (assignToVariable != null) {
            command.append(assignToVariable).append("=$(");
        }
        command.append("qsub");
        if (jobName != null) {
            command.append(" -N ").append(jobName);
        }
        if (additionalQsubArgs != null) {
            for (String qArg : additionalQsubArgs) {
                command.append(" ").append(qArg);
            }
        }

        if (waitOnCommand != null && !waitOnCommand.isEmpty()) {
            command.append(" ").append(waitOnCommand);
        }

        command.append(" ");
        if (scriptDirectory != null && !scriptDirectory.isEmpty()) {
            command.append(scriptDirectory);
            if (!scriptDirectory.endsWith("/")) {
                command.append("/");
            }
        }

        command.append(scriptFile);
        if (assignToVariable != null) {
            command.append(")");
        }

        return command.toString();
    }

    /**
     * Submits a job to the cluster.
     *
     * @param job
     * @return the jobId
     * @throws IOException
     */
    public String doJob(Job job) throws IOException, CommandExecutionException {
        createScriptFile(job.getWorkingDirectory(), job.getScriptDirectory(), job.getScriptFile(), job.getCommands(), job.getModuleLoad());
        StringBuilder command = new StringBuilder();
        command.append("cd ").append(job.getWorkingDirectory()).append("\n");
        command.append(createSubmitCommand(job.getJobName(), job.getScriptDirectory(), job.getScriptFile(), job.getAddQsubArgs(), job.getDependencies(), null));
        log.writeLog(command.toString());
        String jobId = executeCommand(command.toString()).trim();
        job.setJobId(jobId);
        return jobId;
    }

    private void setLog(Log log) {
        this.log = log;
    }

}
