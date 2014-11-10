package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.dictionary.TermDictionary;
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
		expandWCTerms();
		groupAndApplyTermIndex();
		expandIndexScope();
		includeDefaultOp();
		TreeNode root = constructTree(parsedQuery);
		inOrder(root);
		parsedQuery = inorderString.toString();
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
	
	private void expandWCTerms() {
		if(!parsedQuery.contains(StringPool.STAR) && !parsedQuery.contains(StringPool.QUESTION_MARK)) {
			return;
		}
		String[] tokens = parsedQuery.split(StringPool.SPACE);
		StringBuilder expansionQuery, sb;
		List<String> expansionTerms;
		
		expansionQuery = new StringBuilder();
		for(String token: tokens) {
			sb = new StringBuilder();
			if(token.contains(StringPool.COLON)) {
				String[] parts = token.split(StringPool.COLON);
				sb.append(parts[0]);
				sb.append(StringPool.COLON);
				if(parts[1].contains(StringPool.STAR) || parts[1].contains(StringPool.QUESTION_MARK)) {
					expansionTerms = getQueryTerms(parts[1]);
					wcTermMap.put(parts[1], expansionTerms);
					if(expansionTerms != null) {
						if(expansionTerms.size() > 1)
							sb.append(StringPool.OPEN_PARENTHESIS);
						for(String expansionTerm: expansionTerms) {
							sb.append(expansionTerm);
							sb.append(StringPool.SPACE);
						}
						sb.deleteCharAt(sb.length() - 1);
						if(expansionTerms.size() > 1)
							sb.append(StringPool.CLOSE_PARENTHESIS);
					} else {
						sb.append("null");
					}
				} else {
					sb.append(parts[1]);
				}
			}
			else if(token.contains(StringPool.OPEN_PARENTHESIS)) {
				sb.append(StringPool.OPEN_PARENTHESIS);
				token.replaceAll(StringPool.OPEN_PARENTHESIS, StringPool.BLANK);
				if(token.contains(StringPool.STAR) || token.contains(StringPool.QUESTION_MARK)) {
					expansionTerms = getQueryTerms(token);
					wcTermMap.put(token, expansionTerms);
					if(expansionTerms != null) {
						if(expansionTerms.size() > 1)
							sb.append(StringPool.OPEN_PARENTHESIS);
						for(String expansionTerm: expansionTerms) {
							sb.append(expansionTerm);
							sb.append(StringPool.SPACE);
						}
						sb.deleteCharAt(sb.length() - 1);
						if(expansionTerms.size() > 1)
							sb.append(StringPool.CLOSE_PARENTHESIS);
					} else {
						sb.append("null");
					}
				} else {
					sb.append(token);
				}
			}
			else if(token.contains(StringPool.CLOSE_PARENTHESIS)) {
				token.replaceAll(StringPool.CLOSE_PARENTHESIS, StringPool.BLANK);
				if(token.contains(StringPool.STAR) || token.contains(StringPool.QUESTION_MARK)) {
					expansionTerms = getQueryTerms(token);
					wcTermMap.put(token, expansionTerms);
					if(expansionTerms != null) {
						if(expansionTerms.size() > 1)
							sb.append(StringPool.OPEN_PARENTHESIS);
						for(String expansionTerm: expansionTerms) {
							sb.append(expansionTerm);
							sb.append(StringPool.SPACE);
						}
						sb.deleteCharAt(sb.length() - 1);
						if(expansionTerms.size() > 1)
							sb.append(StringPool.CLOSE_PARENTHESIS);
					} else {
						sb.append("null");
					}
				} else {
					sb.append(token);
				}
				sb.append(StringPool.CLOSE_PARENTHESIS);
			}
			else {
				if(token.contains(StringPool.STAR) || token.contains(StringPool.QUESTION_MARK)) {
					expansionTerms = getQueryTerms(token);
					wcTermMap.put(token, expansionTerms);
					if(expansionTerms != null) {
						if(expansionTerms.size() > 1)
							sb.append(StringPool.OPEN_PARENTHESIS);
						for(String expansionTerm: expansionTerms) {
							sb.append(expansionTerm);
							sb.append(StringPool.SPACE);
						}
						sb.deleteCharAt(sb.length() - 1);
						if(expansionTerms.size() > 1)
							sb.append(StringPool.CLOSE_PARENTHESIS);
					} else {
						sb.append("null");
					}
				} else {
					sb.append(token);
				}
			}
			expansionQuery.append(sb.toString() + StringPool.SPACE);
		}
		parsedQuery = expansionQuery.toString().trim();
	}
	
	private List<String> getQueryTerms(String term) {
		
		String newTerm = term.replaceAll("\\*", ".*");
		newTerm = newTerm.replaceAll("\\?", ".?");
		
		List<String> expansionTerms = new ArrayList<String>();
		
		//String[] termDictionary = new String[]{"mln", "mla", "mleaesa", "msqla"};
		Set<String> termDictionary = TermDictionary.getDictionary().keySet();
		
		for(String dictTerm: termDictionary) {
			if(Pattern.matches(newTerm, dictTerm)) {
				expansionTerms.add(dictTerm);
			}
		}
		
		if(expansionTerms.size() > 0) {
			return expansionTerms;
		}
		return null;
	}
	
	public Map<String, List<String>> getWCExpandedTerms() {
		return wcTermMap;
	}
	
	private Map<Integer, String> termMap;
	private StringBuilder inorderString;
	private String query;
	private String parsedQuery;
	private String defOp;
	private Map<String, List<String>> wcTermMap = new HashMap<String, List<String>>();
	
}
