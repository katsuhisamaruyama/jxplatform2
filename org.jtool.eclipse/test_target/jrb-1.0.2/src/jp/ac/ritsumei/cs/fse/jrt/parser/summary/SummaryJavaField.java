/*
 *     SummaryJavaField.java  Nov 10, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.parser.summary;
import jp.ac.ritsumei.cs.fse.jrt.model.JavaModifier;

public class SummaryJavaField {
    private String name;
    private String type;
    private JavaModifier modifier = null;
    private SummaryJavaClass jclass;

    public SummaryJavaField(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public String getType() {
        return type;
    }

    public void setModifier(JavaModifier modifier) {
        this.modifier = modifier;
    }

    public JavaModifier getModifier() {
        return modifier;
    }

    public void setJavaClass(SummaryJavaClass jc) {
        jclass = jc;
    }

    public SummaryJavaClass getJavaClass() {
        return jclass;
    }

    public boolean isAccessible(SummaryJavaClass jc) {
        if (isPublic()) {
            return true;
        }
        if (isDefault()) {
            return true;
        }

        if (isProtected()) {
            return true;
        }
        if (isPrivate()) {
            return true;
        }
        return false;
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

    public void print() {
        System.out.println("    FIELD = " + getModifier().toString()
                           + " " + getType() + " " + getName());
    }
}
