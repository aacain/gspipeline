/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gspipeline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author aacain
 */
public class Job {

    List<String> commands = new LinkedList();
    String workingDirectory;
    String scriptDirectory;
    String scriptFile;
    List<String> addQsubArgs;
    String jobId;
    String jobName;
    HashMap<String, List<String>> dependMap;
    String moduleLoad;

    public Job() {
    }

    public Job(String workingDirectory, String scriptDirectory, String scriptFile, List<String> addQsubArgs) {
        this.workingDirectory = workingDirectory;
        this.scriptDirectory = scriptDirectory;
        this.scriptFile = scriptFile;
        this.addQsubArgs = addQsubArgs;
    }

    public Job(String jobName, String workingDirectory, String scriptDirectory, String scriptFile, List<String> addQsubArgs) {
        this.workingDirectory = workingDirectory;
        this.scriptDirectory = scriptDirectory;
        this.scriptFile = scriptFile;
        this.addQsubArgs = addQsubArgs;
        this.jobName = jobName;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void addCommands(List<String> commands) {
        this.commands.addAll(commands);
    }

    public void addCommand(String command) {
        this.commands.add(command);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getScriptDirectory() {
        return scriptDirectory;
    }

    public void setScriptDirectory(String scriptDirectory) {
        this.scriptDirectory = scriptDirectory;
    }

    public List<String> getAddQsubArgs() {
        return addQsubArgs;
    }

    public void setAddQsubArgs(List<String> addQsubArgs) {
        this.addQsubArgs = addQsubArgs;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public void addDependency(String dependencyType, String jobId) {
        if (this.dependMap == null) {
            this.dependMap = new HashMap<String, List<String>>();
        }
        List<String> ids = this.dependMap.get(dependencyType);
        if (ids == null) {
            ids = new LinkedList<String>();
            this.dependMap.put(dependencyType, ids);
        }
        ids.add(jobId);
    }

    public String getDependencies() {
        StringBuilder bldr = new StringBuilder();
        if (this.dependMap != null && !this.dependMap.isEmpty()) {
            bldr.append("-W ");
            for (Map.Entry<String, List<String>> entry : this.dependMap.entrySet()) {
                bldr.append(entry.getKey());
                for (String jobId : entry.getValue()) {
                    bldr.append(":").append(jobId);
                }
            }
        }

        return bldr.toString();
    }

    public String getModuleLoad() {
        return moduleLoad;
    }

    public void setModuleLoad(String moduleLoad) {
        this.moduleLoad = moduleLoad;
    }
}
