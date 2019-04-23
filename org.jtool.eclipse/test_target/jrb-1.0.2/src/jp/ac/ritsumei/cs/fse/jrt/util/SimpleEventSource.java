/*
 *     SimpleEventSource.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.util;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEventListener;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.LogEventListener;
import java.util.*;

public class SimpleEventSource {
    private List warningListeners = new ArrayList();  //  WarningEventListener
    private List logListeners = new ArrayList();      //  LogEventListener

    public SimpleEventSource() {
    }

    public boolean addWarningEventListener(WarningEventListener l) {
        return warningListeners.add(l);
    }

    public boolean removeWarningEventListener(WarningEventListener l) {
        return warningListeners.remove(l);
    }

    public void fireWarningEvent(WarningEvent evt) {
        /*
        if (warningListeners.size() == 0) {
            System.out.println(evt.getMessage());
            return;
        }
        */

        Iterator it = warningListeners.iterator();
        while (it.hasNext()) {
            WarningEventListener l = (WarningEventListener)it.next();
            l.printMessage(evt);
        }
    }

    public boolean addLogEventListener(LogEventListener l) {
        return logListeners.add(l);
    }

    public boolean removeLogEventListener(LogEventListener l) {
        return logListeners.remove(l);
    }

    public void fireLogEvent(LogEvent evt) {
        /*
        if (logListeners.size() == 0) {
            System.out.println(evt.getMessage());
            return;
        }
        */

        Iterator it = logListeners.iterator();
        while (it.hasNext()) {
            LogEventListener l = (LogEventListener)it.next();
            l.printMessage(evt);
        }
    }
}
