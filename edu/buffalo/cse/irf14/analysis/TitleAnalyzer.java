package edu.buffalo.cse.irf14.analysis;

public class TitleAnalyzer implements Analyzer {
	
	TokenStream ts;
	TokenFilter capitalizationTF, symbolTF, stopwordTF;
	
	/**
	 * pass the given token through {@link CapitalizationTokenFilter},
	 * then through {@link SymbolTokenFilter}, then through
	 * {@link StopwordTokenFilter}
	 */
	
	public TitleAnalyzer(TokenStream stream) {
		capitalizationTF = new CapitalizationTokenFilter(stream);
		try {
			while(capitalizationTF.increment());
			
			symbolTF = new SymbolTokenFilter(capitalizationTF.getStream());
			while(symbolTF.increment());
			
			stopwordTF = new StopwordTokenFilter(symbolTF.getStream());
		
		} catch (TokenizerException e) {
			e.printStackTrace();
		}
		ts = stopwordTF.getStream();
		ts.reset();
	}
	


	@Override
	public boolean increment() throws TokenizerException {
		//System.out.println("in TitleAnalyzer#increment()");
		if(ts.hasNext()) {
			return(stopwordTF.increment());
			//System.out.println("TA_token:" + ts.getCurrent());
		}
		else {
			return false;
		}
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return ts;
	}

}
