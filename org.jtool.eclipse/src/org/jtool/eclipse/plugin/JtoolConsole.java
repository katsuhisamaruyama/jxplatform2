/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.eclipse.plugin;

import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Displays a message on the console.
 * @author Katsuhisa Maruyama
 */
public class JtoolConsole {
    
    private static final String CONSOLE_NAME = "JTool Console";
    
    private static MessageConsoleStream consoleStream;
    
    private static void showConsole() {
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles = consoleManager.getConsoles();
        MessageConsole console = null;
        for (int i = 0; i < consoles.length; i++) {
            if (CONSOLE_NAME.equals(consoles[i].getName())) {
                console = (MessageConsole)consoles[i];
            }
        }
        if (console == null) {
            console = new MessageConsole(CONSOLE_NAME, null);
        }
        
        consoleManager.addConsoles(new MessageConsole[] { console });
        consoleManager.showConsoleView(console);
        consoleStream = console.newMessageStream();
    }
    
    public static void print(String mesg) {
        if (consoleStream == null) {
            showConsole();
        }
        consoleStream.print(mesg);
    }
    
    public static void println(String mesg) {
        if (consoleStream == null) {
            showConsole();
        }
        consoleStream.println(mesg);
    }
}
