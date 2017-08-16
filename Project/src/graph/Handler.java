/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author Andres
 */
public class Handler {
	
	public static int SCREEN_SIZE = 600;
	public static int EXTRA_X = 8;
	public static int EXTRA_Y = 31;
	public static int WINDOW_Y = 60;
	
	public static float PRECISION = 1f;
	public static float SCALE = 60f;
	
	private static ArrayList<Point> points;
	private static String lastString = "";
	
	public static void init() {
		new Window();
		points = new ArrayList<>();
//		shuntingYard("3 + 4 * 2 / ( 1 − 5 ) ^ 2 ^ 3");
	}
	
	/**
	 * devuelve el color dado por parametros en HSB
	 * @param h
	 * @param s
	 * @param b
	 * @return 
	 */
	public static Color color(double h, double s, double b) {
		return Color.getHSBColor((float)(h/360f),(float)(s/100f),(float)(b/100f));
	}
	
	public static boolean graph(String text) {
		try {
//			System.out.println("Raw function: "+text);
//			ArrayList<Token> tokens = convertEquation(text);
//			System.out.println("Converted function: "+printList(tokens));
//			tokens = redact(tokens);
//			System.out.println("Redacted function: "+printList(tokens));
//			tokens = shuntingYard(tokens);
//			System.out.println("Ordered function: "+printList(tokens));
//			solve(tokens);
			if (!lastString.equals(text)) {
				lastString = text;
				eval(text);
			}
			
			return true;
		} catch(Exception e) {
//			System.out.println("Exception: "+e.getMessage());
//			System.out.println("Aiuda");
			return false;
		}
	}
	
	public static String printList(ArrayList<Token> list) {
		String a = "";
		for (int i = 0; i < list.size(); i++) {
			a = a + list.get(i) + " ";
		}
		return a;
	}
	
	public static void eval(String eq) throws Exception {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		eq = eq.replace("cos", "Math.cos");
		eq = eq.replace("sin", "Math.sin");
		eq = eq.replace("tan", "Math.sin");
		eq = eq.replace("abs","Math.abs");
		eq = eq.replace("pow","Math.pow");
		points.clear();
		for (int x = 0; x < 1000; x++) {
			double fullX = (x-500)/100f;
			double res = (double)engine.eval(eq.replace("x", ""+fullX).replace("random",""+Math.random()));
			points.add(new Point((fullX*SCALE)+(SCREEN_SIZE/2),(SCREEN_SIZE/2)-(res*SCALE)));
		}
//		System.out.println(engine.eval(eq));
	}
	
	public static ArrayList<Token> convertEquation(String eq) {
		ArrayList<Token> tokens = new ArrayList<>();
		int charNumber = 0;
		while (charNumber < eq.length()) {
			String tokenValue = ""+eq.charAt(charNumber);
			if (!tokenValue.equals(" ")) {
				Token token = null;

				if (token == null) {
					// IS IT A NUMBER?
					try {
						if (tokenValue.equals(".")) {
							tokenValue = "0.";
						}
						Double.parseDouble(tokenValue);
						token = new Token(tokenValue);
						while(true) {
							Double.parseDouble(tokenValue+eq.charAt(charNumber+1));
							charNumber++;
							tokenValue = tokenValue+eq.charAt(charNumber);
							token = new Token(tokenValue);
						}
					} catch (Exception e) { }
				}

				if (token == null) {
					// IS OPERATOR?
					if ("x*/+-^()".contains(tokenValue)) {
						token = new Token(tokenValue);
					}
				}

				if (token == null && eq.length() > charNumber+2) {
					// IS ADVANCED OPERATOR?
					String threeLong = tokenValue+eq.charAt(charNumber+1)+eq.charAt(charNumber+2);
					if (threeLong.equals("cos") || threeLong.equals("tan") || 
						threeLong.equals("sin") || threeLong.equals("abs")) {
						charNumber+=2;
						token = new Token(threeLong);
					}
				}

				if (token != null) {
					tokens.add(token);
				}
			}
			charNumber++;
		}
		return tokens;
	}
			
	public static ArrayList<Token> redact(ArrayList<Token> in) throws Exception {
		ArrayList<Token> out = new ArrayList<>();

		if (in.get(0).isOperator()) {
			throw new Exception("Wrongful operator.");
		} else {
			out.add(in.get(0));
		}

		for (int i = 1; i < in.size(); i++) {
			if (in.get(i).isVariable() || in.get(i).isNumber()) {
				if (in.get(i-1).isVariable() || in.get(i-1).isNumber()) {
					out.add(new Token("*"));
				}
				out.add(in.get(i));
			}
			
			if (in.get(i).isOpenParentheses()) {
				if (in.get(i-1).isClosedParentheses() || in.get(i-1).isVariable() || in.get(i-1).isNumber()) {
					out.add(new Token("*"));
				}
				out.add(in.get(i));
			}
			
			if (in.get(i).isClosedParentheses()) {
				out.add(in.get(i));
			}
			
			if (in.get(i).isOperator()) {
				if (in.get(i-1).isOperator()) {
					throw new Exception("Duplicate operators.");
				} else {
					out.add(in.get(i));
				}
			}
		}
			
		return out;
	}
	
