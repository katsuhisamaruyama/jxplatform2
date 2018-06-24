# JxPlatform2 (JxPlatform v2) 

JxPlatform is a module that provides three types of easy-to-use models of Java source code. It facilitates the development and maintenance of various kinds of software tools. 

### Source Code Model 

JxPlatform builds a Java model consisting of the following elements: 

* JavaProject - Stores a collection of Java source files, packages, and classes 
* JavaFile - Provides information on a Java source file 
* JavaPackage - Provides information on a package 
* JavaClass - Provides information on a class, an interface, an enum, or an enum constant 
* JavaMethod - Provides information on a method, a constructor, or an initializer 
* JavaField (extends JavaVaraible) - Provides information on a field variable 
* JavaLocal (extends JavaVaraible) - Provides information on a local variable or a parameter 

### CFG (Control Flow Graph) 

JxPlatform creates a CFG for each method existing in Java source code. 

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

JxPlatform creates a PDG from a CFG for each method existing in Java source code. 

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

### From a release

You can directly download jar files of jxplatform2 on [GitHub](<https://github.com/katsuhisamaruyama/jxplatform2/tree/master/org.jtool.eclipse/releases>). 

### From the sources

You can build jxplatform2 with the following commands: 

    git clone https://github.com/katsuhisamaruyama/jxplatform2/
    cd jxplatform2/org.jtool.eclipse
    ./gradlew build jar shadowJar

Jar files of jxplatform2 can be found in the 'build/libs' folder.

## Usage

### As a stand-alone application

`jxplatform-1.0-all.jar` is an executable jar file. For example, when you put Java source code under the `xxx` folder (Java source files are expanded under the folder), the following command builds a Java model for the source code.

    java -jar jxplatform-1.0-all.jar -target xxx/ -classpath 'xxx/lib/*' -name name -logfile xxx.log

* `-classpath` specifies class paths where needed libraries are contained 
* `-name` specifies the name of a project managed in jxplatform2 
* `-logfile` specifies the name of a file in which the result of analysis is written 

These three options can be eliminated if they are needless. 

If your stand-alone application employs jxplatform2, you should use `jxplatform-1.0-lib.jar` instead of `jxplatform-1.0-all.jar`. The following API calls build a Java model. 

    JavaModelBuilder builder = new JavaModelBuilder(name, target, classpath);
    builder.build(true);

### As an Eclipse plug-ins

You put `jxplatform-1.0-lib.jar` in the 'plug-ins' directory under the Eclipse. Eclipse needs to be restarted. 

Your code builds a Java model for the source code and return a project containing the generated model with either of the following two API calls:

    ProjectManager.getInstance().build(IJavaProject project);
    ProjectManager.getInstance().buildWhole(IJavaProject project);

### Building CFGs and PDGs

CFGStore and PDGStore classes provides APIs for building CFGs and PDGs from the Java model.

## Author

[Katsuhisa Maruyama](http://www.fse.cs.ritsumei.ac.jp/~maru/index.html)
