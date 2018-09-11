# JxPlatform2 (JxPlatform v2) 

JxPlatform2 is a module that provides three types of easy-to-use models of Java source code. It facilitates the development and maintenance of various kinds of software tools. 

### Source Code Model 

JxPlatform2 builds a Java model consisting of the following elements: 

* JavaProject - Stores a collection of Java source files, packages, and classes 
* JavaFile - Provides information on a Java source file 
* JavaPackage - Provides information on a package 
* JavaClass - Provides information on a class, an interface, an enum, or an enum constant 
* JavaMethod - Provides information on a method, a constructor, or an initializer 
* JavaField (extends JavaVaraible) - Provides information on a field variable 
* JavaLocal (extends JavaVaraible) - Provides information on a local variable or a parameter 

### CFG (Control Flow Graph) 

JxPlatform2 creates a CFG for each method existing in Java source code. 

* CFGStore - Stores a collection of CFGs created from Java source code 
* CFG - Provides information about a CFG 
* CCFG- Provides information about a class control flow graph (CCFG) 
* BasicBlock - Provides information about a basic block of a CFG 

Each CFG consists of nodes and edges between two nodes. 

* CFGNode - Represents a node of a CFG 
* CFGClassEntry - Represents an entry node of a CFG for a class or an interface 
* CFGMethodEntry- Represents an entry node of a CFG for a method or a constructor 
* CFGFieldEntry - Represents an entry node of a CFG for a field declaration, or an enum-constant 
* CFGExit - Represents an exit node of a CFG 
* CFGStatement - Represents a statement node of a CFG 
* CFGMerge - Represents a merge node of a CFG 
* ControlFlow - Represents control flow between CFG nodes 

### PDG (Program Dependence Graph) 

JxPlatform2 creates a PDG from a CFG for each method existing in Java source code. 

* PDGStore - Stores a collection of PDGs created from CFGs 
* PDG - Provides information about a PDG 
* ClDG - Provides information about a class dependence graph (ClDG) 
* SDG - Provides information about a system dependence graph (SDG) 
* Slice - Provides information about a program slice 

Each PDG consists of nodes and edges between two nodes. 

* PDGNode - Represents a node of a PDG 
* PDGEntry- Represents an entry node of a PDG for a method, a constructor, a field declaration, or an enum-constant 
* PDGClassEntry - Represents an entry node of a ClDG for a class or an interface 
* PDGStatement - Represents a statement node of a PDG 
* DependenceEdge - Represents dependence between PDG nodes 
* CD - Represents control dependence between PDG nodes 
* DD - Represents data dependence between PDG nodes 
* CallEdge - Represents a call edge in a ClDG 
* ClassMemberEdge - Represents a class member edge in a ClDG 

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

    java -jar jxplatform-1.0-all.jar -target xxx/ -classpath 'xxx/lib/*' -name name -logfile xxx.log

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

    JavaModelBuilder builder = new JavaModelBuilder(name, target, classpath);
    builder.setVisible(true);  // Displays log information on console
    JavaProject jproject = builder.build();
    ...
    builder.unbuild();

### As an Eclipse plug-in

The following is the typical code for building a Java model for the source code within the Eclipse project:

    org.eclipse.jdt.core.IJavaProject project;  // Possible to use org.eclipse.core.resources.IProject
    ModelBuilderPlugin modelBuilder = new ModelBuilderPlugin();
    JavaProject jproject = modelBuilder.build(project);

The plug-in automatically collects source files that was modified after the previous build.
Thus, the dirty source files will be analyzed if your code will perform build.
Use the `buildWhole(project)` method for clean re-build.  

### Building CFGs and PDGs

UNDER FIXING BUGS -- Thanks for your waiting.

CFGStore and PDGStore classes provides APIs for building CFGs and PDGs from the Java model.

## Author

[Katsuhisa Maruyama](http://www.fse.cs.ritsumei.ac.jp/~maru/index.html)
