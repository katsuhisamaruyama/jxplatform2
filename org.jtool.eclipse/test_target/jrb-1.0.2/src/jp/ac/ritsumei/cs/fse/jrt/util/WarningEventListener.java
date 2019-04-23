/*
 *     WarningEventListener.java  Nov 3, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.util;
import java.util.EventListener;

public interface WarningEventListener extends EventListener {
    public void printMessage(WarningEvent evt);
}
