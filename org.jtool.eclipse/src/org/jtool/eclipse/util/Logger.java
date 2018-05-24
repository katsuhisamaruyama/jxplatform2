/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.util;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Records logs.
 * @author Katsuhisa Maruyama
 */
public class Logger {
    
    private static Logger instance = new Logger();
    
    private List<String> logMessages = new ArrayList<String>();
    private String logfile;
    
    private JXConsole console = new JXConsole();
    
    private Logger() {
    }
    
    public static Logger getInstance() {
        return instance;
    }
    
    public void printProgress(String mesg) {
        System.out.print(mesg);
        System.out.flush();
    }
    
    public void printMessage(String mesg) {
        logMessages.add(mesg);
        
        System.out.println(mesg);
        System.out.flush();
    }
    
    public void printLog(String mesg) {
        logMessages.add(mesg);
    }
    
    public void printError(String mesg) {
        logMessages.add(mesg);
        
        System.err.println(mesg);
        System.err.flush();
        console.println(mesg);
    }
    
    public void printUnresolvedError(String mesg) {
        printError("!Unresolved : " + mesg);
    }
    
    public void setLogFile(String logfile) {
        this.logfile = logfile;
        File file = new File(logfile);
        if (file.exists()) {
            file.delete();
        }
    }
    
    public void writeLog() {
        if (logfile == null || logfile.length() == 0) {
            return;
        }
        
        StringBuilder buf = new StringBuilder();
        for (String mesg : logMessages) {
            buf.append(mesg);
            buf.append("\n");
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(logfile), true));
            writer.append(buf.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.err.flush();
        }
        logMessages.clear();
    }
}