	public static ArrayList<Token> shuntingYard(ArrayList<Token> eq) {
		
//		while there are tokens to be read:
//			read a token.
//			if the token is a number, then push it to the output queue.
//			if the token is an operator, then:
//				while there is an operator at the top of the operator stack with
//					greater than or equal to precedence and the operator is left associative:
//						pop operators from the operator stack, onto the output queue.
//				push the read operator onto the operator stack.
//			if the token is a left bracket (i.e. "("), then:
//				push it onto the operator stack.
//			if the token is a right bracket (i.e. ")"), then:
//				while the operator at the top of the operator stack is not a left bracket:
//					pop operators from the operator stack onto the output queue.
//				pop the left bracket from the stack.
//				/* if the stack runs out without finding a left bracket, then there are
//				mismatched parentheses. */
//		if there are no more tokens to read:
//			while there are still operator tokens on the stack:
//				/* if the operator token on the top of the stack is a bracket, then
//				there are mismatched parentheses. */
//				pop the operator onto the output queue.
//		exit.

		ArrayList<Token> outputQueue = new ArrayList<>();
		ArrayList<Token> operatorStack = new ArrayList<>();
		
		for (int i = 0; i < eq.size(); i++) {
			Token token = eq.get(i);
			System.out.println("");
			if (token.isNumber() || token.isVariable()) {
				System.out.println("add token to output");
				outputQueue.add(token);
			} else {
				if (token.isOpenParentheses()) {
					System.out.println("push token to stack");
					operatorStack.add(token);
				} else if (token.isClosedParentheses()) {
					Token lastOperator = operatorStack.get(operatorStack.size()-1);
					while(!lastOperator.isOpenParentheses()) {
						System.out.println("pop stack to output");
						outputQueue.add(lastOperator);
						operatorStack.remove(operatorStack.size()-1);
						lastOperator = operatorStack.get(operatorStack.size()-1);
					}
						System.out.println("pop stack to output");
					operatorStack.remove(operatorStack.size()-1);
				} else if (token.isOperator()) {
					if (operatorStack.size()>0) {
						Token lastOperator = operatorStack.get(operatorStack.size()-1);
						while(lastOperator.isHigher(token) && operatorStack.size()>1) {
							System.out.println("pop stack to output");
							outputQueue.add(lastOperator);
							operatorStack.remove(operatorStack.size()-1);
							lastOperator = operatorStack.get(operatorStack.size()-1);
						}
					}
						System.out.println("push token to stack");
					operatorStack.add(token);
				}
			}
		}
		
		while (operatorStack.size()>0) {
			Token lastOperator = operatorStack.get(operatorStack.size()-1);
			operatorStack.remove(operatorStack.size()-1);
			
			if (!lastOperator.isParentheses()) {
				outputQueue.add(lastOperator);
				System.out.println("pop stack to output");
			}
		}
		
		return outputQueue;
	}
	
	public static void solve(ArrayList<Token> param) throws Exception {
		
		points.clear();
		for (int rawX = 0; rawX < SCREEN_SIZE*PRECISION; rawX++) {
			double x = (rawX-(SCREEN_SIZE*0.5f*PRECISION))/PRECISION;
//double x = 2;
			ArrayList<Token> eq = new ArrayList<Token>();
			for (int i = 0; i < param.size(); i++) {
				eq.add(new Token(param.get(i).getTokenValue()));
			}
			
			
			int j = 0;
			int n1 = 0;
			int n2 = 0;
			
			while (eq.size()>1) {
				Token t = eq.get(j);
				if (t.isNumber() || t.isVariable()) {
					try {
						n2 = j;
					} catch (Exception e) { }
//					System.out.println("Setting factor 2: "+eq.get(n2).getNumericalValue(x));
					j++;
				} else if (t.isOperator()) {
//					System.out.println("Found an operator!");
					n1 = n2-1;
//					System.out.println("Setting factor 1: "+eq.get(n1).getNumericalValue(x));
					double r;
					
	//				APPLY OPERATOR
					if (t.getTokenValue().equals("*")) {
						r = eq.get(n1).getNumericalValue(x)*eq.get(n2).getNumericalValue(x);
//						System.out.println(n1+"*"+n2+"="+r);
					} else if (t.getTokenValue().equals("/")) {
						r = eq.get(n1).getNumericalValue(x)/eq.get(n2).getNumericalValue(x);
//						System.out.println(n1+"/"+n2+"="+r);
					} else if (t.getTokenValue().equals("+")) {
						r = eq.get(n1).getNumericalValue(x)+eq.get(n2).getNumericalValue(x);
//						System.out.println(n1+"+"+n2+"="+r);
					} else if (t.getTokenValue().equals("-")) {
						r = eq.get(n1).getNumericalValue(x)-eq.get(n2).getNumericalValue(x);
//						System.out.println(n1+"-"+n2+"="+r);
					} else if (t.getTokenValue().equals("^")) {
						r = Math.pow(eq.get(n1).getNumericalValue(x),eq.get(n2).getNumericalValue(x));
//						System.out.println(n1+"^"+n2+"="+r);
					} else {
						throw new Exception ("Wrongful operator.");
					}
					eq.remove(j);
					eq.get(n2).setTokenValue(""+r);
					eq.remove(n1);
					n2--;
					j=0;
				}
			}
			points.add(new Point(x+(SCREEN_SIZE/2),(SCREEN_SIZE/2)-eq.get(n2).getNumericalValue(x)));
		}
	}
	
	public static BufferedImage getGraph() {
		int size = SCREEN_SIZE;
		BufferedImage graph = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		Graphics g = graph.getGraphics();
		
		g.setColor(color(39.2, 11.5, 89));
		g.fillRect(0, 0, size, size);
		
		g.setColor(Color.gray);
		g.drawLine(size/2, 0, size/2, size);
		g.drawLine(0, size/2, size, size/2);
		
		g.setColor(Color.black);
		g.drawLine(0, 0, size, 0);
		g.drawLine(0, 0, 0, size);
		g.drawLine(size-1, 0, size-1, size-1);
		g.drawLine(0, size-1, size-1, size-1);
		
		for (int i = 0; i < points.size(); i++) {
			g.drawImage(points.get(i).getImage(), (int)points.get(i).getX(), (int)points.get(i).getY(), null);
		}
		
		return graph;
	}
	
}
