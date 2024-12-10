import glob
import os
import subprocess
import sys

# Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TOOL_DIR = os.path.join(BASE_DIR, "tool")
OUTPUT_DIR = "D:/jLox/"

def run_command(commands, cwd=None):

    try:
        result = subprocess.run(commands, cwd=cwd, check=True, capture_output=True, text=True)
        print(result.stdout)
    except subprocess.CalledProcessError as e:
        print(f"Error: {e.stderr}")
        sys.exit(1)
    
def compile_grammar():
    print("compiling the grammar..")
    run_command(["java", "GenerateAst.java", OUTPUT_DIR], cwd=TOOL_DIR)

def compile_source():
    print("compiling the interpreter source..")
    run_command(["javac", "*.java"], cwd=BASE_DIR)

def clean_directory():
    print("cleaning the source directory")
    run_command(["del", "*.class"], cwd=BASE_DIR)

def clean_directories():
    print("cleaning the directory..")
    for file_path in glob.glob("*.class"):
        os.remove(file_path)
    for file_path in glob.glob("*.txt"):
        os.remove(file_path)

def run_lox(input_file, output=sys.stdout):
    print("running lox..")
    # run_command(["java", "Lox", input_file], cwd=BASE_DIR)
    try:
        process = subprocess.Popen(
        ["java", "Lox", input_file],
        cwd=BASE_DIR,
        stdin=sys.stdin,
        stdout=output,
        stderr=sys.stderr,
        text=True
        )
        process.wait()
    except Exception as e:
        print(f"Error running Lox: {e}")

def run_lox_repl(output=sys.stdout):
    print("running lox repl..")
    try:
        process = subprocess.Popen(
        ["java", "Lox"],
        cwd=BASE_DIR,
        stdin=sys.stdin,
        stdout=output,
        stderr=sys.stderr,
        text=True
        )
        process.wait()
    except KeyboardInterrupt:
        print("\nExiting Lox REPL.")
    except Exception as e:
        print(f"Error running Lox REPL: {e}")

def help():
    print("Usage:")
    print("       py build.py <input_file>.lox : interpret the <input_file>.lox file, seperate from the -f option")
    print("       py build.py help : instructions on how to use the build tool")
    print("       py build.py repl : runs in REPL mode")
    print("       py build.py clean : cleans the directories by removing .class and .txt files")
    print("       py build.py test : for testing commands, prints the entire program state to stdout")
    print("       py build.py build - just builds the project, in reality all commands do that except for clean, build merely exits without errors")
    print("Options must come before commands, Options:")
    print("       -file : instead of printing output to the terminal, print it to an output.txt file")
    print("       -f <filename> : same as -file, but prints to a user specified output file, doesn't work well with \'repl\'")

if len(sys.argv) < 2:
    help()
    sys.exit(1)


# the command is always the last parameter
command = sys.argv[len(sys.argv) - 1]

if command == "clean":
    clean_directories()
    sys.exit(0)

# variables to determine behavior
option = ""
target_file = sys.stdout

# assign the command parameters depending on command length
if len(sys.argv) == 3:
    option = sys.argv[1]
    if option == "-file":
        target_file = open("output.txt", "w")
    elif option == "-f":
        print("Error, '-f' needs a specified output file after it!")
        help()
        sys.exit(1)
if len(sys.argv) == 4:
    option = sys.argv[1]
    target_file = open(sys.argv[2], "w")

#build steps
compile_grammar()
compile_source()

if command.endswith(".lox"):
    run_lox(input_file=command, output=target_file)
    if target_file is not sys.stdout:
        target_file.close()
elif command == "help":
    help()
elif command == "repl":
    run_lox_repl(output=target_file)
elif command == "test":
    print(f"command: {command}, option: {option}, target_file: {target_file}")
elif command == "build":
    sys.exit(0)
else:
    print(f"ERROR: the command {command} is invalid")
    help()