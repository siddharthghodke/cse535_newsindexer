/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * Wrapper exception class for any errors during Tokenization
 * @author nikhillo, sghodke, amitpuru
 */
public class TokenizerException extends Exception {

	private static final long serialVersionUID = 215747832619773661L;
	private String msg;
	
	public TokenizerException() {
		super();
	}
	
	public TokenizerException(String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public TokenizerException(Throwable e) {
		super(e);
	}
	
	public String getMessage() {
		return msg;
	}

}
