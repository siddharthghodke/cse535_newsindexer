/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that represents a stream of Tokens. All {@link Analyzer} and
 * {@link TokenFilter} instances operate on this to implement their
 * behavior
 * @author nikhillo, amitpuru, sghodke
 */
public class TokenStream implements Iterator<Token>{
	
	private List<Token> ts;
	private int pointer;
	/*
	 * flag to verify if next item is fetched by the next() method
	 * used for getCurrent() and remove() methods
	 * */
	private boolean nextFetched;
	private boolean pointerResetAfterRemove;
	
	/**
	 * Default constructor
	 */
	public TokenStream() {

		ts = new ArrayList<Token>();
		pointer = -1;
		nextFetched = false;
		pointerResetAfterRemove = false;
	}
	
	/**
	 * Method that checks if there is any Token left in the stream
	 * with regards to the current pointer.
	 * DOES NOT ADVANCE THE POINTER
	 * @return true if at least one Token exists, false otherwise
	 */
	@Override
	public boolean hasNext() {
		
		// return false if token stream is empty, or pointer has reached the end of the stream
		if(pointer == ts.size() || pointer == -1) {
			return false;
		}
		return true;
	}

	/**
	 * Method to return the next Token in the stream. If a previous
	 * hasNext() call returned true, this method must return a non-null
	 * Token.
	 * If for any reason, it is called at the end of the stream, when all
	 * tokens have already been iterated, return null
	 */
	@Override
	public Token next() {
		
		if(pointer > -1 && pointer < ts.size()) {
			nextFetched = true;
			pointerResetAfterRemove = false;
			return ts.get(pointer++);
		}
		
		// return null since the token stream is empty or pointer is beyond last element
		nextFetched = false;
		return null;
	}
	
	/**
	 * Method to remove the current Token from the stream.
	 * Note that "current" token refers to the Token just returned
	 * by the next method. 
	 * Must thus be NO-OP when at the beginning of the stream or at the end
	 */
	@Override
	public void remove() {

		if(!nextFetched) {
			return;
		}
		pointer--;
		if(pointer < 0 || pointer >= ts.size()) {
			pointer++;
			return;
		}
		ts.remove(pointer);
		nextFetched = false;
	}
	
	/**
	 * Method to reset the stream to bring the iterator back to the beginning
	 * of the stream. Unless the stream has no tokens, hasNext() after calling
	 * reset() must always return true.
	 */
	public void reset() {

		if(ts.isEmpty()) {
			pointer = -1;
		}
		pointer = 0;
	}
	
	/**
	 * Method to append the given TokenStream to the end of the current stream
	 * The append must always occur at the end irrespective of where the iterator
	 * currently stands. After appending, the iterator position must be unchanged
	 * Of course this means if the iterator was at the end of the stream and a 
	 * new stream was appended, the iterator hasn't moved but that is no longer
	 * the end of the stream.
	 * @param stream : The stream to be appended
	 */
	public void append(TokenStream stream) {
		
		if(stream == null) {
			return;
		}
		if(stream.size() == 0) {
			return;
		}

		stream.reset();
		while(stream.hasNext()) {
			ts.add(stream.next());
		}
		
	}
	
	/**
	 * Method to get the current Token from the stream without iteration.
	 * The only difference between this method and {@link TokenStream#next()} is that
	 * the latter moves the stream forward, this one does not.
	 * Calling this method multiple times would not alter the return value of {@link TokenStream#hasNext()}
	 * @return The current {@link Token} if one exists, null if end of stream
	 * has been reached or the current Token was removed
	 */
	public Token getCurrent() {
		if(!nextFetched && !pointerResetAfterRemove) {
			return null;
		}
		if(pointer - 1 < 0 || pointer > ts.size()) {
			return null;
		}
		
		return ts.get(pointer - 1);
	}
	
	/**
	 * Method to add a single token to the end of the current stream
	 * The token will always be added at the end, irrespective of where the iterator 
	 * currently stands. The iterator position must be unchanged after adding the token
	 * @param token : The token to be added
	 */
	public void add(Token token) {
		ts.add(token);
		if(pointer == -1) {
			pointer = 0;
		}
	}
	
	/**
	 * Method to return size of the token stream
	 */
	
	public int size() {
		return ts.size();
	}
	
	/**
	 * Method to decrement pointer to fetch the previous token AFTER calling remove()
	 * <br><b>WARNING: </b>USE THIS ONLY AFTER A CALL TO remove(). Will result in unexpected 
	 * results otherwise
	 */
	
	public void resetPointerAfterRemove() {
		if(pointer > 0) {
			pointerResetAfterRemove = true;
		}
		return;
	}
	
}
