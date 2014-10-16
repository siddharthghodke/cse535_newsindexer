package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.Stemmer;

public class StemmerTokenFilter extends TokenFilter {

	private static Stemmer s ;
	public StemmerTokenFilter(TokenStream stream) {
		super(stream);
		s = new Stemmer();
	}

	@Override
	public boolean increment() throws TokenizerException {
		
		Token token;
		if(ts.hasNext()) {
			char ch;
			String tokenText, newTokenText;
			token = ts.next();
			tokenText = token.toString();
			ch = tokenText.charAt(0);
			if(!(ch >= 'A' && ch <= 'Z') && !(ch >= 'a' && ch <= 'z')) {
				newTokenText = tokenText;
			} else {
				s.add(tokenText.toCharArray(), tokenText.length());
				s.stem();
				newTokenText = s.toString();
			}

			if(!tokenText.equals(newTokenText)) {
				ts.getCurrent().setTermText(newTokenText);
			}
			return true;
		}
		else  {
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}

}
