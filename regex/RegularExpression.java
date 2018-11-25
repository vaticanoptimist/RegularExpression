package regex;

import java.util.Stack;

public class RegularExpression implements InfixToPostfixParser {
	private String regex;
	
	public RegularExpression() {
		this("");
	}
	
	public RegularExpression(String s) {
		regex = s;
	}
	
	public boolean isValid() {
		int length = regex.length();
		for (int i = 0; i < length; i++) {
			char c = regex.charAt(i);
			if (!RegularExpression.isAlphabet(c)
					&& !RegularExpression.isOperator(c)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isAlphabet(Character c) { return c >= 'a' && c <= 'z'; }
	public static boolean isOperator(Character c) {
		return c == '|' || c == '(' || c == ')' || c == '+' || c == '*';
	}
	
	@Override
	public String getPostfix() {
		StringBuilder sb = new StringBuilder();
		Stack<Character> stack = new Stack<>();
		/*
		 * track whether the previous character is an alphabet, if so, we need to add '.' operator to translate
		 * "ab" into "ab." so that we know this is a concatenation operation.
		 * Using stack since need to track at each level created through open-close parenthesis
		 */
		Stack<Boolean> bStack = new Stack<>(); 
		
		int length = regex.length();
		bStack.push(false);
		for (int i = 0; i < length; i++) {
			char c = regex.charAt(i);
			
			if (RegularExpression.isAlphabet(c)) {
				sb.append(c);
				
				if (bStack.peek()) {
					stack.push('.');
				}
				
				bStack.pop();
				bStack.push(true);
			} else if (c == '(') {
				while (!stack.isEmpty()) {
					sb.append(Character.toString(stack.pop()));
				}
				stack.push(c);
				bStack.push(false);
			} else if (c == ')') {
				while (!stack.isEmpty() && stack.peek() != '(') {
					sb.append(Character.toString(stack.pop()));
				}
				stack.pop();
				
				bStack.pop();
				if (bStack.peek()) {
					stack.push('.');
				}
				bStack.pop();
				bStack.push(true);
			} else if (isUnaryOperator(c)) {
				sb.append(Character.toString(c));
			} else {
				while (!stack.isEmpty() 
						&& precedence(stack.peek()) > precedence(c)) {
					sb.append(Character.toString(stack.pop()));
				}
				stack.push(c);
				bStack.pop();
				bStack.push(false);
			}
		}
		
		while (!stack.isEmpty()) {
			sb.append(Character.toString(stack.pop()));
		}
		return sb.toString();
	}
	
	private int precedence(Character c) {
		switch (c) {
			case '.': return 5;
			case '|': return 4;
			default: return 0;
		}
	}
	
	private boolean isUnaryOperator(Character c) { 
		return c == '*' || c == '+';
	}
}
