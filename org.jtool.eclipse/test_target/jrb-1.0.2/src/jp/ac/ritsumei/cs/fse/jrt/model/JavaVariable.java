/*
 *     JavaVariable.java  Nov 7, 2001
 *
 *     Katsuhisa Maruyama (maru@fse.cs.ritsumei.ac.jp)
 */

package jp.ac.ritsumei.cs.fse.jrt.model;
import jp.ac.ritsumei.cs.fse.jrt.parser.SimpleNode;
import jp.ac.ritsumei.cs.fse.jrt.parser.Token;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaField;
import jp.ac.ritsumei.cs.fse.jrt.parser.summary.SummaryJavaClass;
import java.io.File;

public class JavaVariable extends JavaComponent {
    private String name;
    private int id;
    private Token token = null;
    private JavaModifier modifier = new JavaModifier();
    private String type = null;
    private String qualifiedType = null;
    private JavaClass jclass = null;
    private JavaMethod jmethod = null;
    private SummaryJavaField sfield = null;
    private int sort = 0;
    private final int fieldVariable = 1;
    private final int formalParameter = 2;
    private final int actualParameter = 3;
    private final int localVariable = 4;

    private JavaVariable() {
        super();
    }

    public String toString() {
        return name;
    }
        
    public JavaVariable(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public JavaVariable(String name, int id, Token token) {
        this.name = name;
        this.id = id;
        this.token = token;
    }

    public JavaVariable(JavaVariable jvar) {
        name = jvar.getName();
        id = jvar.getID();
        token = jvar.getToken();
        modifier = jvar.getModifier();
        type = jvar.getType();
        jclass = jvar.getJavaClass();
        jmethod = jvar.getJavaMethod();
        sfield = jvar.getDeclField();
        sort = jvar.getSort();
        setResponsive(jvar);
    }

    public boolean isJavaVariable() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPrettyName() {
        if (name != null) {
            int sep = name.lastIndexOf(".");
            if (sep != -1) {
                return name.substring(sep + 1);
            }
        }
        return name;
    }

    public String getNameWithID() {
        return getName() + "(" + getID() + ")";
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
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

    public boolean isPrimitive() {
        if (type != null && type.charAt(0) == '!') {
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

    public void setJavaMethod(JavaMethod jmethod) {
        this.jmethod = jmethod;
    }

    public JavaMethod getJavaMethod() {
        return jmethod;
    }
 
    public String getNameWithPosition() {
        return getName() + "(" + id + ")" + toStringPosition();
    } 

    public boolean equals(JavaVariable v) {
        if (this == v) {
            return true;
        }
        if (name.compareTo(v.getName()) == 0 && id == v.getID()) {
            return true;
        }
        return false;
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
        if (jclass.isInterface()) {        
            return true;
        }
        return modifier.has("static");
    }

    public boolean isFinal() {
        if (jclass.isInterface()) {        
            return true;
        }
        return modifier.has("final");
    }

    public boolean isTransient() {
        return modifier.has("transient");
    }

    public boolean isVolatile() {
        return modifier.has("volatile");
    }

    public void setSort(int s) {
        sort = s;
    }

    public int getSort() {
        return sort;
    }

    public void setField() {
        sort = fieldVariable;
    }

    public boolean isField() {
        if (sort == fieldVariable) {
            return true;
        }
        return false;
    }        

    public void setLocal() {
        sort = localVariable;
    }

    public boolean isLocal() {
        if (sort == localVariable || sort == formalParameter) {
            return true;
        }
        return false;
    }

    public boolean isParameter() {
        if (isFormal() || isActual()) {
            return true;
        }
        return false;
    }

    public void setFormal() {
        sort = formalParameter;
    }

    public boolean isFormal() {
        if (sort == formalParameter) {
            return true;
        }
        return false;
    }

    public void setActual() {
        sort = actualParameter;
    }

    public boolean isActual() {
        if (sort == actualParameter) {
            return true;
        }
        return false;
    }

    public void setDeclField(SummaryJavaField sfield) {
        this.sfield = sfield;
    }

    public SummaryJavaField getDeclField() {
        return sfield;
    }
}
