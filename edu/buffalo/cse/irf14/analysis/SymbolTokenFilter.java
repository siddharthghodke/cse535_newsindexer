package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.util.StringPool;

public class SymbolTokenFilter extends TokenFilter {

	private static Map<String, String> cm;
	private static final String REGEX_FOR_SINGLE_QUOTE = "('s$)|(')";
	private static final String REGEX_TO_MATCH_DIGITS = "[0-9]+";
	
	static {
		cm = new HashMap<String, String>();
		cm.put("can't","cannot");
		cm.put("couldn't","could not");
		cm.put("didn't","did not");
		cm.put("doesn't","does not");
		cm.put("don't","do not");
		cm.put("'em", "them");
		cm.put("hadn't","had not");
		cm.put("hasn't","has not");
		cm.put("haven't","have not");
		cm.put("he'd","he would");
		cm.put("he'll","he will");
		cm.put("he's","he is");
		cm.put("i'd","i would");
		cm.put("i'll","i will");
		cm.put("i'm","i am");
		cm.put("i've","i have");
		cm.put("isn't","is not");
		cm.put("it's","it is");
		cm.put("let's","let us");
		cm.put("mightn't","might not");
		cm.put("mustn't","must not");
		cm.put("must've","must have");
		cm.put("shan't","shall not");
		cm.put("she'd","she would");
		cm.put("she'll","she will");
		cm.put("she's","she is");
		cm.put("should've","should have");
		cm.put("shouldn't've","should not have");
		cm.put("shouldn't","should not");
		cm.put("that's","that is");
		cm.put("there's","there is");
		cm.put("they'd","they would");
		cm.put("they'll","they will");
		cm.put("they're","they are");
		cm.put("they've","they have");
		cm.put("we'd","we would");
		cm.put("we're","we are");
		cm.put("we've","we have");
		cm.put("weren't","were not");
		cm.put("what'll","what will");
		cm.put("what're","what are");
		cm.put("what's","what is");
		cm.put("what've","what have");
		cm.put("where's","where is");
		cm.put("who'd","who would");
		cm.put("who'll","who will");
		cm.put("who're","who are");
		cm.put("who's","who is");
		cm.put("who've","who have");
		cm.put("won't","will not");
		cm.put("wouldn't","would not");
		cm.put("you'd","you would");
		cm.put("you'll","you will");
		cm.put("you're","you are");
		cm.put("you've","you have");
	}
	
	public SymbolTokenFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {

		Token token;
		String termText, newTermText;
		if(ts.hasNext()) {
			token = ts.next();
			termText = token.getTermText();
			newTermText = removeAtEnd(termText);
			newTermText = removeCommonContractions(newTermText);
			newTermText = removeQuotes(newTermText);
			newTermText = removeHyphens(newTermText);
			if(newTermText.equals(StringPool.BLANK)) {
				ts.remove();
			} else {
				ts.getCurrent().setTermText(newTermText);
			}
			return true;
		}
		else {
			return false;
		}
	}

	private static String removeAtEnd(String tokenText) {
		if(tokenText == null || tokenText.equals(StringPool.BLANK)) {
			return StringPool.BLANK;
		}
		
		String newTokenText = tokenText.replaceAll("([\\.?!;:]+$)", StringPool.BLANK);
		
		/*String newTokenText;
		int indexOfPeriod, indexOfExclamationMark, indexOfQuestionMark, indexOfSemicolon, indexOfColon;
		int tokenLength = tokenText.length();
		indexOfPeriod = tokenText.lastIndexOf(StringPool.PERIOD);
		if(indexOfPeriod == tokenLength - 1) {
			newTokenText = tokenText.substring(0, indexOfPeriod);
		} else if ((indexOfQuestionMark = tokenText.lastIndexOf(StringPool.QUESTION_MARK)) == tokenLength - 1) {
			newTokenText = tokenText.substring(0, indexOfQuestionMark);
		} else if ((indexOfExclamationMark = tokenText.lastIndexOf(StringPool.EXCLAMATION_MARK)) == tokenLength - 1) {
			newTokenText = tokenText.substring(0, indexOfExclamationMark);
		} else if ((indexOfSemicolon = tokenText.lastIndexOf(StringPool.SEMICOLON)) == tokenLength - 1) {
			newTokenText = tokenText.substring(0, indexOfSemicolon);
		} else if ((indexOfColon = tokenText.lastIndexOf(StringPool.COLON)) == tokenLength - 1) {
			newTokenText = tokenText.substring(0, indexOfColon);
		} else {
			newTokenText = tokenText;
		}*/

		return newTokenText;
	}
	
	
	private static String removeCommonContractions(String tokenText) {

		if(tokenText == null || tokenText.equals(StringPool.BLANK)) {
			return StringPool.BLANK;
		}
		String newTokenText;
		boolean isCapital = false;
		Character c = tokenText.charAt(0);
		if(c >= 'A' && c <= 'Z') {
			isCapital = true;
		}
		// convert to lower case, as the map contains key-value pairs in lower case
		newTokenText = cm.get(tokenText.toLowerCase());
		if(newTokenText == null) {
			newTokenText = tokenText;
		} else {
			if(isCapital) {
				newTokenText = c.toString() + newTokenText.substring(1);
			}
		}
		return newTokenText;
	}
	
