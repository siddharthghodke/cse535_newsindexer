package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.List;

public class CapitalizationTokenFilter extends TokenFilter {

	public CapitalizationTokenFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		
		
		return false;
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	private static TokenStream applyCapitalizationFilter(TokenStream tokenStream) {
		
		if(tokenStream == null) {
			return null;
		}
		if(tokenStream.size() == 0) {
			return tokenStream;
		}

		TokenStream processedStream = new TokenStream();
		boolean allCaps = true;
		Token token;
		tokenStream.reset();
		while(tokenStream.hasNext()) {
			token = tokenStream.next();
			if(!isAllCaps(token.toString())){
				allCaps = false;
				break;
			}
		}
		
		tokenStream.reset();
		if(allCaps) {
			while(tokenStream.hasNext()) {
				token = tokenStream.next();
				token.setTermText(token.toString().toLowerCase());
				processedStream.add(token);
			}
			return processedStream;
		}
		
		Token lastToken, curToken, tokenToAdd;
		lastToken = tokenStream.next();
		lastToken.setTermText(lastToken.toString().toLowerCase());
		processedStream.add(lastToken);
		processedStream.next();
		String curTokenString, lastTokenString;
		int lastLen;
		while(tokenStream.hasNext()) {
			curToken = tokenStream.next();
			curTokenString = curToken.toString();
			lastTokenString = lastToken.toString();
			lastLen = lastTokenString.length();
			if(lastTokenString.charAt(lastLen - 1) == '.') {
				tokenToAdd = new Token(curToken.toString().toLowerCase());
				processedStream.add(tokenToAdd);
				processedStream.next();
			}
			else if(isFirstCaps(lastTokenString) && isFirstCaps(curTokenString)) {
				processedStream.getCurrent().merge(curToken);
			}
			else {
				processedStream.add(curToken);
				processedStream.next();
			}
			lastToken = curToken;
		}
		
		
		return processedStream;
	}
	
	/*private static boolean isCamelCased(String str) {
		if(str == null) {
			return false;
		}
		
		if(str.isEmpty() || "".equals(str)) {
			return false;
		}
		
		int len = str.length();
		if(len == 1) {
			return false;
		}
		
		if(str.charAt(0) >= 'A' && str.charAt(0) <= 'Z') {
			// if first char is upper case, check if there is any char in lower case
			for(int i=1; i<len; i++) {
				if(str.charAt(i) >= 'a' && str.charAt(i) <= 'z') {
					return true;
				}
			}
			return false;
		} else if(str.charAt(0) >= 'a' && str.charAt(0) <= 'z') {
			// if first char is in lower case, check if there is any successive char in upper case
			for(int i=1; i<len; i++) {
				if(str.charAt(i) >= 'A' && str.charAt(i) <= 'Z') {
					return true;
				}
			}
			return false;
		}
		return false;
	}*/
	
	private static boolean isFirstCaps(String str) {
		if(str == null) {
			return false;
		}
		if(str.isEmpty() || "".equals(str)) {
			return false;
		}
		
		int len = str.length();
		if(len == 1) {
			return false;
		}
		
		char c = str.charAt(0);
		if(c >= 'A' && c <= 'Z') {
			for(int i=1; i<len; i++) {
				c = str.charAt(i);
				if(c < 'a' || c > 'z') {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	private static boolean isAllCaps(String str) {
		if(str == null) {
			return false;
		}
		if(str.isEmpty() || "".equals(str)) {
			return false;
		}

		// any character that is not in lower case is allowed
		for(int i=0; i<str.length(); i++) {
			char c = str.charAt(i);
			if(c >= 'a' && c <= 'z') {
				return false;
			}
		}
		
		return true;
	}

}
