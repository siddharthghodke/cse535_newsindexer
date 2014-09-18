/**
 * 
 */
package edu.buffalo.cse.irf14.document;

/**
 * Generic wrapper exception class for parsing exceptions
 * @author nikhillo, sghodke, amitpuru
 */
public class ParserException extends Exception {

	private static final long serialVersionUID = 4691717901217832517L;
	private String msg = null;

	public ParserException() {
		super();
	}
	
	public ParserException(String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public ParserException(Throwable cause) {
		super(cause);
	}
	
    public String getMessage() {
        return msg;
    }
	
}
