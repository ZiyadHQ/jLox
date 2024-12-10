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

def run_lox(input_file):
    print("running lox..")
    run_command(["java", "Lox", input_file], cwd=BASE_DIR)

def run_lox_repl():
    print("running lox repl..")
    try:
        process = subprocess.Popen(
        ["java", "Lox"],
        cwd=BASE_DIR,
        stdin=sys.stdin,
        stdout=sys.stdout,
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
    print("       py build.py <input_file>.lox")
    print("       py build.py help")
    print("       py build.py repl")

if len(sys.argv) < 2:
    help()
    sys.exit(1)

input = sys.argv[1]

#build steps
compile_grammar()
compile_source()

if input.endswith(".lox"):
    run_lox(input_file=input)
elif input == "help":
    help()
elif input == "repl":
    run_lox_repl()
else:
    print(f"ERROR: the command {input} is invalid")
    help()