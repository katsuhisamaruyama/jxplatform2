# JxPlatform2 (JxPlatform v2) 

JxPlatform2 is a module that provides three types of easy-to-use models of Java source code. It facilitates the development and maintenance of various kinds of software tools. 

### Source Code Model 

JxPlatform2 builds a Java source code model consisting of the following elements: 

* JavaProject - Stores a collection of Java source files, packages, and classes 
* JavaFile - Provides information on a Java source file 
* JavaPackage - Provides information on a package 
* JavaClass - Provides information on a class, an interface, an enum, or an enum constant 
* JavaMethod - Provides information on a method, a constructor, or an initializer 
* JavaField (extends JavaVaraible) - Provides information on a field
* JavaLocalVar (extends JavaVaraible) - Provides information on a local variable or a parameter 

### CFG (Control Flow Graph) 

JxPlatform2 creates a CFG for each method existing in Java source code. 

* CFGStore - Provides APIs to create CFGs from Java source code and stores them 
* CFG (extends CommonCFG) - Provides information about a CFG of a method or a field 
* CCFG (extends CommonCFG) - Provides information about a class control flow graph (CCFG) of a class 
* BasicBlock - Provides information about a basic block of a CFG 
* CallGraph - Provides information about a call graph

Each CFG consists of nodes and edges between two nodes. 

* CFGNode - Represents a node of a CFG 
* CFGClassEntry - Represents an entry node of a CFG for a class or an interface 
* CFGMethodEntry- Represents an entry node of a CFG for a method or a constructor 
* CFGFieldEntry - Represents an entry node of a CFG for a field declaration, or an enum-constant 
* CFGExit - Represents an exit node of a CFG 
* CFGStatement - Represents a statement node of a CFG 
* CFGParameter - Represents a parameter node of a CFG 
* CFGMerge - Represents a merge node of a CFG 
* ControlFlow - Represents control flow between CFG nodes 

Each CFGStatement node holds a define-set and a use-set of references to fields, local variables, and method calls. The define-set contains fields and local variables defined in an expression corresponding to the node. The use-set contains fields and local variables used in the expression and method calls performed.

* JReference - Represents a reference to a field, or a local variable, or a method 
* JMethodReference - Represents reference to a called method or a called constructor 
* JFieldReference - Represents a reference to an accessed field 
* JLocalVarReference - Represents a reference to an accessed local variable 
* JInvisibleReference - represents a reference to an invisible variable (that stores a return value or an argument value) 

### PDG (Program Dependence Graph) 

JxPlatform2 creates a PDG from a CFG for each method existing in Java source code. 

* PDGStore - Stores a collection of PDGs created from CFGs 
* PDG (extends CommonCFG) - Provides information about a PDG of a method or a field 
* ClDG (extends CommonCFG)- Provides information about a class dependence graph (ClDG) a PDG of a method or a class 
* SDG (extends CommonCFG) - Provides information about a system dependence graph (SDG) the whole source code

Each PDG consists of nodes and edges between two nodes. 

* PDGNode - Represents a node of a PDG 
* PDGEntry-  Represents an entry node of a PDG for a method, a constructor, a field declaration, or an enum-constant 
* PDGClassEntry - Represents an entry node of a ClDG for a class or an interface 
* PDGStatement - Represents a statement node of a PDG 
* DependenceEdge - Represents dependence between PDG nodes 
* CD - Represents control dependence between PDG nodes 
* DD - Represents data dependence between PDG nodes 
* CallEdge - Represents a call edge in a ClDG 
* ClassMemberEdge - Represents a class member edge in a ClDG 

### Program slice 

Each slice consists PDG nodes that may affect the value of a variable of interest (called a slice criterion).

* Slice - Stores information about a program slice 
* SliceCriterion - Represents a slicing criterion 

A slice is constructed based on flow-sensitive dependency analysis. It traverses only the PDG nodes that reach a node given as a slice criterion. 

## Requirement

