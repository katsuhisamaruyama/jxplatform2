/*
 *     MethodType.java  Nov 15, 2001
 *
 *     Seisuke Shimizu (sei@fse.cs.ritsumei.ac.jp)
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.*;
import jp.ac.ritsumei.cs.fse.jrt.model.*;
import java.util.ArrayList;
import java.util.Iterator;

public class MethodType {
    private static ArrayList matchedMethods = new ArrayList();  // SummaryJavaMethod

    public static SummaryJavaMethod getMethodTypeAt(SummaryJavaClass src,
      SummaryJavaClass jclass, String name, ArrayList params) {
        if (src == null || jclass == null) {
            return null;
        }

        name = name.substring(0, name.indexOf('{'));
        SummaryJavaMethod calling = getMethodInvocation(src, name, params);

        matchedMethods.clear();
        matchedMethods.addAll(jclass.getJavaMethods(name));
        return selectAccessibleAndApplicableMethods(src, calling);
    }

    public static SummaryJavaMethod getMethodType(SummaryJavaClass src,
      SummaryJavaClass jclass, String name, ArrayList params) {
        if (src == null || jclass == null) {
            return null;
        }

        name = name.substring(0, name.indexOf('{'));
        SummaryJavaMethod calling = getMethodInvocation(src, name, params);

        matchedMethods.clear();
        collectCandidatesForCalledMethod(jclass, calling.getName());
        return selectAccessibleAndApplicableMethods(src, calling);
    }

    private static SummaryJavaMethod selectAccessibleAndApplicableMethods(
      SummaryJavaClass src, SummaryJavaMethod calling) {
        removeInaccessibleMethods(src);
        removeInapplicableMethods(calling);

        if (matchedMethods.size() > 1) {
            removeLessSpecificMethods();
            removeMethodsWithSameName();
        }

        if (matchedMethods.size() == 1) {
            SummaryJavaMethod jmethod = (SummaryJavaMethod)matchedMethods.get(0);
            return jmethod;
        }
        return null;
    }

    private static SummaryJavaMethod getMethodInvocation(
      SummaryJavaClass jc, String name, ArrayList params) {
        SummaryJavaMethod calling = new SummaryJavaMethod(name);
        calling.setJavaClass(jc);
        Iterator it = params.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            calling.addParameter(jvar.getType());
        }
        return calling;
    }

    private static void collectCandidatesForCalledMethod(
      SummaryJavaClass jclass, String name) {
        matchedMethods.addAll(jclass.getJavaMethods(name));

        Iterator it = jclass.getAncestors().iterator();
        while (it.hasNext()) {
            SummaryJavaClass jc = (SummaryJavaClass)it.next();
            matchedMethods.addAll(jc.getJavaMethods(name));
        }
    }

    private static void removeInaccessibleMethods(SummaryJavaClass src) {
        ArrayList candidates = new ArrayList(matchedMethods);
        for (int i = 0; i < candidates.size(); i++) {
            SummaryJavaMethod jm = (SummaryJavaMethod)candidates.get(i);

            SummaryJavaClass jc = jm.getJavaClass();
            if (jm.isPrivate() && !src.equals(jc)) {
                matchedMethods.remove(jm);
            }

            if (jm.isDefault() && !src.isInSamePackage(jc)) {
                matchedMethods.remove(jm);
            }

            if (jm.isProtected()
              && !(src.equals(jc) || src.getAncestors().contains(jc))) {
                matchedMethods.remove(jm);
            }
        }
    }

    private static void removeInapplicableMethods(SummaryJavaMethod calling) {
        ArrayList candidates = new ArrayList(matchedMethods);
        for (int i = 0; i < candidates.size(); i++) {
            SummaryJavaMethod jm = (SummaryJavaMethod)candidates.get(i);

            if (calling.getParameterNumber() != jm.getParameterNumber()) {
                matchedMethods.remove(jm);
            }

            if (calling.getParameterNumber() != 0 && !isConvertible(calling, jm)) {
                matchedMethods.remove(jm);
            }
        }
    }        

    private static void removeLessSpecificMethods() {
        ArrayList candidates = new ArrayList(matchedMethods);
        for (int t = 0; t < candidates.size(); t++) {
            SummaryJavaMethod tmethod = (SummaryJavaMethod)candidates.get(t);

            for (int u = 0; u < candidates.size(); u++) {
                SummaryJavaMethod umethod = (SummaryJavaMethod)candidates.get(u);

                if (isMoreSpecific(tmethod, umethod) &&
                  tmethod.getSignature().compareTo(umethod.getSignature()) != 0) {
                    matchedMethods.remove(umethod);
                }
            }
        }
    }

    private static void removeMethodsWithSameName() {
        ArrayList candidates = new ArrayList(matchedMethods);
        for (int t = 0; t < candidates.size(); t++) {
            SummaryJavaMethod tmethod = (SummaryJavaMethod)candidates.get(t);

            for (int u = t + 1; u < candidates.size(); u++) {
                SummaryJavaMethod umethod = (SummaryJavaMethod)candidates.get(u);

                if (tmethod.getSignature().compareTo(umethod.getSignature()) == 0) {
                    matchedMethods.remove(umethod);
                }
            }
        }
    }

    private static boolean isMoreSpecific(
      SummaryJavaMethod tmethod, SummaryJavaMethod umethod) {
        SummaryJavaClass tclass = tmethod.getJavaClass();
        SummaryJavaClass uclass = umethod.getJavaClass();
        if (!tclass.equals(uclass) && !tclass.getAncestors().contains(uclass)) {
            return false;
        }
        return isConvertible(tmethod, umethod);
    }

    private static boolean isConvertible(
      SummaryJavaMethod tmethod, SummaryJavaMethod umethod) {
        for (int pos = 0; pos < tmethod.getParameterNumber(); pos++) {
            String tparam = tmethod.getParameter(pos);
            String uparam = umethod.getParameter(pos);
            if (tparam == null || uparam == null) {
                return false;
            }

            if (tparam.equals(uparam) || isSpeciallyConvertible(tparam, uparam)) {
                return true;
            }

            while (tparam.endsWith("[]") && uparam.endsWith("[]")) {
                tparam = tparam.substring(0, tparam.lastIndexOf("["));
                uparam = uparam.substring(0, uparam.lastIndexOf("["));
            }
            if (tparam.endsWith("[]") || uparam.endsWith("[]")) {
                return false;
            }

            if (tparam.charAt(0) == '!' && uparam.charAt(0) == '!') {
                return isConvertiblePrimitiveType(tparam.substring(1), uparam.substring(1));
            }

            if (tparam.charAt(0) != '!' && uparam.charAt(0) != '!') {
                SummaryJavaFile tfile = tmethod.getJavaClass().getJavaFile();
                SummaryJavaFile ufile = umethod.getJavaClass().getJavaFile();
                SummaryJavaClass tclass = tfile.getJavaClass(tparam);
                SummaryJavaClass uclass = ufile.getJavaClass(uparam);
                return isConvertibleType(tclass, uclass);
            }
        }
        return false;
    }

    private static boolean isSpeciallyConvertible(String tparam, String uparam) {
        if (tparam.compareTo(uparam) == 0) {
            return true;
        }
        if (tparam.equals("null")) {
            return true;
        }
        if (uparam.compareTo(JDKZipFile.JDKPREFIX + "/java/lang/Object") == 0) {
            return true;
        }
        if (tparam.endsWith("[]")
          && uparam.compareTo(JDKZipFile.JDKPREFIX + "/java/lang/Cloneable") == 0) {
            return true;
        }
        if (tparam.endsWith("[]")
          && uparam.compareTo(JDKZipFile.JDKPREFIX + "/java/lang/Serializable") == 0) {
            return true;
        }
        return false;
    }

    private static boolean isConvertiblePrimitiveType(String tparam, String uparam) {
        String type = FieldType.wideningPrimitiveType(tparam, uparam);
        if (type.compareTo(uparam) == 0) {
            return true;
        }
        return false;
    }

    private static boolean isConvertibleType(SummaryJavaClass tclass, SummaryJavaClass uclass) {
        if (tclass == null || uclass == null) {
            return false;
        }
        if (tclass.equals(uclass) || tclass.getAncestors().contains(uclass)) {
            return true;
        }
        return false;
    }

    private static void printMethods(ArrayList ml) {
        Iterator it = ml.iterator();
        while (it.hasNext()) {
            SummaryJavaMethod jm = (SummaryJavaMethod)it.next();
            System.out.println("   CANDIDATE = " + jm.getSignature());
        }
    }
}
