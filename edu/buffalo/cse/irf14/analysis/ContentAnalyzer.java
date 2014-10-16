package edu.buffalo.cse.irf14.analysis;

public class ContentAnalyzer implements Analyzer {

	TokenStream ts;
	TokenFilter filter;
	
	public ContentAnalyzer(TokenStream stream) {
		try {
			TokenFilterFactory tff = TokenFilterFactory.getInstance();

			filter = tff.getFilterByType(TokenFilterType.SYMBOL, stream);
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.STOPWORD, filter.getStream());
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.SPECIALCHARS, filter.getStream());
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.DATE, filter.getStream());
			while(filter.increment());

			filter = tff.getFilterByType(TokenFilterType.NUMERIC, filter.getStream());
			while(filter.increment());

			filter = tff.getFilterByType(TokenFilterType.STEMMER, filter.getStream());
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.CAPITALIZATION, filter.getStream());
			while(filter.increment());

			filter = tff.getFilterByType(TokenFilterType.ACCENT, filter.getStream());
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
		if(ts.hasNext()) {
			return filter.increment();
		} else {
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}

}
