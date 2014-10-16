/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.StringPool;

/**
 * Class that converts a given string into a {@link TokenStream} instance
 * @author nikhillo, sghodke, amitpuru
 */
public class Tokenizer {
	
	// delimiter that will be used for tokenizing
	String delim;
	
	/**
	 * Default constructor. Assumes tokens are whitespace delimited
	 */
	public Tokenizer() {
		delim = StringPool.SPACE;
	}
	
	/**
	 * Overloaded constructor. Creates the tokenizer with the given delimiter
	 * @param delim : The delimiter to be used
	 */
	public Tokenizer(String delim) {
		this.delim = delim;
	}
	
	/**
	 * Method to convert the given string into a TokenStream instance.
	 * This must only break it into tokens and initialize the stream.
	 * No other processing must be performed. Also the number of tokens
	 * would be determined by the string and the delimiter.
	 * So if the string were "hello world" with a whitespace delimited
	 * tokenizer, you would get two tokens in the stream. But for the same
	 * text used with lets say "~" as a delimiter would return just one
	 * token in the stream.
	 * @param str : The string to be consumed
	 * @return : The converted TokenStream as defined above
	 * @throws TokenizerException : In case any exception occurs during
	 * tokenization
	 */
	public TokenStream consume(String str) throws TokenizerException {
		// TODO create property file for exception messages
		if(null == str) {
			throw new TokenizerException("Cannot consume null string");
		}
		
		if(StringPool.BLANK.equals(str)) {
			throw new TokenizerException("Cannot consume empty string");
		}
		
		TokenStream ts = new TokenStream();
		Token token;
		int pos = 0;
		
		try {
			String[] parts = str.split(delim);
			for(String part: parts) {
				if(StringPool.BLANK.equals(part)) {
					continue;
				}
				token = new Token(part);
				pos++;
				token.setPos(pos);
				ts.add(token);
			}
			
			return ts;
			
		} catch(Exception e) {
			throw new TokenizerException(e.getMessage());
		}
	}
}
