/*
 *     ExceptionPrint.java  Oct 30, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEvent;
import jp.ac.ritsumei.cs.fse.jrt.util.WarningEventListener;
import java.io.*;

public class ExceptionPrint implements WarningEventListener {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public ExceptionPrint() {
    }

    public void printMessage(WarningEvent evt) {
        System.out.println(evt.getMessage());
        try {
            System.out.print("*** Continue ? ***");
            br.readLine();
        } catch (IOException e) { }
    }
}
