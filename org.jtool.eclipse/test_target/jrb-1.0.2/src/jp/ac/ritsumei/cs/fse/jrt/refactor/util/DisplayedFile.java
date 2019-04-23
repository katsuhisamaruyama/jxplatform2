/*
 *     DisplayedFile.java  Dec 22, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class DisplayedFile {
    private String name;
    private String oldText;
    private String newText;
    private ArrayList oldHighlights= new ArrayList();  // HighlightToken
    private ArrayList newHighlights= new ArrayList();  // HighlightToken

    public DisplayedFile() {
    }

    public DisplayedFile(String n, String oldt, String newt) {
        name = n;
        oldText = oldt;
        newText = newt;
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getShortName();
    }

    public String getShortName() {
        if (name != null) {
            int sep = name.lastIndexOf(File.separator);
            if (sep != -1) {
                return name.substring(sep + 1);
            }
        }
        return name;
    }

    public void setOldText(String t) {
        oldText = t;
    }

    public String getOldText() {
        return oldText;
    }

    public void setNewText(String t) {
        newText = t;
    }

    public String getNewText() {
        return newText;
    }

    public void setOldHighlight(ArrayList highlights) {
        oldHighlights = highlights;
    }

    public void addOldHighlight(ArrayList highlights) {
        oldHighlights.addAll(highlights);
    }

    public List getOldHighlight() {
        return oldHighlights;
    }

    public void setNewHighlight(ArrayList highlights) {
        newHighlights = highlights;
    }

    public void addNewHighlight(ArrayList highlights) {
        newHighlights.addAll(highlights);
    }

    public List getNewHighlight() {
        return newHighlights;
    }
}
