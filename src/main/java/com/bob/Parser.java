package com.bob;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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
        for (AnnotationDeclaration decorator : decorators) {
            List<MethodDeclaration> decoratedMethods = getDecoratedMethods(decorator, outputDir);
            for (MethodDeclaration decoratedMethod : decoratedMethods) {
                System.out.println("Applying decoration " + decorator.getName().asString() +
                        " to method: " + decoratedMethod.getName().asString());

                applyDecoration(decorator, decoratedMethod);
            }
        }
    }

    public static void initializeOutputDirectory(File projectDir, File outputDir) {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        try {
            FileUtils.copyDirectory(projectDir, outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void applyDecoration(AnnotationDeclaration decorator, MethodDeclaration method) {
        BlockStmt methodBody = method.getBody().orElseThrow();
        MethodDeclaration decoratorMethod = getDecoratorApplication(decorator);

        MethodDeclaration decoratedMethod = decoratorMethod.clone();
        BlockStmt decoratedMethodBody = decoratedMethod.getBody().orElseThrow();
        NodeList<Statement> decoratorStatementNodes = decoratedMethodBody.getStatements();

        for (Statement statement : decoratorStatementNodes) {
            if (statement.isExpressionStmt()) {
                Expression expression = statement.asExpressionStmt().getExpression();
                if (expression.isMethodCallExpr()) {
                    if (expression.asMethodCallExpr().getName().asString().equals("proceed")) {
                        statement.replace(methodBody);
                    }
                }
            }
        }

        method.setBody(decoratedMethodBody);
        writeWithCompilationUnit(method.findCompilationUnit().get());
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
                .toList().getFirst().asClassOrInterfaceDeclaration().getMethodsByName("apply").getFirst();
    }


    public static List<MethodDeclaration> getDecoratedMethods(AnnotationDeclaration decorator, File directory) {
        ArrayList<MethodDeclaration> decoratedMethods = new ArrayList<>();
        addDecoratedMethodsToArray(decoratedMethods, decorator, directory);
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


    public static boolean isDecorated(MethodDeclaration methodDeclaration, AnnotationDeclaration decorator) {
        return methodDeclaration.getAnnotations().stream()
                .map(annotationExpr -> annotationExpr.getName().asString())
                .anyMatch(annotationName -> annotationName.equals(decorator.getName().asString()));
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
