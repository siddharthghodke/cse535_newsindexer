package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.util.StringPool;

public class NumericTokenFilter extends TokenFilter {
	
	public NumericTokenFilter(TokenStream stream) {
		super(stream);
	}
		
	Token token;

	@Override
	public boolean increment() throws TokenizerException {
		String tokenText;
		if(ts.hasNext()) {
			token = ts.next();
			tokenText = removeNumber(token.toString());
			if(StringPool.BLANK.equals(tokenText) || tokenText.isEmpty()) {
				ts.remove();
			} else {
				ts.getCurrent().setTermText(tokenText);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}
	
	public String removeNumber(String token)
	{
		if(token.matches(REGEX_FOR_TIME) || token.matches(REGEX_FOR_DATE_FORMAT)) {
			return token;
		}
		
		Pattern pattern = Pattern.compile("[a-zA-Z]+");
		Matcher matcher = pattern.matcher(token);
		if(matcher.find()) {
			return token;
		}
		
		token=token.replaceAll(REGEX_FOR_ANY_NUMBER, StringPool.BLANK);
		
		return token;
	}

	private static final String REGEX_FOR_TIME = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
	private static final String REGEX_FOR_DATE_FORMAT = "(-)?[0-9]{8}";
	private static final String REGEX_FOR_ANY_NUMBER = "[0-9]+[([\\.,]?[0-9]+)]*";
}
