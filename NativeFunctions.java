import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NativeFunctions {

    public static void registerFunctions(Environment globals) {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                var time = (double) System.currentTimeMillis() / 1000.0;
                return time;
            };

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(arguments.get(0));
                return null;
            }
        });

        globals.define("sleep", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {

                try {
                    Thread.sleep((long) Math.ceil((double) arguments.get(0)));
                } catch (InterruptedException e) {
                    throw new RuntimeError(null, "Error, sleep() can only accept number values");
                }

                return null;
            }
        });

        globals.define("readTextFile", new LoxCallable() {
            @Override
            public int arity() {return 1;}

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                try{
                    return Files.readString(Path.of(arguments.get(0).toString()));
                }
                catch (Exception e){return false;}
                
            }
        });
    }

}
