package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

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
		TreeNode root = constructTree(parsedQuery);
		inOrder(root);
		parsedQuery = "{ " + inorderString.toString() + " }";
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
		if(op.equals(Constants.OR)) {
			return 2;
		} else if(op.equals(Constants.AND)) {
			return 1;
		}
		return 0;
	}
	
	static boolean isOperator(String str) {
		if(str.equals(Constants.OR) || str.equals(Constants.AND) || str.equals(Constants.NOT)) {
			return true;
		}
		return false;
	}
	
	private void removePhrases() {
		int indexOfQuote = parsedQuery.indexOf(StringPool.DOUBLE_QUOTES);
		String phraseToReplace;
		int i = 0;
		while(indexOfQuote != -1) {
			i++;
			int indexOfSecondQuote = parsedQuery.indexOf(StringPool.DOUBLE_QUOTES, indexOfQuote + 1);
			phraseToReplace = parsedQuery.substring(indexOfQuote, indexOfSecondQuote + 1);
			termMap.put(i, phraseToReplace);
			parsedQuery = parsedQuery.replaceAll(phraseToReplace, StringPool.CAROT + i);
			indexOfQuote = parsedQuery.indexOf(StringPool.DOUBLE_QUOTES);
		}
	}
	
	private void expandPhrases() {
		if(!parsedQuery.contains(StringPool.CAROT)) {
			return;
		}
		
		int indexOfCarot = parsedQuery.indexOf(StringPool.CAROT);
		while(indexOfCarot != -1) {
			Integer i = Integer.parseInt(parsedQuery.substring(indexOfCarot + 1, indexOfCarot + 2));
			String phrase = termMap.get(i);
			if(phrase != null) {
				parsedQuery = parsedQuery.substring(0, indexOfCarot) + phrase + parsedQuery.substring(indexOfCarot + 2);
			}
			indexOfCarot = parsedQuery.indexOf(StringPool.CAROT);
		}
	}
	
	private void groupAndApplyTermIndex() {
		// TODO use pattern matcher
		if(!parsedQuery.contains("Term:") && !parsedQuery.contains("Category:") && !parsedQuery.contains("Place:") && !parsedQuery.contains("Author:")) {
			return;
		}
		boolean isGrouping = false;
		int groupingCount = 0;
		String[] tokens = parsedQuery.split(StringPool.SPACE);
		StringBuilder sb = new StringBuilder();
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
				sb.append(token + StringPool.SPACE);
			} else {
				groupingCount++;
				if(!isGrouping) {
					sb.append("Term:(");
				}
				sb.append(token + StringPool.SPACE);
				isGrouping = true;
			}
		}
		
		if(isGrouping && groupingCount > 1) {
			sb.delete(sb.length() - 1, sb.length());
			sb.append(StringPool.CLOSE_PARENTHESIS);
		} else if(isGrouping) {
			int indexToTrim = sb.lastIndexOf("Term:(");
			sb.delete(indexToTrim, indexToTrim + 6);
		}
		
		parsedQuery = sb.toString().trim();
	}
	
	private void expandIndexScope() {
		String[] tokens = parsedQuery.split(StringPool.SPACE);
		String index = null;
		StringBuilder sb = new StringBuilder();
		for(String token: tokens) {
			if(token.contains(":(")) {
				String[] parts = token.split(":\\(");
				index = parts[0];
				sb.append(StringPool.OPEN_PARENTHESIS);
				if(parts[1] != null && !isOperator(parts[1])) {
					sb.append(index + StringPool.COLON + parts[1] + StringPool.SPACE);
				}
			} else if(index != null && !isOperator(token)) {
				int indexOfCloseParenthesis = token.indexOf(StringPool.CLOSE_PARENTHESIS);
				sb.append(index + StringPool.COLON + token + StringPool.SPACE);
				if(indexOfCloseParenthesis != -1) {
					index = null;
				}
			} else {
				sb.append(token + StringPool.SPACE);
			}
		}
		
		parsedQuery = sb.toString().trim();
	}
	
	private void includeDefaultOp() {
		StringBuilder sb = new StringBuilder();
		String[] tokens = parsedQuery.split(StringPool.SPACE);
		boolean lastOp = true;
		boolean lastNOT = false;
		for(String token: tokens) {
			if(isOperator(token)) {
				lastOp = true;
				if(token.equals(Constants.NOT)) {
					token = Constants.AND;
					lastNOT = true;
				}
				sb.append(token + StringPool.SPACE);
			} else if(!lastOp) {
				sb.append(defOp + StringPool.SPACE + token + StringPool.SPACE);
				lastOp = false;
			} else {
				if(lastNOT) {
					sb.append(StringPool.TILDE);
				}
				sb.append(token + StringPool.SPACE);
				lastOp = false;
				lastNOT = false;
			}
		}
		parsedQuery = sb.toString().trim();
	}
	
	private TreeNode constructTree(String query) {
		TreeStack treeNodeStack = new TreeStack();
		TreeNode node;
		OperatorStack opStack = new OperatorStack();
		String[] tokens = query.split(StringPool.SPACE);
		int openBr = 0;
		StringBuilder subTreeString = new StringBuilder();
		for(String token: tokens) {
			if(isOperator(token) && openBr == 0) {
				if(opStack.isEmpty()) {
					opStack.push(token);
				} else {
					node = new TreeNode(opStack.pop());
					node.right = treeNodeStack.pop();
					node.left = treeNodeStack.pop();
					treeNodeStack.push(node);
					opStack.push(token);
				}
			}
			else {
				int indexOfOpenParenthesis = token.indexOf(StringPool.OPEN_PARENTHESIS);
				int indexOfCloseParenthesis = token.lastIndexOf(StringPool.CLOSE_PARENTHESIS);
				if(openBr == 0 && indexOfOpenParenthesis == -1 && indexOfCloseParenthesis == -1) {
					node = new TreeNode(token);
					node.right = node.left = null;
					treeNodeStack.push(node);
					continue;
				}
				
				if(indexOfOpenParenthesis == -1 && indexOfCloseParenthesis == -1) {
					subTreeString.append(token + StringPool.SPACE);
					continue;
				}
				
				if(indexOfOpenParenthesis != -1) {
					int innerOpenParenthesis = token.indexOf(StringPool.OPEN_PARENTHESIS, indexOfOpenParenthesis + 1);
					if(openBr == 0) {
						subTreeString.append(token.substring(1) + StringPool.SPACE);
					} else {
						subTreeString.append(token + StringPool.SPACE);
					}
					while(innerOpenParenthesis != -1) {
						openBr++;
						innerOpenParenthesis = token.indexOf(StringPool.OPEN_PARENTHESIS, innerOpenParenthesis + 1);
					}
					openBr++;
					continue;
				} else if(indexOfCloseParenthesis != -1) {
					int innerCloseParenthesis = token.indexOf(StringPool.CLOSE_PARENTHESIS);
					while(innerCloseParenthesis != -1) {
						openBr--;
						innerCloseParenthesis = token.indexOf(StringPool.CLOSE_PARENTHESIS, innerCloseParenthesis + 1);
					}
					if(openBr == 0) {
						subTreeString.append(token.substring(0, indexOfCloseParenthesis));
					} else {
						subTreeString.append(token + StringPool.SPACE);
					}
					if(openBr != 0) {
						continue;
					} else {
						node = constructTree(subTreeString.toString());
						subTreeString = new StringBuilder();
						node.data = StringPool.HASH + node.data;
						treeNodeStack.push(node);
					}
				}
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
	
	private void inOrder(TreeNode root) {
		if(root != null) {
			char ch = root.data.charAt(0);
			if(ch == '#') {
				root.data = root.data.substring(1);
				inorderString.append("[ ");
			}
			inOrder(root.left);
			StringBuilder tempRootData = new StringBuilder(root.data);
			boolean addNot = false;
			if(ch == '~') {
				tempRootData.delete(0, 1);
				addNot = true;
			}
			if(!root.data.contains(StringPool.COLON) && !isOperator(root.data)) {	
				tempRootData.insert(0, "Term:");
			}
			if(addNot) {
				tempRootData.insert(0, StringPool.LESS_THAN);
				tempRootData.append(StringPool.GREATER_THAN);
			}
			root.data = tempRootData.toString();
			inorderString.append(root.data + StringPool.SPACE);
			inOrder(root.right);
			if(ch == '#') {
				inorderString.append("] ");
			}
		}
	}
	
	private Map<Integer, String> termMap;
	private StringBuilder inorderString;
	private String query;
	private String parsedQuery;
	private String defOp;
	
}
