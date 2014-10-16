/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.util.Constants;


/**
 * Factory class for instantiating a given TokenFilter
 * @author nikhillo, sghodke, amitpuru
 *
 */
public class TokenFilterFactory {
	
	private static final TokenFilterFactory INSTANCE = new TokenFilterFactory();
	
	// making the constructor private so that no other class can instantiate it
	private TokenFilterFactory() {
		
	}
	/**
	 * Static method to return an instance of the factory class.
	 * Usually factory classes are defined as singletons, i.e. 
	 * only one instance of the class exists at any instance.
	 * This is usually achieved by defining a private static instance
	 * that is initialized by the "private" constructor.
	 * On the method being called, you return the static instance.
	 * This allows you to reuse expensive objects that you may create
	 * during instantiation
	 * @return An instance of the factory
	 */
	public static TokenFilterFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Returns a fully constructed {@link TokenFilter} instance
	 * for a given {@link TokenFilterType} type
	 * @param type: The {@link TokenFilterType} for which the {@link TokenFilter}
	 * is requested
	 * @param stream: The TokenStream instance to be wrapped
	 * @return The built {@link TokenFilter} instance
	 */
	public TokenFilter getFilterByType(TokenFilterType type, TokenStream stream) {
		
		if(type.toString().equals(Constants.STOPWORD)) {
			return new StopwordTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.ACCENT)) {
			return new AccentTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.SYMBOL)) {
			return new SymbolTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.STEMMER)) {
			return new StemmerTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.SPECIALCHARS)) {
			return new SpecialCharsTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.CAPITALIZATION)) {
			return new CapitalizationTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.DATE)) {
			return new DateTokenFilter(stream);
		}
		else if(type.toString().equals(Constants.NUMERIC)) {
			return new NumericTokenFilter(stream);
		}
		return null;
	}
}
