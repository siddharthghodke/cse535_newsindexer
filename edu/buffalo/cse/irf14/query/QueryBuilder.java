package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {
	
	public QueryBuilder(String query, String defOp) {
		this.query = query;
		this.defOp = defOp;
	}
	
	public String buildQuery() {
		
		termMap = new HashMap<Integer, String>();
		inorderString = new StringBuilder();
		parsedQuery = new String(query);
		
		removePhrases();
		groupAndApplyTermIndex();
		expandIndexScope();
		includeDefaultOp();
		TreeNode root = constructTree();
		nonRecursiveInOrder(root);
		expandPhrases();
		
		return parsedQuery;
	}
	

	static class TreeNode {
		TreeNode left, right;
		String data;
		
		public TreeNode(String data) {
			this.data = data;
			this.left = null;
			this.right = null;
		}
	}
	
	static class OperatorStack {
		List<String> opStack;
		int top;

		OperatorStack() {
			top = -1;
			opStack = new ArrayList<String>();
		}

		void push(String data) {
			opStack.add(data);
			top++;
		}

		String pop() {
			return opStack.remove(top--);
		}

		String peek() {
			return opStack.get(top);
		}
		
		boolean isEmpty() {
			if (top == -1) {
				return true;
			}
			return false;
		}
	}
	
	static class TreeStack {
		List<TreeNode> treeElements;
		int top;
		
		TreeStack() {
			top = -1;
			treeElements = new ArrayList<TreeNode>();
		}
		
		void push(TreeNode node) {
			treeElements.add(node);
			top++;
		}
		
		TreeNode pop() {
			return treeElements.remove(top--);
		}
		
		TreeNode peek() {
			return treeElements.get(top);
		}
		
		boolean isEmpty() {
			if(top == -1) {
				return true;
			}
			return false;
		}
	}

	static int precedence(String op) {
		if(op.equals("OR")) {
			return 2;
		} else if(op.equals("AND")) {
			return 1;
		}
		return 0;
	}
	
	static boolean isOperator(String str) {
		if(str.equals("OR") || str.equals("AND") || str.equals("NOT")) {
			return true;
		}
		return false;
	}
	
	public TreeNode constructTree() {
		
		String[] tokens = parsedQuery.split(" ");
		OperatorStack opStack = new OperatorStack();
		TreeStack treeNodeStack = new TreeStack();
		TreeNode node;
		for(String token: tokens) {
			String stackOp;
			if(isOperator(token)) {
				if(opStack.isEmpty()) {
					opStack.push(token);
				} else {
					stackOp = opStack.peek();
					if(precedence(token) > precedence(stackOp)) {
						opStack.push(token);
					} else {
						node = new TreeNode(opStack.pop());
						node.right = treeNodeStack.pop();
						node.left = treeNodeStack.pop();
						treeNodeStack.push(node);
						opStack.push(token);
					}
				}
			}
			else {
				node = new TreeNode(token);
				node.right = node.left = null;
				treeNodeStack.push(node);
			}
		}
		
		while(!opStack.isEmpty()) {
			node = new TreeNode(opStack.pop());
			node.right = treeNodeStack.pop();
			node.left = treeNodeStack.pop();
			treeNodeStack.push(node);
		}
		
		return treeNodeStack.pop();
	}
	
	private void removePhrases() {
		int indexOfQuote = parsedQuery.indexOf("\"");
		String phraseToReplace;
		int i = 0;
		while(indexOfQuote != -1) {
			i++;
			int indexOfSecondQuote = parsedQuery.indexOf("\"", indexOfQuote + 1);
			phraseToReplace = parsedQuery.substring(indexOfQuote, indexOfSecondQuote + 1);
			termMap.put(i, phraseToReplace);
			parsedQuery = parsedQuery.replaceAll(phraseToReplace, "^" + i);
			indexOfQuote = parsedQuery.indexOf("\"");
		}
		
		//return query;
		//parsedQuery = query;
	}
	private void expandPhrases() {
		
		if(!parsedQuery.contains("^")) {
			return;
		}
		
		int indexOfCarot = parsedQuery.indexOf("^");
		while(indexOfCarot != -1) {
			Integer i = Integer.parseInt(parsedQuery.substring(indexOfCarot + 1, indexOfCarot + 2));
			String phrase = termMap.get(i);
			if(phrase != null) {
				parsedQuery = parsedQuery.substring(0, indexOfCarot) + phrase + parsedQuery.substring(indexOfCarot + 2);
			}
			indexOfCarot = parsedQuery.indexOf("^");
		}
		
		//return query;
		//parsedQuery = query;
	}
	
	private void groupAndApplyTermIndex() {
		// TODO use pattern matcher
		if(!parsedQuery.contains("Term:") && !parsedQuery.contains("Category:") && !parsedQuery.contains("Place:") && !parsedQuery.contains("Author:")) {
			return;
		}
		boolean isGrouping = false;
		int groupingCount = 0;
		String[] tokens = parsedQuery.split(" ");
		StringBuilder sb = new StringBuilder();
		//int indexOfParenthesis = -1;
		for(String token: tokens) {
			if(token.contains("Term:") || token.contains("Category:") || token.contains("Place:") || token.contains("Author:") || isOperator(token)) {
				if(isGrouping && groupingCount > 1) {
					sb.delete(sb.length() - 1, sb.length());
					sb.append(") ");
				} else if(isGrouping) {
					int indexToTrim = sb.lastIndexOf("Term:(");
					sb.delete(indexToTrim, indexToTrim + 6);
				}
				isGrouping = false;
				groupingCount = 0;
				sb.append(token + " ");
			} else {
				groupingCount++;
				if(!isGrouping) {
					sb.append("Term:(");
				}
				sb.append(token + " ");
				isGrouping = true;
			}
		}
		
		if(isGrouping && groupingCount > 1) {
			sb.delete(sb.length() - 1, sb.length());
			sb.append(")");
		} else if(isGrouping) {
			int indexToTrim = sb.lastIndexOf("Term:(");
			sb.delete(indexToTrim, indexToTrim + 6);
		}
		
		//return sb.toString().trim();
		parsedQuery = sb.toString().trim();
	}
	
	private void expandIndexScope() {
		String[] tokens = parsedQuery.split(" ");
		String index = null;
		StringBuilder sb = new StringBuilder();
		for(String token: tokens) {
			if(token.contains(":(")) {
				String[] parts = token.split(":\\(");
				index = parts[0];
				sb.append("(");
				if(parts[1] != null && !isOperator(parts[1])) {
					sb.append(index + ":" + parts[1] + " ");
				}
			} else if(index != null && !isOperator(token)) {
				int indexOfCloseParenthesis = token.indexOf(")");
				sb.append(index + ":" + token + " ");
				if(indexOfCloseParenthesis != -1) {
					index = null;
				}
			} else {
				sb.append(token + " ");
			}
		}
		
		//return sb.toString().trim();
		parsedQuery = sb.toString().trim();
	}
	
	private void includeDefaultOp() {
		StringBuilder sb = new StringBuilder();
		String[] tokens = parsedQuery.split(" ");
		boolean lastOp = true;
		boolean lastNOT = false;
		for(String token: tokens) {
			if(isOperator(token)) {
				lastOp = true;
				if(token.equals("NOT")) {
					token = "AND";
					lastNOT = true;
				}
				sb.append(token + " ");
			} else if(!lastOp) {
				sb.append(defOp + " " + token + " ");
				lastOp = false;
			} else {
				if(lastNOT) {
					sb.append("~");
				}
				sb.append(token + " ");
				lastOp = false;
				lastNOT = false;
			}
		}
		//String extendedQuery = sb.toString().trim();
		//return extendedQuery;
		parsedQuery = sb.toString().trim();
	}
	
	private void nonRecursiveInOrder(TreeNode root) {
		inorderString = new StringBuilder();
		TreeStack stack = new TreeStack();
		while(root != null) {
			stack.push(root);
			root = root.left;
		}
		
		inorderString.append("{ ");
		TreeNode node;
		//boolean isExplicitIndex = false;
		while(!stack.isEmpty()) {
			node = stack.pop();
			int indexOfParenthesis = node.data.indexOf("(");
			if(indexOfParenthesis != -1) {
				inorderString.append("[ ");
				node.data = node.data.substring(indexOfParenthesis + 1);
			}
			int indexOfCloseParenthesis = node.data.indexOf(")");
			if(indexOfCloseParenthesis != -1) {
				node.data = node.data.substring(0, indexOfCloseParenthesis);
			}
			
			int indexOfTilde = node.data.indexOf("~");
			if(indexOfTilde != -1) {
				inorderString.append("<");
				node.data = node.data.substring(indexOfTilde + 1);
			}
			if(!node.data.contains(":") && !isOperator(node.data)) {	
				inorderString.append("Term:");
			}
			inorderString.append(node.data + " ");
			
			if(indexOfTilde != -1) {
				inorderString.deleteCharAt(inorderString.length() - 1);
				inorderString.append(">");
			}
			if (indexOfCloseParenthesis != -1) {
				if(inorderString.charAt(inorderString.length() - 1) == ' ') {
					inorderString.append("] ");
				} else {
					inorderString.append(" ] ");
				}
			}
			
			node = node.right;
			while(node != null) {
				stack.push(node);
				node = node.left;
			}
		}
		if(inorderString.charAt(inorderString.length() - 1) == ' ') {
			inorderString.append("}");
		} else {
			inorderString.append(" }");
		}
		
		parsedQuery = inorderString.toString().trim();
	}
	
	
	private Map<Integer, String> termMap;
	private StringBuilder inorderString;
	private String query;
	private String parsedQuery;
	private String defOp;
	
}
