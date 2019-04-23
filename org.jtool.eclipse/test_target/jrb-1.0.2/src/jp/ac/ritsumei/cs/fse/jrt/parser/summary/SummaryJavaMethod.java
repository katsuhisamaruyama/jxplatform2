/*
 *     SummaryJavaMethod.java  Nov 10, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser.summary;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaModifier;
import java.util.ArrayList;
import java.util.Iterator;

public class SummaryJavaMethod {
    protected String name;
    protected String type;
    protected JavaModifier modifier = null;
    protected SummaryJavaClass jclass;
    protected ArrayList parameters = new ArrayList();  // String

    public SummaryJavaMethod(String name) {
        this.name = name;
    }        

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setJavaClass(SummaryJavaClass jc) {
        jclass = jc;
    }

    public SummaryJavaClass getJavaClass() {
        return jclass;
    }

    public void addParameter(String type) {
        parameters.add(type);
    }

    public int getParameterNumber() {
        return parameters.size();
    }

    public String getParameter(int pos) {
        try {
            String type = (String)parameters.get(pos);
            return jclass.getQualifiedName(type);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void setModifier(JavaModifier modifier) {
        this.modifier = modifier;
    }

    public JavaModifier getModifier() {
        return modifier;
    }

    public boolean isPublic() {
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
        return modifier.has("abstract");
    }

    public boolean equalsSignature(SummaryJavaMethod jm) {
        return equalsSignature(jm.getSignature());
    }

    public boolean equalsSignature(String sig) {
        if (getSignature().compareTo(sig) == 0) {
            return true;
        }
        return false;
    }

    public String getSignature() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append("(");
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            String type = (String)it.next();
            buf.append(" ");
            buf.append(jclass.getQualifiedName(type));
        }
        buf.append(" )");
        return buf.toString();
    }

    public String getMethodInfo() {
        StringBuffer buf = new StringBuffer();
        Iterator it = parameters.iterator();
        while (it.hasNext()) {
            String type = (String)it.next();
            buf.append(" ");
            if (type.charAt(0) == '!') {
                type = type.substring(1);
            }
            buf.append(type);
        }
        String params = "";
        if (buf.length() != 0) {
            params = buf.toString().substring(1);
        }
        return getName() + "(" + params + ")";
    }

    public void print() {
        System.out.println("    METHOD = " + getModifier()
                           + " " + getType() + " " + getSignature());
    }
}
