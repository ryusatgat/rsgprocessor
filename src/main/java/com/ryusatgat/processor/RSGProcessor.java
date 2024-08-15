package com.ryusatgat.processor;

import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

@SupportedAnnotationTypes("com.ryusatgat.processor.RSGBigDecimal")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class RSGProcessor extends AbstractProcessor {
    private Messager msg;
    private Trees trees;
    private Context context;
    private TreePathScanner<Object, CompilationUnitTree> scanner;
    private BigDecimalExpression bigDecimalExpression;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        final JavacProcessingEnvironment env = (JavacProcessingEnvironment) processingEnv;
        this.trees = Trees.instance(processingEnv);
        this.context = env.getContext();
        
        bigDecimalExpression = new BigDecimalExpression(context);

        msg = processingEnv.getMessager();

        this.scanner = new TreePathScanner<>() {
            @Override
            public BinaryTree visitBinary(BinaryTree node, CompilationUnitTree unitTree) {
                TreePath currentPath = getCurrentPath();
                replaceBinaryOperation(currentPath);

                return (BinaryTree)super.visitBinary(node, unitTree);
            }

            private void replaceBinaryOperation(TreePath path) {
                JCTree.JCExpression newMethodInvocation = bigDecimalExpression.convert(path.getLeaf().toString());
                JCTree parent = (JCTree) path.getParentPath().getLeaf();

                if (parent instanceof JCTree.JCExpressionStatement jCExpressionStatement) {
                    jCExpressionStatement.expr = newMethodInvocation;
                } else if (parent instanceof JCTree.JCVariableDecl jCVariableDecl) {
                    jCVariableDecl.init = newMethodInvocation;
                } else if (parent instanceof JCTree.JCMethodInvocation jCMethodInvocation) {
                    List<JCTree.JCExpression> newArgs = List.nil();
                    
                    for (JCTree.JCExpression arg : jCMethodInvocation.args) {
                        if (arg == path.getLeaf()) {
                            newArgs = newArgs.append(newMethodInvocation);
                        } else {
                            newArgs = newArgs.append(arg);
                        }

                        jCMethodInvocation.args = newArgs;
                    }
                } else if (parent instanceof JCTree.JCReturn jCReturn) {
                    jCReturn.expr = newMethodInvocation;
                } else if (parent instanceof JCTree.JCParens parensNode) {
                    parensNode.expr = newMethodInvocation;
                } else {
                    if (parent != null)
                        System.out.println("Not found!!! --> " + parent.getKind().toString());
                }
            }                    
        };

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RSGBigDecimal.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                msg.printMessage(Diagnostic.Kind.ERROR, "Bad annotation position: " + element.getKind().toString());
            } else {
                if (Objects.nonNull(scanner)) {
                    final TreePath path = trees.getPath(element);
                    // msg.printMessage(Diagnostic.Kind.NOTE, "processing >> " + element.getSimpleName().toString());
                    scanner.scan(path, path.getCompilationUnit());
                }
            }

        }

        return true;
    }

/*
    public String findMethodName(TreePath path) {
        while (path != null) {
            if (path.getLeaf() instanceof JCMethodDecl methodDecl) {
                return methodDecl.name.toString();
            }
            path = path.getParentPath();
        }

        return null;
    }
    
    private void buildVariableTypes(TreePath path) {
        path.getCompilationUnit().getTypeDecls().forEach(typeDecl -> {
            if (typeDecl instanceof JCClassDecl classDecl) {
               for (JCTree member : classDecl.defs) {
                    if (member instanceof JCMethodDecl methodDecl) {
                        JCTree.JCBlock body = methodDecl.getBody();
                        if (body != null) {
                            body.stats.forEach(stmt -> {
                                if (stmt instanceof JCVariableDecl varDecl) {
                                    System.out.println("Variable: " + varDecl.getName() + " " + varDecl.getType());
                                }
                            });
                        }
                    }
                }
            }
        });
    }
*/

}
