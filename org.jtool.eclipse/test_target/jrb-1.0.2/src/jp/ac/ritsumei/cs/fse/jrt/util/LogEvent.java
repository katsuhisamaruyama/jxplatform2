/*
 *     LogEvent.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.util;
import java.util.EventObject;
import java.sql.Timestamp;

public class LogEvent extends EventObject {
    private String message;

    public LogEvent(Object source, String msg) {
        super(source);
        message = msg;
    }

    public void setMessage(String msg) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        message = timestamp + " : " + msg;
    }

    public String getMessage() {
        return message;
    }
}
