package com.bob;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import javassist.compiler.ast.MethodDecl;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {

    public static void parse(File projectDir, File outputDir) {
        initializeOutputDirectory(projectDir, outputDir);

        List<AnnotationDeclaration> decorators = getDecorators(outputDir);
        List<String> decoratorNames = decorators.stream()
                .map(AnnotationDeclaration::getName)
                .map(SimpleName::asString)
                .toList();

        //for (AnnotationDeclaration decorator : decorators) {
        //    List<MethodDeclaration> decoratedMethods = getDecoratedMethods(decorator, outputDir);
        //    for (MethodDeclaration decoratedMethod : decoratedMethods) {
        //        System.out.println("Applying decoration " + decorator.getName().asString() +
        //                " to method: " + decoratedMethod.getName().asString());
        //
        //        applyDecoration(decorator, decoratedMethod);
        //    }
        //}

        List<MethodDeclaration> decoratedMethods = getDecoratedMethods(decorators, outputDir);
        for (MethodDeclaration decoratedMethod : decoratedMethods) {
            System.out.println("Applying decoration to method: " + decoratedMethod.getName().asString());
            applyDecoration(decorators, decoratedMethod);
        }


    }

    public static void initializeOutputDirectory(File projectDir, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        try {
            FileUtils.copyDirectoryStructure(projectDir, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // visitor which replaces the proceed() call with the body of the decorated method
    private static final GenericVisitorAdapter<ExpressionStmt, BlockStmt> proceedExpressionReplacerVisitor =
            new GenericVisitorAdapter<ExpressionStmt, BlockStmt>() {

        public ExpressionStmt visit(ExpressionStmt expressionStmt, BlockStmt methodBody) {
            if (expressionStmt.getExpression().isMethodCallExpr()) {
                if (expressionStmt.getExpression().asMethodCallExpr().getName().asString().equals("proceed")) {
                    expressionStmt.replace(methodBody);
                }
            }
            return super.visit(expressionStmt, methodBody);
        }

    };


    public static void applyDecoration(AnnotationDeclaration decorator, MethodDeclaration method) {
        BlockStmt methodBody = method.getBody().orElseThrow();
        MethodDeclaration decoratorMethod = getDecoratorApplication(decorator);

        MethodDeclaration decoratedMethod = decoratorMethod.clone();
        BlockStmt decoratedMethodBody = decoratedMethod.getBody().orElseThrow();
        proceedExpressionReplacerVisitor.visit(decoratedMethodBody, methodBody);

        method.setBody(decoratedMethodBody);
        writeWithCompilationUnit(method.findCompilationUnit().get());
    }


    public static void applyDecoration(List<AnnotationDeclaration> decorators, MethodDeclaration method) {
        List<String> decoratorNames = decorators.stream()
                .map(AnnotationDeclaration::getName)
                .map(SimpleName::asString)
                .toList();

        for (AnnotationExpr decoratorExpr: method.getAnnotations().stream()
                .filter(annotationExpr -> decoratorNames.contains(annotationExpr.getName().asString())).toList()) {

            AnnotationDeclaration decorator = decorators.stream()
                .filter(annotationDeclaration -> annotationDeclaration.getName().asString()
                        .equals(decoratorExpr.getName().asString()))
                .toList().get(0);
            MethodDeclaration decoratorMethod = getDecoratorApplication(decorator);

            MethodDeclaration decoratedMethod = decoratorMethod.clone();
            BlockStmt decoratedMethodBody = decoratedMethod.getBody().orElseThrow();
            BlockStmt methodBody = method.getBody().orElseThrow();
            proceedExpressionReplacerVisitor.visit(decoratedMethodBody, methodBody);

            method.setBody(decoratedMethodBody);
            writeWithCompilationUnit(method.findCompilationUnit().get());
            System.out.println("Applied decoration: " + decoratorExpr.getName().asString());
        }
    }


    public static void writeWithCompilationUnit(CompilationUnit cu) {
        try {
            Files.write(cu.getStorage().get().getPath(), cu.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static MethodDeclaration getDecoratorApplication(AnnotationDeclaration decorator) {
        return decorator.getMembers().stream()
                .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                .toList().get(0).asClassOrInterfaceDeclaration().getMethodsByName("apply").get(0);
    }


    public static List<MethodDeclaration> getDecoratedMethods(AnnotationDeclaration decorator, File directory) {
        ArrayList<MethodDeclaration> decoratedMethods = new ArrayList<>();
        addDecoratedMethodsToArray(decoratedMethods, decorator, directory);
        return decoratedMethods;
    }

    public static List<MethodDeclaration> getDecoratedMethods(List<AnnotationDeclaration> decorators, File directory) {
        ArrayList<MethodDeclaration> decoratedMethods = new ArrayList<>();
        addDecoratedMethodsToArray(decoratedMethods, decorators, directory);
        return decoratedMethods;
    }


    public static void addDecoratedMethodsToArray(ArrayList<MethodDeclaration> arrayList,
                                                  AnnotationDeclaration decorator,
                                                  File file) {
        for (File thisFile : Objects.requireNonNull(file.listFiles())) {
            if (thisFile.isDirectory()) {
                addDecoratedMethodsToArray(arrayList, decorator, thisFile);
            } else {
                if (thisFile.getName().endsWith(".java")) {

                    CompilationUnit cu = null;
                    try {
                        cu = StaticJavaParser.parse(thisFile);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    for (TypeDeclaration<?> type : cu.getTypes().stream().toList()) {
                        if (type.isClassOrInterfaceDeclaration()) {
                            ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) type;
                            for (MethodDeclaration method : classDeclaration.getMethods()) {
                                if (isDecorated(method, decorator)) {
                                    arrayList.add(method);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void addDecoratedMethodsToArray(ArrayList<MethodDeclaration> arrayList,
                                                  List<AnnotationDeclaration> decorators,
                                                  File file) {
        for (File thisFile : Objects.requireNonNull(file.listFiles())) {
            if (thisFile.isDirectory()) {
                addDecoratedMethodsToArray(arrayList, decorators, thisFile);
            } else {
                if (thisFile.getName().endsWith(".java")) {

                    CompilationUnit cu = null;
                    try {
                        cu = StaticJavaParser.parse(thisFile);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    for (TypeDeclaration<?> type : cu.getTypes().stream().toList()) {
                        if (type.isClassOrInterfaceDeclaration()) {
                            ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) type;
                            for (MethodDeclaration method : classDeclaration.getMethods()) {
                                if (isDecorated(method, decorators)) {
                                    arrayList.add(method);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public static boolean isDecorated(MethodDeclaration methodDeclaration, AnnotationDeclaration decorator) {
        return methodDeclaration.getAnnotations().stream()
                .map(annotationExpr -> annotationExpr.getName().asString())
                .anyMatch(annotationName -> annotationName.equals(decorator.getName().asString()));
    }

    public static boolean isDecorated(MethodDeclaration methodDeclaration, AnnotationExpr annotationExpression) {
        return methodDeclaration.getAnnotations().stream()
                .map(annotationExpr -> annotationExpr.getName().asString())
                .anyMatch(annotationName -> annotationName.equals(annotationExpression.getName().asString()));
    }

    public static boolean isDecorated(MethodDeclaration methodDeclaration, List<AnnotationDeclaration> decorators) {
        List<String> decoratorNames = decorators.stream()
                .map(AnnotationDeclaration::getName)
                .map(SimpleName::asString)
                .toList();
        return methodDeclaration.getAnnotations().stream()
                .map(annotationExpr -> annotationExpr.getName().asString())
                .anyMatch(decoratorNames::contains);
    }


    public static List<AnnotationDeclaration> getDecorators(File directory) {
        ArrayList<AnnotationDeclaration> decorators = new ArrayList<>();
        addDecoratorsToArray(decorators, directory);
        return decorators;
    }

    public static void addDecoratorsToArray(ArrayList<AnnotationDeclaration> arrayList, File file) {
        for (File thisFile : Objects.requireNonNull(file.listFiles())) {
            if (thisFile.isDirectory()) {
                addDecoratorsToArray(arrayList, thisFile);
            } else {
                if (thisFile.getName().endsWith(".java")) {

                    CompilationUnit cu = null;
                    try {
                        cu = StaticJavaParser.parse(thisFile);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    for (TypeDeclaration<?> type : cu.getTypes().stream().toList()) {
                        if (isDecorator(type)) {
                            arrayList.add((AnnotationDeclaration) type);
                        }
                    }
                }
            }
        }
    }

    public static boolean isDecorator(TypeDeclaration<?> typeDeclaration) {
        if (typeDeclaration.isAnnotationDeclaration()) {
            AnnotationDeclaration annotationDeclaration = (AnnotationDeclaration) typeDeclaration;
            List<BodyDeclaration<?>> members = annotationDeclaration.getMembers();
            for (BodyDeclaration<?> member : members) {
                if (member.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration innerClass = (ClassOrInterfaceDeclaration) member;
                    for (ClassOrInterfaceType extendingClass : innerClass.getExtendedTypes()) {
                        if (extendingClass.getName().asString().equals("Decorator")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
