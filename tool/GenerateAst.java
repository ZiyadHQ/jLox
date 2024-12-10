package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {

        if(args.length != 1){
            System.err.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal : Object value",
            "Unary : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        // commented since its only used in the book and not in my implementation
        // writer.println("package com.craftinginterpreters.lox");
        // writer.println();
        writer.println("import Java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");
        writer.println();

        // the loop for writing each type's class
        for(String type : types){
            // the tokens of the type definition string, not related to the interpreters
            String[] toks = type.split(":");

            String className = toks[0].trim();
            String fields = toks[1].trim();

            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
        writer.println("    static class " + className + " extends " + baseName + "{");

        // constructor
        writer.println("    " + className + "("+fieldList+")" + "{");

        String[] fields = fieldList.split(", ");

        for(String field : fields){
            // assign a value to each field of the object
            // we extract 'name' because the field variable contains both the type of the var
            // and the name of it, we only need the name here as we have defined the types in the function
            // parameter list.
            String name = field.split(" ")[1];
            writer.println("        this." + name + " = " + name + ";");
        }
        writer.println("    }");

        // the definition of each field of this object
        for(String field : fields){
            writer.println("    final " + field + ";");
        }
        writer.println("    }");
    }
}