JDK 1.8 
[Eclipse](https://www.eclipse.org/) 4.7 (Oxygen) and later  


## License 

[Eclipse Public License 1.0 (EPL-1.0)](<https://opensource.org/licenses/eclipse-1.0.php>) 

## Installation

### From the command line

You can directly download jar files for the batch-process application and library versions of JxPlatform2 on [GitHub](<https://github.com/katsuhisamaruyama/jxplatform2/tree/master/org.jtool.eclipse/releases>). 

Alternatively, you can build the batch-process versions of JxPlatform2 with the following commands: 

    git clone https://github.com/katsuhisamaruyama/jxplatform2/
    cd jxplatform2/org.jtool.eclipse
    ./gradlew build jar shadowJar

Jar files of JxPlatform2 can be found in the 'build/libs' folder. 


### From an Eclipse

When using the Eclipse update site, select menu items: "Help" -> "Install New Software..." ->  
Input `https://katsuhisamaruyama.github.io/jxplatform2/org.jtool.eclipse.site/site.xml` in the text field of "Work with:" 

If you prefer to manually install the plug-in, download the latest release of the jar file in the [plug-in directory]
(<https://github.com/katsuhisamaruyama/jxplatform2/tree/master/org.jtool.eclipse.site/plugins>)
and put it in the 'plug-ins' directory under the Eclipse installation. Eclipse needs to be restarted. 


## Usage

### As a batch-process application

`jxplatform-1.0-all.jar` is an executable jar file itself. For example, when you put Java source code under the `xxx` folder (Java source files are expanded under the folder), the following command builds a Java model for the source code.

    java -jar jxplatform-1.0-all.jar -target xxx/ -classpath 'xxx/lib/*:xxx/libs/*' -srcpath 'xxx/src:xxx/test' -name name -logfile xxx.log

* `-classpath`: (optional) specifies class paths where needed libraries are contained 
* `-name`: (optional) specifies the name of a project managed in jxplatform2 
* `-logfile`: (optional) specifies the name of a file in which the result of analysis is written 

### As a library embedded into an application

If your batch-process application employs JxPlatform2 as a library, you should use `jxplatform-1.0-lib.jar` instead of `jxplatform-1.0-all.jar`. In this case, the following jar files are also included in the classpath. 

* `org.eclipse.core.contenttype-*.jar`
* `org.eclipse.core.jobs-*.jar`
* `org.eclipse.core.resources-*.jar`
* `org.eclipse.core.runtime-*.jar`
* `org.eclipse.equinox.common-*.jar`
* `org.eclipse.equinox.preferences-*.jar`
* `org.eclipse.jdt.core-*.jar`
* `org.eclipse.osgi-*.jar`
* `javassist.jar`

The code building a Java model is describe below. 

    // import org.jtool.eclipse.batch.ModelBuilderBatch;
    // import org.jtool.eclipse.javamodel.JavaProject;
    // import org.jtool.eclipse.javamodel.JavaClass;
    // import org.jtool.eclipse.javamodel.JavaMethod;
    // import org.jtool.eclipse.javamodel.JavaField;
    
    String name;       // an arbitrary project name
    String target;     // the path of the top directory that contains Java source files in the project
    String classpath;  // the path of the top directory that contains Java class files and/or jar files
    
    ModelBuilderBatch builder = new ModelBuilderBatch();
    builder.setLogVisible(true);
    JavaProject jproject = builder.build(name, target, classpath);
    
    for (JavaClass jclass : jproject.getClasses()) {
        jclass.print();
        for (JavaMethod jmethod : jclass.getMethods()) {
            jmethod.print();
        }
        for (JavaField jfield : jclass.getFields()) {
            jfield.print();
        }
    }
    
    builder.unbuild();

### As an Eclipse plug-in

The following is the typical code for building a Java model for the source code within the Eclipse project.

    // import org.eclipse.jdt.core.IJavaProject;
    // import org.jtool.eclipse.plugin.ModelBuilderPlugin;
    // import org.jtool.eclipse.javamodel.JavaProject;
    
    IJavaProject project;  // a Java project resource in Eclipse
    
    ModelBuilderPlugin builder = new ModelBuilderPlugin();
    builder.setLogVisible(true);
    JavaProject jproject = builder.build(project);
    
    ...
    
    builder.unbuild();

The plug-in automatically collects source files that was modified after the previous build.
Thus, the dirty source files will be analyzed if your code will perform build.
Use the `buildWhole(project)` method for clean re-build.  


### Building CFGs

The following code builds CCFGs for all classes and CFGs for all methods and fields within a project.

    // import org.jtool.eclipse.cfg.CCFG;
    // import org.jtool.eclipse.cfg.CFG;
    
    ModelBuilderBatch builder = new ModelBuilderBatch();
    builder.setLogVisible(true);
    JavaProject jproject = builder.build(name, target, classpath);
    
    for (JavaClass jclass : jproject.getClasses()) {
        CCFG ccfg = builder.getCCFG(jclass);
        for (CFG cfg : ccfg.getCFGs()) {
            cfg.print();
        }
    }
    
    builder.unbuild();

To build a normal CFG, only the source code within the project is analyzed. If high-precision of dependency analysis is needed, the bytecode of library classes can be additionally analyzed using the following code:

    ModelBuilderBatch builder = new ModelBuilderBatch(true);
    // ModelBuilderPlugin builder = new ModelBuilderPlugin(true);


A CFG can be created from an object of JavaMethod or JavaField as described below.

    JavaMethod jmethod;  // a representation of a method
    JavaField jfield;    // a representation of a field
    boolean force;       // whether the analyzer forces to create CFGs PDGs or allows to reuse them
    CFG cfg = builder.getCFG(jmethod, force);
    CFG cfg = builder.getCFG(jfield, force);

A call graph can be created within a project, a class, or a method as described below..

    CallGraph callGraph;
    callGraph = build.getCallGraph(JavaProject jproject);
    callGraph = build.getCallGraph(JavaClass jclass);
    callGraph = build.getCallGraph(JavaMethod jmethod);


### Building PDGs

The following code builds ClDGs for all classes and PDGs for all methods and fields within a project.

    // import org.jtool.eclipse.pdg.ClDG;
    // import org.jtool.eclipse.pdg.PDG;
    
    ModelBuilderBatch builder = new ModelBuilderBatch();
    builder.setLogVisible(true);
    builder.setContainingFallThroughEdge(true);  // contains fall-through edges in the constructing a PDG
    JavaProject jproject = builder.build(name, target, classpath);
    
    for (JavaClass jclass : jproject.getClasses()) {
        ClDG cldg = builder.getClDG(jclass);
        for (PDG pdg : cldg.getPDGs()) {
            pdg.print();
        }
    }
    
    builder.unbuild();


A PDG, ClDG, and SDG can be created from an object of JavaMethod, JavaField, or JavaClass as described below.
    
    PDG pdg;
    pdg = getPDG(cfg, force);
    pdg = builder.getPDG(jmethod, force);
    pdg = builder.getPDG(jfield, force);
    pdg = builder.getPDGWithinSDG(jmethod, force);
    pdg = builder.getPDGWithinSDG(jfield, force);
    
    ClDG cldg;
    cldg = getClDG(ccfg, force);
    cldg = builder.getClDG(jclass, force);
    cldg = builder.getClDGWithinSDG(jclass, force);
    
    SDG sdg;
    sdg = builder.getSDG(jclass, force);
    sdg = builder.getSDG(classes, force);  // classes: Set<JavaClass>
    sdg = builder.getSDG();


### Extracting program slices

A program slice can be created from an object of PDG as described below.

    // import org.jtool.eclipse.pdg.PDGNode;
    // import org.jtool.eclipse.pdg.PDGStatement;
    // import org.jtool.eclipse.cfg.JReference;
    // import org.jtool.eclipse.slice.Slice;
    // import org.jtool.eclipse.slice.SliceCriterion;
    
    JavaClass jclass;  // a class to be sliced
    PDGNode node;      // a node given as a slice criterion
    JReference var;    // a variable of interest given as a slice criterion
    
    ModelBuilderBatch builder = new ModelBuilderBatch();
    builder.setLogVisible(true);
    JavaProject jproject = builder.build(name, target, classpath);
    
    Set<JavaClass> classes = builder.getAllClassesBackward(jclass);
    SDG sdg = builder.getSDG(classes);
    ClDG cldg = sdg.getClDG(jclass);
    
    SliceCriterion criterion = new SliceCriterion(cldg, node, var);
    Slice slice = new Slice(criterion);
    slice.print();
    
    builder.unbuild();

A convenient static method is also provided.
 
    CommonPDG pdg;   // a PDG or ClDG
    String code;     // the source code text of a class or a method corresponding to common PDG
    int lineNumber:    // the line number corresponding to a variable of interest
    int columnNumber:  // column number corresponding to the variable on the line
    
    SliceCriterion criterion = SliceCriterion.find(pdg, code, lineNumber, int columnNumber) {

The following code snippet generates Java source code from a program slice.

    ModelBuilderPlugin builder;  // model builder
    JavaClass jclass;            // a class to be sliced
    JavaMethod jmethod;          // a class to be sliced
    Slice slice;                 // slice
    
    SliceExtractor extractor = new SliceExtractor(builder, slice.getNodes(), jclass);
    // SliceExtractor extractor = new SliceExtractor(builder, slice.getNodes(), jmethods);
    String code = extractor.extract();

## Author

[Katsuhisa Maruyama](http://www.fse.cs.ritsumei.ac.jp/~maru/index.html)
