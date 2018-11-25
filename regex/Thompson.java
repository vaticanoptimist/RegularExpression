package regex;

import java.util.Scanner;

public class Thompson {
	

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String line;
		System.out.print("Enter a regular expression: ");
		line = sc.nextLine();

		RegularExpression regex = new RegularExpression(line);
		if (!regex.isValid()) {
			System.out.println("Not a valid or supported regular expression. It only supports:\n1) open and close parenthesis\n2) 'a' - 'z'\n3) '|'");
		} else {
			NondeterministicFiniteAutomata nfa = new NondeterministicFiniteAutomata(regex);
			System.out.print("Enter a string: ");
			while(sc.hasNextLine() ) {
				line = sc.nextLine();
				if (line.equals("QUIT")) break;
				System.out.println("                        " + nfa.evaluate(line));
				System.out.print("Enter a string: ");
			}
		}
		sc.close();
	}

}