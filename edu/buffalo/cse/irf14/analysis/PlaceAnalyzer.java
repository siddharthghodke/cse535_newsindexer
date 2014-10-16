package edu.buffalo.cse.irf14.analysis;

public class PlaceAnalyzer implements Analyzer {
	
	TokenStream ts;
	TokenFilter filter;
	
	public PlaceAnalyzer(TokenStream stream) {
		try {
			TokenFilterFactory tff = TokenFilterFactory.getInstance();
					
			filter = tff.getFilterByType(TokenFilterType.CAPITALIZATION, stream);
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.SPECIALCHARS, filter.getStream());
			while(filter.increment());
			
			filter = tff.getFilterByType(TokenFilterType.SYMBOL, filter.getStream());
			
			ts = filter.getStream();
			ts.reset();
		} catch (Exception e) {
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
