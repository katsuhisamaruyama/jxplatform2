/*
 *     MoveClass.java  Dec 18, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.classes;
import jp.ac.ritsumei.cs.fse.jrt.refactor.ClassRefactoring;
import jp.ac.ritsumei.cs.fse.jrt.refactor.util.*;
import jp.ac.ritsumei.cs.fse.jrt.refactor.dialog.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.File;
import javax.swing.JFrame;

public class MoveClass extends ClassRefactoring {
    private String dstName = null;
    private JavaFile dfile;
    private boolean isPublic = false;

    public MoveClass() {
        super();
    }

    public MoveClass(JFrame f, JavaClass jc, String dir, String name) {
        super(f, jc.getJavaFile(), jc);
        setRootDir(dir);
        dstName = name;
    }

    protected void preconditions() throws RefactoringException {
        if (dstName == null) {
            String name = jfile.getShortFileName() + ".java";
            dstName = MoveClassDialog.show(frame, "Move Class: " + jclass.getName(), rootDir, name);
        }
        if (dstName == null) {
            throw new RefactoringException("");
        }

        if (!dstName.endsWith(".java")) {
            throw new RefactoringException("not Java file:" + dstName);
        }

        dfile = impl.getJavaFile(dstName, jfile);
        if (dfile.isValid()) {
            if (impl.existsSameClassInFile(jclass, dfile)) {
                throw new RefactoringException("already exists:"
                  + " class/interface " + jclass.getName() + " in file " + dstName);
            }
        }

        String dstFileName = dstName;
        if (dstName.indexOf(File.separator) != -1) {
            dstFileName = dstName.substring(dstName.lastIndexOf(File.separator) + 1);
        }

        if (dstFileName.compareTo(jclass.getName() + ".java") == 0) {
            isPublic = true;
        }
    }

    protected void transform() throws RefactoringException {
        RefactoringVisitor transformer = new MoveClassVisitor(jclass, isPublic);
        jfile.accept(transformer);
        String dstClassDecl = transformer.getTempCode();

        PrintVisitor printer = new PrintVisitor();
        String newCode = printer.getCode(jfile);

        DisplayedFile file = new DisplayedFile(jfile.getName(), jfile.getText(), newCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);

        String dstCode;
        if (dfile.isValid()) {
            transformer = new InsertClassVisitor(dstClassDecl);
            dfile.accept(transformer);
        
            printer = new PrintVisitor();
            dstCode = printer.getCode(dfile);

        } else {
            if (dstName.indexOf(File.separator) != -1) {
                dfile.setName(dstName);
            } else {
                String fileName = jfile.getName().substring(0, jfile.getName().lastIndexOf(File.separator));
                dfile.setName(fileName + File.separator + dstName);
            }
            dfile.setText("NOT EXIST");
            dstCode = dstClassDecl;
        }

        file = new DisplayedFile(dfile.getName(), dfile.getText(), dstCode);
        file.setOldHighlight(transformer.getHighlights());
        file.setNewHighlight(printer.getHighlights());
        changedFiles.add(file);
    }

    protected void additionalTransformation() throws RefactoringException {
        if (isPublic) {
            return;
        }

        List files = impl.collectFilesUsingClass(jclass);
        files.remove(jfile);
        if (files.size() == 0) {
            return;
        }

        List candidateFiles = new ArrayList();
        Iterator it = files.iterator();
        while (it.hasNext()) {
            JavaFile cfile = (JavaFile)it.next();
            if (!impl.existsSameNameFileInPackage(dfile.getName(), cfile)) {
                candidateFiles.add(cfile);
            }
        }

        if (candidateFiles.size() == 0) {
            return;
        }

        String mesg = "The following files will be unable to use class " + jclass.getName() + ".";
        List filesToBeChanged = FileListDialog.show(frame, mesg, candidateFiles);

        it = filesToBeChanged.iterator();
        while (it.hasNext()) {
            dfile = (JavaFile)it.next();

            RefactoringVisitor transformer = new MoveClassVisitor(jclass, dfile);
            dfile.accept(transformer);

            PrintVisitor printer = new PrintVisitor();
            String dstCode = printer.getCode(dfile);

            DisplayedFile file = new DisplayedFile(dfile.getName(), dfile.getText(), dstCode);
            file.setOldHighlight(transformer.getHighlights());
            file.setNewHighlight(printer.getHighlights());
            changedFiles.add(file);
        }
    }

    protected String getLog() {
        String srcFileName = jfile.getShortFileName() + ".java";
        return "Move Class: " + jclass.getName() + " from " + srcFileName + " to " + dstName;
    }
}
