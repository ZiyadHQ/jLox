jLox is an implementation of the Interpreter described in the popular book 'CRAFTING INTERPRETERS' by Robert Nystrom.

the project is written in VSC, it uses a simple .bat script to make it easier to build, but due to the way java runs it shouldn't be an issue as long as the '.java' files are in the same directory.

the details of 'lox', which is the language implemented using jLox are found within the repository 'craftinginterpreters':

	https://github.com/munificent/craftinginterpreters

build scripts:
	run.bat - compiles and runs the base directory's .java files.
	tool/compile_grammer.bat - compiles the grammar rules into a Expr.java file and puts the result into the base directory of the project.