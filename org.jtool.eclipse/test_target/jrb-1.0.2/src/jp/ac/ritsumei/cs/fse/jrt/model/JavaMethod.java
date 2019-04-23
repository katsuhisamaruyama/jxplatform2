/*
 *     JavaMethod.java  Nov 7, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaClass;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaMethod;
import jp.ac.ritsumei.cs.fse.jrt.graphs.util.Graph;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

public class JavaMethod extends JavaComponent {
    private String name;
    private boolean isConstructor;
    private JavaModifier modifier = new JavaModifier();
    private String type = null;
    private String qualifiedType = null;
    private ArrayList declarations = new ArrayList();  // JavaStatement
    private ArrayList parameters = new ArrayList();  // JavaStatement
    private JavaVariableList variables = new JavaVariableList();
    private JavaClass jclass;
    private ArrayList calledMethods = new ArrayList();  // SummaryJavaMethod
    private Graph cfg = null;  // CFG: Control Flow Graph
    private Graph pdg = null;  // PDG: Program Dependence Graph

    public JavaMethod() {
        super();
    }        

    public JavaMethod(SimpleNode node) {
        super(node);
    }

    public JavaMethod(JavaMethod jm) {
        this(jm.getASTNode());
        name = jm.getName();
        modifier = jm.getModifier();
        type = jm.getType();
        parameters = jm.getParameters();
        jclass = jm.getJavaClass();
    }

    public boolean isJavaMethod() {
        return true;
    }

    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setModifier(JavaModifier modifier) {
        this.modifier = modifier;
    }

    public JavaModifier getModifier() {
        return modifier;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getPrettyType() {
        if (type.charAt(0) == '!') {
            return type.substring(1);
        }
        return type;
    }

    public String getQualifiedType() {
        if (qualifiedType != null) {
            return qualifiedType;
        }

        SummaryJavaClass sclass = getJavaClass().getSummaryJavaClass();
        if (getType() != null && sclass != null) {
            qualifiedType = sclass.getJavaFile().getQualifiedName(getType());
            return qualifiedType;
        }
        return null;
    }

    public void addJavaVariable(JavaVariable jvar) {
        variables.add(jvar);
    }

    public JavaVariableList getJavaVariables() {
        return variables;
    }

    public JavaVariable getJavaVaraible(JavaVariable jvar) {
        Iterator it = variables.iterator();
        while (it.hasNext()) {
            JavaVariable jv = (JavaVariable)it.next();
            if (jv.equals(jvar)) {
                return jv;
            }
        }
        return null;
    }

    public void addCalledMethod(SummaryJavaMethod smethod) {
        calledMethods.add(smethod);
    }

    public ArrayList getCalledMethods() {
        return calledMethods;
    }

    public void addDeclaration(JavaStatement jst) {
        declarations.add(jst);
    }

    public ArrayList getDeclarations() {
        return declarations;
    }

    public void addParameter(JavaStatement jst) {
        parameters.add(jst);
    }

    public void setParameters(ArrayList params) {
        parameters = params;
    }

    public ArrayList getParameters() {
        return parameters;
    }

    public int getParameterNumber() {
        return parameters.size();
    }

    public String getParameterTypes() {
        StringBuffer buf = new StringBuffer();
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            buf.append(" ");
            buf.append(jvar.getQualifiedType());
        }
        return buf.toString();
    }

    public boolean equalsParameterTypes(JavaMethod jm) {
        if (getParameterTypes().compareTo(jm.getParameterTypes()) == 0) {
            return true;
        }
        return false;
    }

    public String getSignature() {
        return getName() + "(" + getParameterTypes() + " )";
    }

    public String getParameterNames() {
        StringBuffer buf = new StringBuffer();
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            if (buf.length() != 0) {
                buf.append(", ");
            }
            buf.append(jvar.getName());
        }
        return buf.toString();
    }

    public boolean equalsSignature(JavaMethod jm) {
        return equalsSignature(jm.getSignature());
    }

    public boolean equalsSignature(String sig) {
        if (getSignature().compareTo(sig) == 0) {
            return true;
        }
        return false;
    }

    public String getMethodInfo() {
        StringBuffer buf = new StringBuffer();
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            buf.append(" ");
            buf.append(jvar.getPrettyType());
        }
        String params = "";
        if (buf.length() != 0) {
            params = buf.toString().substring(1);
        }
        return getName() + "(" + params + ")";
    }

    /* deprecation */
    public String getDeclaration() {
        StringBuffer buf = new StringBuffer();
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            JavaStatement jst = (JavaStatement)it.next();
            JavaVariable jvar = jst.getDeclaration();
            if (buf.length() != 0) {
                buf.append(" ,");
            }
            buf.append(jvar.getPrettyType());
            buf.append(" ");
            buf.append(jvar.getName());
        }
        return buf.toString();
    }

    public void setCFG(Graph g) {
        cfg = g;
    }

    public Graph getCFG() {
        return cfg;
    }

    public void setPDG(Graph g) {
        pdg = g;
    }

    public Graph getPDG() {
        return pdg;
    }

    public void setConstructor(boolean bool) {
        isConstructor = bool;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isVoid() {
        if (type.compareTo("void") == 0) {
            return true;
        }
        return false;
    }

    public void setJavaClass(JavaClass jclass) {
        this.jclass = jclass;
    }

    public JavaClass getJavaClass() {
        return jclass;
    }

    public boolean isPublic() {
        if (jclass.isInterface()) {        
            return true;
        }
        return modifier.has("public");
    }

    public boolean isProtected() {
        return modifier.has("protected");
    }

    public boolean isPrivate() {
        return modifier.has("private");
    }

    public boolean isDefault() {
        return (!isPublic() && !isProtected() && !isPrivate());
    }

    public boolean isStatic() {
        return modifier.has("static");
    }

    public boolean isAbstract() {
        if (jclass.isInterface()) {        
            return true;
        }
        return modifier.has("abstract");
    }

    public boolean isFinal() {
        return modifier.has("final");
    }

    public boolean isNative() {
        return modifier.has("native");
    }

    public boolean isSynchronized() {
        return modifier.has("synchronized");
    }

    public boolean isStrictfp() {
        return modifier.has("strictfp");
    }
}
