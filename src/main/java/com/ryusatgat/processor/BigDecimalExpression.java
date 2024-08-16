package com.ryusatgat.processor;

import java.util.Stack;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class BigDecimalExpression {

    private final TreeMaker treeMaker;
    private final Names names;

    public BigDecimalExpression(Context context) {
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }

    public JCTree.JCExpression convert(String expression) {
        String postfix = infixToPostfix(expression);
        return postfixToBigDecimalExpression(postfix);
    }

    private String infixToPostfix(String infix) {
        StringBuilder output = new StringBuilder();
        Stack<String> operators = new Stack<>();
        StringBuilder operand = new StringBuilder();
        char[] chars = infix.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
    
            if (Character.isDigit(c) || c == '.') {
                operand.append(c);
            } else if (Character.isLetter(c)) {
                operand.append(c);
            } else {
                if (operand.length() > 0) {
                    output.append(operand.toString()).append(' ');
                    operand.setLength(0);
                }
                
                if (c == '(') {
                    operators.push(String.valueOf(c));
                } else if (c == ')') {
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        output.append(operators.pop()).append(' ');
                    }
                    if (!operators.isEmpty() && operators.peek().equals("(")) {
                        operators.pop();
                    }
                } else if (isOperator(c)) {
                    String currentOp = String.valueOf(c);

                    if (i < chars.length - 1 && (c == '=' || c == '!' || c == '<' || c == '>')) {
                        char nextChar = chars[i + 1];
                        if (nextChar == '=') {
                            currentOp += nextChar;
                            i++;
                        }
                    }
                    
                    while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(currentOp)) {
                        output.append(operators.pop()).append(' ');
                    }
                    operators.push(currentOp);
                }
            }
        }
        
        if (operand.length() > 0) {
            output.append(operand.toString()).append(' ');
        }
        
        while (!operators.isEmpty()) {
            output.append(operators.pop()).append(' ');
        }
    
        return output.toString().trim();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' ||
               c == '<' || c == '>' || c == '=' || c == '!' || c == '&' || c == '|';
    }

    private int precedence(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/", "%" -> 2;
            case "<", ">", "<=", ">=", "==", "!=", "<=>" -> 3;
            case "&" -> 4;
            case "|" -> 5;
            default -> -1;
        };
    }

    private JCTree.JCExpression postfixToBigDecimalExpression(String postfix) {
        Stack<JCTree.JCExpression> stack = new Stack<>();
        String[] tokens = postfix.split(" ");
        
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (isOperator(token.charAt(0))) {
                JCTree.JCExpression right = stack.pop();
                JCTree.JCExpression left = stack.pop();
                JCTree.JCExpression result = applyBigDecimalOperator(left, token, right);
                stack.push(result);
            } else {
                stack.push(createBigDecimalLiteralOrVariable(token));
            }
        }
    
        return stack.pop();
    }

    private JCTree.JCExpression applyBigDecimalOperator(JCTree.JCExpression left, String operator, JCTree.JCExpression right) {
        Name methodName;
        boolean isComparison = false;
        boolean isLogical = false;
        switch (operator) {
            case "+" -> methodName = names.fromString("add");
            case "-" -> methodName = names.fromString("subtract");
            case "*" -> methodName = names.fromString("multiply");
            case "/" -> methodName = names.fromString("divide");
            case "<", ">", "<=", ">=", "==", "!=" -> {
                methodName = names.fromString("compareTo");
                isComparison = true;
            }
            case "&&", "||" -> {
                isLogical = true;
                methodName = names.fromString(operator.equals("&&") ? "and" : "or");
            }
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        JCTree.JCExpression methodInvocation = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(left, methodName),
                List.of(right)
        );

        if (isComparison) {
            JCTree.Tag comparisonTag;
            int comparisonValue = 0;
    
            switch (operator) {
                case ">" -> comparisonTag = JCTree.Tag.GT;
                case ">=" -> comparisonTag = JCTree.Tag.GE;
                case "<" -> comparisonTag = JCTree.Tag.LT;
                case "<=" -> comparisonTag = JCTree.Tag.LE;
                case "==" -> comparisonTag = JCTree.Tag.EQ;
                case "!=" -> comparisonTag = JCTree.Tag.NE;
                default -> throw new IllegalStateException("Unexpected comparison operator: " + operator);
            }
    
            return treeMaker.Binary(comparisonTag, methodInvocation, treeMaker.Literal(comparisonValue));
        }

        if (isLogical) {
            return operator.equals("&&") ? treeMaker.Binary(JCTree.Tag.AND, left, right)
                                         : treeMaker.Binary(JCTree.Tag.OR, left, right);
        }

        return methodInvocation;
    }

    private JCTree.JCExpression createBigDecimalLiteralOrVariable(String token) {
        if (isNumber(token)) {
            return treeMaker.NewClass(
                    null,
                    List.nil(),
                    treeMaker.Ident(names.fromString("BigDecimal")),
                    List.of(treeMaker.Literal(token)),
                    null
            );
        } else if (token.contains(".")) {
            String[] parts = token.split("\\.");
            JCTree.JCExpression methodExpr = treeMaker.Ident(names.fromString(parts[0]));
            for (int i = 1; i < parts.length; i++) {
                methodExpr = treeMaker.Select(methodExpr, names.fromString(parts[i]));
            }
            return treeMaker.Apply(List.nil(), methodExpr, List.nil());
        } else {
            return treeMaker.Ident(names.fromString(token));
        }
    }

    private boolean isNumber(String token) {
        try {
            new java.math.BigDecimal(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}