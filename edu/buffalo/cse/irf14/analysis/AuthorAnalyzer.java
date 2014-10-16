/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author amitpuru, sghodke
 *
 */
public class AuthorAnalyzer implements Analyzer {
	
	TokenStream ts;
	TokenFilter filter;
	
	public AuthorAnalyzer(TokenStream stream) {
		try {
			TokenFilterFactory tff = TokenFilterFactory.getInstance();
			filter = tff.getFilterByType(TokenFilterType.STOPWORD, stream);
			ts = filter.getStream();
			ts.reset();
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public boolean increment() throws TokenizerException {
		if(ts.hasNext()) {
			return filter.increment();
		}
		else {
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}

}
