package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.StringPool;

public class SpecialCharsTokenFilter extends TokenFilter {

	public SpecialCharsTokenFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		Token token;
		if(ts.hasNext()) {
			token = ts.next();
			String tokenText, newTokenText;
			tokenText = token.toString();
			newTokenText = tokenText.replaceAll(REGEX_FOR_SPECIAL_CHAR, StringPool.BLANK);
			if(newTokenText.contains(StringPool.HYPHEN)){
				newTokenText = newTokenText.replaceAll(REGEX_FOR_HYPHEN, MATCHED_PART_ONE + MATCHED_PART_TWO);
			}
			if(newTokenText.contains(StringPool.COLON)) {
				if(!newTokenText.matches(REGEX_FOR_COLON))
					newTokenText = newTokenText.replaceAll(StringPool.COLON, StringPool.BLANK);
			}
			if(newTokenText.isEmpty()) {
				ts.remove();
			} else if(!newTokenText.equals(tokenText)) {
				ts.getCurrent().setTermText(newTokenText);
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

	private static final String REGEX_FOR_SPECIAL_CHAR = "[!@#$%^&*()\\[\\]<>/|\\{\\};+=~`,\"_\\\\]+";
	private static final String REGEX_FOR_HYPHEN = "([a-zA-Z]+)-([a-zA-Z]+)";
	private static final String REGEX_FOR_COLON = "([0-9]{1,2}):([0-9]{1,2})(:[0-9]{1,2}){0,1}([A|P]M){0,1}";
	private static final String MATCHED_PART_ONE = "$1";
	private static final String MATCHED_PART_TWO = "$2";
}
