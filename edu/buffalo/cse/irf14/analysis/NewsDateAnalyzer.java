package edu.buffalo.cse.irf14.analysis;

public class NewsDateAnalyzer implements Analyzer {

	TokenStream ts;
	TokenFilter filter;

	public NewsDateAnalyzer(TokenStream stream) {
		try {
			TokenFilterFactory tff = TokenFilterFactory.getInstance();

			filter = tff.getFilterByType(TokenFilterType.SYMBOL, stream);
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.SPECIALCHARS, filter.getStream());
			while(filter.increment());

			filter = tff.getFilterByType(TokenFilterType.DATE, filter.getStream());
			while(filter.increment());
			
			ts = filter.getStream();
			ts.reset();
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public boolean increment() throws TokenizerException {
		return false;
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}

}
