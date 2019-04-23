/*
 *     WarningEvent.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.util;
import java.util.EventObject;

public class WarningEvent extends EventObject {
    private String message;

    public WarningEvent(Object source, String msg) {
        super(source);
        message = msg;
    }

    public void setMessage(String msg) {
        message = msg;
    }

    public String getMessage() {
        return message;
    }
}

