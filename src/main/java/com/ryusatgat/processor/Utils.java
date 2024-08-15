package com.ryusatgat.processor;

import java.util.Stack;

public class Utils {
    public static String convertBigdecimal(String expression) {
        expression = expression.replaceAll("\\s+", "");
        Stack<String> operators = new Stack<>();
        Stack<String> operands = new Stack<>();
        Stack<Integer> precedenceStack = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            char ch = expression.charAt(i);

            if (Character.isLetter(ch)) {
                StringBuilder identifier = new StringBuilder();
                while (i < expression.length() && Character.isLetter(expression.charAt(i))) {
                    identifier.append(expression.charAt(i));
                    i++;
                }
                i--;
                operands.push(identifier.toString());
            } else if (Character.isDigit(ch) || ch == '.') {
                StringBuilder num = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    num.append(expression.charAt(i));
                    i++;
                }
                i--;
                operands.push("new BigDecimal(\"" + num.toString() + "\")");
            } else {
                switch (ch) {
                case '(' -> {
                    operators.push(String.valueOf(ch));
                    precedenceStack.push(-1);
                    }
                case ')' -> {
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processOperator(operators, operands);
                    }   operators.pop();
                    precedenceStack.pop();
                    }
                case '+', '-', '*', '/' -> {
                    while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(String.valueOf(ch))) {
                        processOperator(operators, operands);
                    }   operators.push(String.valueOf(ch));
                    }
                case '=' -> {
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                        i++;
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence("==")) {
                            processOperator(operators, operands);
                        }
                        operators.push("==");
                    }
                    }
                case '!' -> {
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                        i++;
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence("!=")) {
                            processOperator(operators, operands);
                        }
                        operators.push("!=");
                    }
                    }
                case '&' -> {
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '&') {
                        i++;
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence("&&")) {
                            processOperator(operators, operands);
                        }
                        operators.push("&&");
                    }
                    }
                case '|' -> {
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '|') {
                        i++;
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence("||")) {
                            processOperator(operators, operands);
                        }
                        operators.push("||");
                    }
                    }
                case '>' -> {
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                        i++; // Skip the next '='
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(">=")) {
                            processOperator(operators, operands);
                        }
                        operators.push(">=");
                    } else {
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(">")) {
                            processOperator(operators, operands);
                        }
                        operators.push(">");
                    }
                    }
                case '<' -> {
                    if (i + 1 < expression.length() && expression.charAt(i + 1) == '=') {
                        i++; // Skip the next '='
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence("<=")) {
                            processOperator(operators, operands);
                        }
                        operators.push("<=");
                    } else {
                        while (!operators.isEmpty() && precedence(operators.peek()) >= precedence("<")) {
                            processOperator(operators, operands);
                        }
                        operators.push("<");
                    }
                    }
                default -> {
                    }
                }
            }
            i++;
        }

        while (!operators.isEmpty()) {
            processOperator(operators, operands);
        }

        return operands.pop();
    }

    private static void processOperator(Stack<String> operators, Stack<String> operands) {
        String op2 = operands.pop();
        String op1 = operands.pop();
        String op = operators.pop();
        operands.push(applyOperation(op1, op2, op));
    }

    private static int precedence(String op) {
        return switch (op) {
            case "*", "/" -> 2;
            case "+", "-" -> 1;
            case ">", ">=", "<", "<=" -> 0;
            case "==", "!=" -> -1;
            case "&&" -> -2;
            case "||" -> -3;
            default -> 0;
        };
    }

    private static String applyOperation(String op1, String op2, String op) {
        return switch (op) {
            case "+" -> op1 + ".add(" + op2 + ")";
            case "-" -> op1 + ".subtract(" + op2 + ")";
            case "*" -> op1 + ".multiply(" + op2 + ")";
            case "/" -> op1 + ".divide(" + op2 + ")";
            case "==" -> op1 + ".compareTo(" + op2 + ") == 0";
            case "!=" -> op1 + ".compareTo(" + op2 + ") != 0";
            case ">" -> op1 + ".compareTo(" + op2 + ") > 0";
            case ">=" -> op1 + ".compareTo(" + op2 + ") >= 0";
            case "<" -> op1 + ".compareTo(" + op2 + ") < 0";
            case "<=" -> op1 + ".compareTo(" + op2 + ") <= 0";
            case "&&" -> "(" + op1 + " != BigDecimal.ZERO && " + op2 + " != BigDecimal.ZERO)";
            case "||" -> "(" + op1 + " != BigDecimal.ZERO || " + op2 + " != BigDecimal.ZERO)";
            default -> "";
        };
    }
}