	private static String removeQuotes(String tokenText) {
		
		if(tokenText == null || tokenText.equals(StringPool.BLANK)) {
			return StringPool.BLANK;
		}
		String newTokenText = tokenText.replaceAll(REGEX_FOR_SINGLE_QUOTE, StringPool.BLANK);
		return newTokenText;
	}
	
	/*private static String removeSingleQuotes(String tokenText) {

		String startQuoteToken = null;
		int indexOfQuote, secondIndexOfQuote, startQuoteTokenIndex = -1;
		String[] processedStream = new String[strTokens.length];
		int tokenLength;
		
		for(int i=0; i<strTokens.length; i++) {
			token = strTokens[i];
			tokenLength = token.length();
			indexOfQuote = token.indexOf("'");
			secondIndexOfQuote = token.indexOf("'", indexOfQuote + 1);
			if(indexOfQuote == 0 && secondIndexOfQuote == tokenLength - 1) {
				processedStream[i] = token.substring(1, secondIndexOfQuote);
			} else if(indexOfQuote == 0) {
				startQuoteTokenIndex = i;
				startQuoteToken = token;
				processedStream[i] = token;
			} else if ((indexOfQuote == tokenLength - 1) && (startQuoteToken != null)) {
				processedStream[startQuoteTokenIndex] = startQuoteToken.substring(1);
				processedStream[i] = token.substring(0, indexOfQuote);
				startQuoteToken = null;
			} else {
				processedStream[i] = token;
			}
		}
		return processedStream;
	}*/
	
	/*private static String[] removePossesiveApostrophes(String[] strTokens) {
		String token, newToken;
		String[] processedStream = new String[strTokens.length];
		int indexOfApostrophe;
		int tokenLength;
		
		for(int i=0; i<strTokens.length; i++) {
			token = strTokens[i];
			tokenLength = token.length();
			indexOfApostrophe = token.indexOf("'");
			if(indexOfApostrophe == tokenLength - 1) {
				// if last character of the token is apostrophe
				newToken = token.substring(0, indexOfApostrophe);
			} else if ((indexOfApostrophe == tokenLength - 2) && (token.charAt(tokenLength - 1) == 's')) {
				// if last 2 characters of the token are => 's
				newToken = token.substring(0, indexOfApostrophe);
			} else {
				newToken = token;
			}
			
			processedStream[i] = newToken;
		}
		
		
		return processedStream;
	}*/
	
	private static String removeHyphens(String tokenText) {
		
		if(tokenText == null || tokenText.equals(StringPool.BLANK)) {
			return StringPool.BLANK;
		}
		boolean isNumber;
		String[] parts;
		Pattern pattern = Pattern.compile(REGEX_TO_MATCH_DIGITS);
		Matcher matcher;
			isNumber = false;
			parts = tokenText.split("-");
			if(parts.length == 0) {
				// ignore the token since it is only hyphen(s)
				return StringPool.BLANK;
			}
			
			else if(parts.length == 1) {
				// add this part as it is, since the hyphen(s) are removed
				return parts[0];
			}
			
			else if(parts.length == 2) {
				// if either of the part is empty, add the other part, since both parts cannot be empty
				if(parts[0].isEmpty() || parts[0].equals(StringPool.BLANK)) {
					return parts[1];
				} else if(parts[1].isEmpty() || parts[1].equals(StringPool.BLANK)) {
					return parts[0];
				}
				
				// check if number is present in either of the parts
				matcher = pattern.matcher(parts[0]);
				if(!matcher.find()) {
					matcher = pattern.matcher(parts[1]);
					if(matcher.find()) {
						isNumber = true;
					}
				} else {
					isNumber = true;
				}
				
				// if number is present, then preserve the token
				// else return space separated value for token e.g. week-day => week day
				if(isNumber) {
					return tokenText;
				} else {
					return tokenText.replace(StringPool.HYPHEN, StringPool.SPACE);
				}
			}
			
			else {
				// remove all the hyphens and add the non-empty parts
				/*for(String part: parts) {
					if(!part.isEmpty() && !part.equals("")) {
						processedStream.add(part);
					}
				}*/
				return tokenText.replaceAll(StringPool.HYPHEN, StringPool.BLANK);
			}
	}
	
	@Override
	public TokenStream getStream() {
		return ts;
	}
	

}
