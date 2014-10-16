package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.StringPool;


public class CapitalizationTokenFilter extends TokenFilter {

	private static boolean isAllCaps = true;
	private static Token lastToken;
	
	public CapitalizationTokenFilter(TokenStream stream) {
		super(stream);
		lastToken = null;
		Token token;
		String tokenText;
		while(ts.hasNext()) {
			token = ts.next();
			tokenText = token.toString();
			if(!isAllCaps(tokenText)){
				isAllCaps = false;
				break;
			}
		}
		ts.reset();
	}

	@Override
	public boolean increment() throws TokenizerException {
		
		Token token;
		String tokenText, lastTokenText;
		if(ts.hasNext()) {
			token = ts.next();
			tokenText = token.toString();
			if(isAllCaps || (lastToken == null)) {
				ts.getCurrent().setTermText(tokenText.toLowerCase());
				lastToken = token;
				return true;
			}
			
			lastTokenText = lastToken.toString();
			int lastLen = lastTokenText.length();
			if(lastTokenText.charAt(lastLen - 1) == '.') {
				ts.getCurrent().setTermText(tokenText.toLowerCase());
			}
			else if(isFirstCaps(lastTokenText) && isFirstCaps(tokenText)) {
				ts.remove();
				ts.resetPointerAfterRemove();
				ts.getCurrent().merge(token);
			}
			else {
				// dont do anything, since the word is already in correct form
			}
			lastToken = token;
			return true;
		}
		else {
			isAllCaps = true;
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}
	
	
	private static boolean isFirstCaps(String str) {
		if(str == null) {
			return false;
		}
		if(str.isEmpty() || StringPool.BLANK.equals(str)) {
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
		if(str.isEmpty() || StringPool.BLANK.equals(str)) {
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
