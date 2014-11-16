FLAGS = -cp ./bin/:./junit.jar:./hamcrest-core.jar:./junit-benchmarks.jar
EXECFLAGS = $(FLAGS)
COMPFLAGS = $(FLAGS) -Xlint:unchecked -sourcepath src/ -d ./bin/

all: test

test:
	javac $(COMPFLAGS) src/test/SetTest.java
	java $(EXECFLAGS) org.junit.runner.JUnitCore test.SetTest

clean:
	rm -r bin/*

snapshot:
	git archive --prefix=code/ -o code_snapshot.tar.gz HEAD
