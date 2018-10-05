package net.sleppik.pixiedust;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PixiedustInterpreter implements Runnable {

	public class PixiedustError extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2513552492569278619L;

		public PixiedustError(String message) {
			super(message);
		}

		public PixiedustError(Throwable cause) {
			super(cause);
		}

		public PixiedustError(String message, Throwable cause) {
			super(message, cause);
		}

		public PixiedustError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

	}

	public class PixiedustSyntaxError extends PixiedustError {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4360958740670532176L;

		public PixiedustSyntaxError(String message) {
			super(message);
		}

		public PixiedustSyntaxError(Throwable cause) {
			super(cause);
		}

		public PixiedustSyntaxError(String message, Throwable cause) {
			super(message, cause);
		}

		public PixiedustSyntaxError(String message, Throwable cause, boolean enableSuppression,
				boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}

	}

	private final String[] code;
	private final InputStream stdIn;
	private final PrintStream stdOut;
	private final PrintStream stdErr;
	private final Map<String, Integer> labels;
	
	private int pointer = 0;
	private ArrayList<Integer> memory = new ArrayList<>();
	
	private int registerDotDot = 0;
	private int registerDotPlus = 0;
	private int registerPlusStar = 0;
	private int registerPlusPlus = 0;
	private int registerPlusDot = 0;
	
	public static String lm(int i) {
		return "line " + (i + 1) + ": ";
	}

	public PixiedustInterpreter(final String[] code, final InputStream stdIn, final PrintStream stdOut, final PrintStream stdErr) throws PixiedustError {
		this.code = code;
		this.stdIn = stdIn;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
		
		HashMap<String, Integer> labels = new HashMap<>();
		for (int i = 0; i < code.length; i++) {
			if (code[i].matches("[^*+.]"))
				throw new PixiedustSyntaxError(lm(i) + "only the characters *+. and whitespace are allowed.");
			switch (code[i].charAt(0)) {
			
			
			case '*':
				if (code[i].charAt(1) == '.') {
					if (code[i].substring(2, 4) == ".*")
						throw new PixiedustError(lm(i) + ".* is not a register");
					int[] copiedFrom = parseExpressions(code[i].substring(4));
					if (copiedFrom.length != 1)
						throw new PixiedustSyntaxError(lm(i) + (copiedFrom.length > 1 ? "extraneous" : "missing") +
														" parameter for assignment statement");
				} else if (code[i].substring(1, 3) == "+*")
					throw new PixiedustError(lm(i) + "+* is currently unsupported");
				else {
					if (code[i].substring(3, 5) == ".*")
						throw new PixiedustError(lm(i) + ".* is not a register");
					int[] copiedFrom = parseExpressions(code[i].substring(4));
					if (copiedFrom.length != 2)
						throw new PixiedustSyntaxError(lm(i) + "math operation " +
														(copiedFrom.length > 2 ? "has too many" : "does not have enough") +
														" arguments.");
				}
				break;
			
				
			case '+':
				switch (code[i].charAt(1)) {
				case '*':
					if (code[i].length() < 3)
						throw new PixiedustSyntaxError(lm(i) + "missing jump condition");
					break;
				case '+':
					int[] written = parseExpressions(code[i].substring(2));
					if (written.length != 1)
						throw new PixiedustSyntaxError(lm(i) + (written.length > 1 ? "extraneous" : "missing") +
														" parameter for print statement");
					break;
				case '.':
					labels.put(code[i].substring(2), i);
				}
				break;
			
				
			case '.':
				if (code[i].length() == 1)
					throw new PixiedustSyntaxError(lm(i) + "missing comparison type");
				int[] comparing = parseExpressions(code[i].substring(2));
				if (comparing.length != 2)
					throw new PixiedustSyntaxError(lm(i) + "comparison instruction " +
													(comparing.length > 2 ? "has too many" : "does not have enough") +
													" arguments.");
				break;
			}
		}
		
		this.labels = Collections.unmodifiableMap(labels);
		for (int i = 0; i < code.length; i++) {
			if (code[i].startsWith("+*") && !labels.containsKey(code[i].substring(3)))
				throw new PixiedustError(lm(i) + "can't find label: " + code[i].substring(3));
		}
	}

	public static void main(String[] args) throws FileNotFoundException, PixiedustError {
		Scanner input = new Scanner(new File(args[0]));
		ArrayList<String> code = new ArrayList<>();
		while (input.hasNext()) {
			String lineIn = input.nextLine().replaceAll("\\s", "");
			if (lineIn != "") code.add(lineIn);
		}
		input.close();
		new PixiedustInterpreter(code.toArray(new String[code.size()]), System.in, System.out, System.err).run();
	}

	@Override
	public void run() {
		for (int i = 0; i < code.length; i++) {
			switch (code[i].charAt(0)) {
			case '*':
				if (code[i].charAt(1) == '.')
					setRegister(code[i].substring(2, 4), parseExpressions(code[i].substring(4))[0]);
				else {
					int[] operands = parseExpressions(code[i].substring(5));
					int result = 0;
					switch (code[i].substring(1, 3)) {
					case "++":
						result = operands[0] + operands[1];
						break;
					case "+.":
						result = operands[0] + operands[1];
						break;
					case "**":
						result = operands[0] + operands[1];
						break;
					case "*.":
						result = operands[0] + operands[1];
						break;
					case "*+":
						result = operands[0] + operands[1];
						break;
					}
					setRegister(code[i].substring(3, 5), result);
				}
				break;
				
				
			case '+':
				switch (code[i].charAt(1)) {
				case '*':
					boolean jumpCondition = registerDotDot == 0;
					if (code[i].charAt(2) == '*' && jumpCondition) break;
					if (code[i].charAt(2) == '.' && !jumpCondition) break;
					i = labels.get(code[i].substring(2));
					break;
				case '+':
					stdOut.print((char) parseExpressions(code[i].substring(2))[0]);
				}
				break;
				
				
			case '.':
				int[] comparing = parseExpressions(code[i].substring(2));
				switch (code[i].charAt(1)) {
				case '*':
					registerDotDot = comparing[0] == comparing[1] ? 1 : 0;
					break;
				case '+':
					registerDotDot = comparing[0] < comparing[1] ? 1 : 0;
					break;
				case '.':
					registerDotDot = comparing[0] > comparing[1] ? 1 : 0;
				}
				break;
			}
		}
	}

	private void setRegister(String registerID, int newValue) {
		switch (registerID) {
		case "**":
			pointer = newValue;
			break;
		case "*+":
			stdErr.print((char) newValue);
			break;
		case "*.":
			memory.ensureCapacity(pointer);
			memory.set(pointer, newValue);
			break;
		case "+*":
			registerPlusDot = newValue;
			break;
		case "++":
			registerPlusPlus = newValue;
			break;
		case "+.":
			registerPlusDot = newValue;
			break;
		case ".+":
			registerDotPlus = newValue;
			break;
		case "..":
			registerDotDot = newValue;
		}
	}

	public int[] parseExpressions(String exps) {
		ArrayList<Integer> parsed = new ArrayList<>();
		for (int i = 0; i < exps.length(); i += 2) {
			String nextExp = exps.substring(i);
			if (!nextExp.startsWith(".*")) {
				switch (nextExp.substring(0, 2)) {
				case "**":
					parsed.add(pointer);
					break;
				case "*+":
					try {
						parsed.add(stdIn.read());
					} catch (IOException e) {
						parsed.add(0);
					}
					break;
				case "*.":
					parsed.add(memory.get(pointer));
					break;
				case "+*":
					parsed.add(registerPlusStar);
					break;
				case "++":
					parsed.add(registerPlusPlus);
					break;
				case "+.":
					parsed.add(registerPlusDot);
					break;
				case ".+":
					parsed.add(registerDotPlus);
					break;
				case "..":
					parsed.add(registerDotDot);
				}
			} else {
				
				@SuppressWarnings("resource")
				Scanner numberer = new Scanner(nextExp + '*');
				numberer.useDelimiter("\\*");
				numberer.next();
				String binaryRep = numberer.next();
				int finalValue = 0;
				for (int j = 0; j < binaryRep.length(); j++) if (binaryRep.charAt(j) == '+')
					finalValue |= 1 << (binaryRep.length() - j - 1);
				
				i += binaryRep.length() + 1;
				parsed.add(finalValue);
			}
		}
		return parsed.stream().mapToInt(Integer::intValue).toArray();
	}

}
