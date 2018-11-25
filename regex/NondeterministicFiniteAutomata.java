package regex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

class Edge {
	State source;
	Character symbol; // null value represent empty character
	State next;
	
	public Edge(State s, Character c) {
		this(s, c, null);
	}
	
	public Edge(State s, Character c, State n) {
		source = s;
		symbol = c;
		next = n;
	}
}

class State {
	List<Edge> edges;
	
	public State() {
		edges = new ArrayList<Edge>();
		edges.add(new Edge(this, null));
	}
	
	public State(Character c) {
		edges = new ArrayList<Edge>();
		edges.add(new Edge(this, c));
	}
}

public class NondeterministicFiniteAutomata {
	
	private State initialState;
	private List<Edge> outgoingEdges;
	
	private NondeterministicFiniteAutomata(State s) {
		initialState = s;
		outgoingEdges = new ArrayList<Edge>();
		for (Edge e : s.edges) {
			outgoingEdges.add(e);
		}
	}
	
	private NondeterministicFiniteAutomata(Character c) {
		this(new State(c));
	}
	
	// construct a new NFA that is either nfa1 or nfa2
	private NondeterministicFiniteAutomata(NondeterministicFiniteAutomata nfa1,
											NondeterministicFiniteAutomata nfa2) {
		initialState = new State();
		initialState.edges.add(new Edge(initialState, null, nfa1.initialState));
		initialState.edges.add(new Edge(initialState, null, nfa2.initialState));
		
		outgoingEdges = new ArrayList<Edge>();
		for (Edge e : nfa1.outgoingEdges) {
			outgoingEdges.add(e);
		}
		for (Edge e : nfa2.outgoingEdges) {
			outgoingEdges.add(e);
		}
	}
	
	public NondeterministicFiniteAutomata() {
		initialState = new State();
		outgoingEdges = new ArrayList<Edge>();
		for (Edge e : initialState.edges) {
			outgoingEdges.add(e);
		}
	}
	
	public NondeterministicFiniteAutomata(RegularExpression regex) {
		String postfix = regex.getPostfix();
		//System.out.println(postfix);
		
		NondeterministicFiniteAutomata nfa, nfa1, nfa2, newNFA;
		
		Stack<NondeterministicFiniteAutomata> stack = new Stack<>();
		int length = postfix.length();
		for (int i = 0; i < length; i++) {
			char c = postfix.charAt(i);
			
			if (RegularExpression.isAlphabet(c)) {
				stack.push(new NondeterministicFiniteAutomata(c));
			} else {
				switch (c) {
					// concatenate
					case '.':
						nfa2 = stack.pop();
						nfa1 = stack.pop();
						nfa1.append(nfa2);
						stack.push(nfa1);
						break;
					
					// union
					case '|':
						nfa2 = stack.pop();
						nfa1 = stack.pop();
						newNFA = new NondeterministicFiniteAutomata(nfa1, nfa2);
						stack.push(newNFA);
						break;
					
					// one or more
					case '+':
						nfa = stack.pop();
						for (Edge e : nfa.outgoingEdges) {
							State source = e.source;
							Edge newEdge = new Edge(source, e.symbol, nfa.initialState);
							
							source.edges.add(newEdge);
						}
						stack.push(nfa);
						break;
					
					// zero or more
					case '*':
						nfa = stack.pop();
						for (Edge e : nfa.outgoingEdges) {
							State source = e.source;
							source.edges.remove(e);
							
							Edge newEdge = new Edge(source, e.symbol, nfa.initialState);
							source.edges.add(newEdge);
						}

						Edge out = new Edge(nfa.initialState, null);
						nfa.initialState.edges.add(out);
						nfa.outgoingEdges.clear();
						nfa.outgoingEdges.add(out);
						stack.push(nfa);
						break;
				}
			}
		}
		
		nfa = stack.pop();
		this.initialState = nfa.initialState;
		this.outgoingEdges = nfa.outgoingEdges;
	}
	
	public boolean evaluate(String input) {
		Set<State> currentStates = new HashSet<>();
		currentStates.add(initialState);
		addReachableStatesThroughNullEdges(initialState, currentStates);
		
		int length = input.length();
		for (int i = 0; i < length; i++) {
			char c = input.charAt(i);
			Set<State> next = step(currentStates, c);
			currentStates = next;
		}
		return isMatch(currentStates);
	}
	
	private Set<State> step(Set<State> currentStates, Character c) {
		Set<State> nextStates = new HashSet<State>();
		
		for (State s : currentStates) {
			match(s, c, nextStates);
		}
		
		return nextStates;
	}
	
	private void match(State s, Character c, Set<State> states) {
		if (s == null) return;
		
		for (Edge e : s.edges) {
			if (e.symbol == c) {
				states.add(e.next);
				addReachableStatesThroughNullEdges(e.next, states);
			}
		}
	}
	
	private void addReachableStatesThroughNullEdges(State s, Set<State> states) {
		if (s == null) return;
		
		for (Edge e : s.edges) {
			if (e.symbol == null) {
				states.add(e.next);
				addReachableStatesThroughNullEdges(e.next, states);
			}
		}
	}
	
	private boolean isMatch(Set<State> states) {
		return states.contains(null);
	}
	
	private void append(NondeterministicFiniteAutomata nextNFA) {
		for (Edge e : outgoingEdges) {
			e.next = nextNFA.initialState;
		}
		outgoingEdges = nextNFA.outgoingEdges;
	}
}