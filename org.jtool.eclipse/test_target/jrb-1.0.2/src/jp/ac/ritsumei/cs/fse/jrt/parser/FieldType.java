/*
 *     FieldType.java  Nov 14, 2001
 *
 *     Seisuke Shimizu (sei@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.Iterator;

public class FieldType {

    public static SummaryJavaField getFieldType(SummaryJavaClass jclass, String name) {
        if (jclass == null) {
            return null;
        }
        return jclass.getJavaField(jclass.getName() + "." + name);
    }

    public static SummaryJavaField getFieldTypeAt(SummaryJavaClass src,
      SummaryJavaClass jclass, String name) {
        if (src == null || jclass == null) {
            return null;
        }
        SummaryJavaField jfield = getFieldType(jclass, name);
        if (isAccessible(src, jfield)) {
            return jfield;
        }
        return null;
    }

    public static SummaryJavaField getFieldType(SummaryJavaClass src,
      SummaryJavaClass jclass, String name) {
        if (src == null || jclass == null) {
            return null;
        }
        SummaryJavaField jfield = getFieldTypeAt(src, jclass, name);
        if (jfield != null) {
            return jfield;
        }

        Iterator it = jclass.getAncestors().iterator();
        while (it.hasNext()) {
            SummaryJavaClass jc = (SummaryJavaClass)it.next();
            jfield = getFieldTypeAt(src, jc, name);
            if (jfield != null) {
                return jfield;
            }
        }
        return null;
    }

    private static boolean isAccessible(SummaryJavaClass src, SummaryJavaField jfield) {
        if (jfield != null) {
            if (jfield.isPublic()) {
                return true;
            }

            SummaryJavaClass jc = jfield.getJavaClass();
            if (jfield.isProtected() && src.getAncestors().contains(jc)) {
                return true;
            }

            if (!jfield.isPrivate() && src.isInSamePackage(jc)) {
                return true;
            }

            if (jfield.isPrivate() && src.equals(jc)) {
                return true;
            }
        }
        return false;
    }

    public static String wideningConversions(JavaVariableList vlist) {
        String type1 = "";
        Iterator it = vlist.iterator();
        while (it.hasNext()) {
            JavaVariable jvar = (JavaVariable)it.next();
            if (jvar.isPrimitive()) {
                type1 = wideningPrimitiveType(type1, jvar.getType().substring(1));
            } else {
                return jvar.getType();
            }
        }
        return "!" + type1;
    }

    public static String wideningPrimitiveType(String type1, String type2) {
        if (type1.equals("double") || type2.equals("double")) {
            return "double";
        }
        if (type1.equals("float") || type2.equals("float")) {
            return "float";
        }
        if (type1.equals("long") || type2.equals("long")) {
            return "long";
        }
        if (type1.equals("int") || type2.equals("int")) {
            return "int";
        }
        if (type1.equals("char") || type2.equals("char")) {
            return "char";
        }
        if (type1.equals("short") || type2.equals("short")) {
            return "short";
        }
        if (type1.equals("byte") || type2.equals("byte")) {
            return "short";
        }
        return type2;
    }
}
