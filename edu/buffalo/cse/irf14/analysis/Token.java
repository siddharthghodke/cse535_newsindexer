/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.StringPool;

/**
 * This class represents the smallest indexable unit of text.
 * At the very least it is backed by a string representation that
 * can be interchangeably used with the backing char array.
 * 
 * You are encouraged to add more data structures and fields as you deem fit. 
 * @author nikhillo, sghodke, amitpuru
 */
public class Token {
	//The backing string representation -- can contain extraneous information
	private String termText;
	//The char array backing termText
	private char[] termBuffer;
	
	// position in given text
	private int pos;
	
	/**
	 * Constructor to initialize Token with given string
	 */
	public Token(String str) {
		termText = str;
		termBuffer = (termText != null) ? termText.toCharArray() : null;
	}
	
	/**
	 * Method to set the termText to given text.
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @param text
	 */
	protected void setTermText(String text) {
		termText = text;
		termBuffer = (termText != null) ? termText.toCharArray() : null;
	}
	
	/**
	 * Getter for termText
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @return the underlying termText
	 */
	protected String getTermText() {
		return termText;
	}
	
	/**
	 * Method to set the termBuffer to the given buffer.
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @param buffer: The buffer to be set
	 */
	protected void setTermBuffer(char[] buffer) {
		termBuffer = buffer;
		termText = new String(buffer);
	}
	
	/**
	 * Getter for the field termBuffer
	 * @return The termBuffer
	 */
	protected char[] getTermBuffer() {
		return termBuffer;
	}
	
	/**
	 * Method to merge this token with the given array of Tokens
	 * You are free to update termText and termBuffer as you please
	 * based upon your Token implementation. But the toString() method
	 * below must return whitespace separated value for all tokens combined
	 * Also the token order must be maintained.
	 * @param tokens The token array to be merged
	 */
	protected void merge(Token...tokens) {
		
		if(tokens == null) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(termText);
		for(Token token: tokens) {
			sb.append(StringPool.SPACE + token.getTermText());
		}
		termText = sb.toString();
		termBuffer = (termText != null) ? termText.toCharArray() : null;
	}
	
	/**
	 * Returns the string representation of this token. It must adhere to the
	 * following rules:
	 * 1. Return only the associated "text" with the token. Any other information 
	 * must be suppressed.
	 * 2. Must return a non-empty value only for tokens that are going to be indexed
	 * If you introduce special token types (sentence boundary for example), return
	 * an empty string
	 * 3. IF the original token A (with string as "a") was merged with tokens B ("b"),
	 * C ("c") and D ("d"), toString should return "a b c d"
	 * @return The raw string representation of the token
	 */
	@Override
	public String toString() {

		if(termText != null) {
			return termText;
		}
		return null;
	}
	
	public void setPos(int pos) {
		this.pos = pos;
	}
	
	public int getPos() {
		return pos;
	}
}
