/*
 *     ExtensionFileFilter.java  Oct 19, 2001
 *
 *     Tatsuya Kageyama (kage@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.gui;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ExtensionFileFilter extends FileFilter {
    private String description;
    private String extension;

    public ExtensionFileFilter(String description, String extension) {
        super();
        this.description = description;
        this.extension = extension;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        if (file.getName().toLowerCase().endsWith(extension)) {
            return true;
        }
        return false;
    }

    public String getDescription() {
        return description;
    }
}
