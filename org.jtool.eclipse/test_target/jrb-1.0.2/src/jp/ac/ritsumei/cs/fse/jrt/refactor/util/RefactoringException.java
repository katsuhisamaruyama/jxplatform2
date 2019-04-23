/*
 *     RefactoringException.java  Nov 27, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.refactor.util;

public class RefactoringException extends Exception {
    public RefactoringException(String mesg) {
        super(": " + mesg + ".");
    }
}
