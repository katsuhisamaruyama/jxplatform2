
SOURCES = $(wildcard *.java)
CLASSES = $(patsubst %.java,%.class,$(SOURCES))
JUNIT_CP = ../junit-4.12.jar:../hamcrest-core-1.3.jar
CLASSPATH = .

all:	compile

compile: $(CLASSES)

clean:
	rm -f *.class *.java~

%.class: %.java
	javac -cp $(CLASSPATH) $<
