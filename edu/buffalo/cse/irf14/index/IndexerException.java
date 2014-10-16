/**
 * 
 */
package edu.buffalo.cse.irf14.index;

/**
 * Generic wrapper exception class for indexing exceptions
 * @author nikhillo, sghode, amitpuru
 */
public class IndexerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3012675871474097239L;
	private String msg;
	
	public IndexerException() {
		super();
	}
	
	public IndexerException(String msg) {
		super(msg);
		this.msg = msg;
	}
	
	public IndexerException(Throwable cause) {
		super(cause);
	}
	
	public String getMessage() {
		return msg;
	}
}
