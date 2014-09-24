package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;

import edu.buffalo.cse.irf14.util.Month;
import edu.buffalo.cse.irf14.util.StringPool;

public class DateTokenFilter extends TokenFilter {

	public DateTokenFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		
		if(ts.size() == 0) {
			return false;
		}
		String newTokenString = null;
		
		Token firstToken = null;
		Token secondToken = null;
		Token thirdToken = null;
		Token newToken;
		String firstTokenString = StringPool.BLANK;
		String secondTokenString = StringPool.BLANK;
		String thirdTokenString = StringPool.BLANK;
		String date = null;
		String month = null;
		String year = null;
		
		TokenStream newTokenStream = new TokenStream();

		ts.reset();
		System.out.println("\n\n");
		while(ts.hasNext()) {
			Token token = ts.next();
			System.out.print("[" + token.toString() + "]");
		}
		System.out.println();
		
		ts.reset();
		if(ts.hasNext()) {
			firstToken = ts.next();
		}
		if(ts.hasNext()) {
			secondToken = ts.next();
		}
		if(ts.hasNext()) {
			thirdToken = ts.next();
		}

		while(firstToken != null) {
			try
			{
				int matched = 0;
				firstTokenString = firstToken.toString();
				if(secondToken != null) {
					secondTokenString = secondToken.toString();
				}
				else {
					secondTokenString = null;
				}
				if(thirdToken != null) {
					thirdTokenString = thirdToken.toString();
				}
				
				date = null;
				year = null;
				newTokenString = firstTokenString;

				if((month = isMonth(firstTokenString)) != null) {
					// test for month-date-year
					matched++;
					if((secondToken != null) && (date = getDate(secondTokenString)) != null)
					{				
						matched++;
						if(thirdToken != null) {
							year = getYear(thirdTokenString);	
							if(year != null) {
								matched++;
								newTokenString = year + month + date;
							}
							else {
								newTokenString = DEFAULT_YEAR + month + date;
							}
						} else {
							newTokenString = DEFAULT_YEAR + month + date;
						}
					}
					else if((secondToken != null) && (year = getYear(secondTokenString)) != null) {
						matched++;
						newTokenString = year + month + DEFAULT_DATE;
					} else {
						newTokenString = DEFAULT_YEAR + month + date;
					}
				} else if((secondToken!=null) && (month = isMonth(secondTokenString)) != null) {
					// test case for date-month-year
					matched++;
					if((date = getDate(firstTokenString)) != null) {
						matched++;
						if((thirdToken != null) && (year = getYear(thirdTokenString)) != null) {
							matched++;
							newTokenString = year + month + date;
						}
						else {
							newTokenString = DEFAULT_YEAR + month + date;
						}
					}
					else {
						// not considering test for year-month
						matched--;
					}
				}	// end of month test

				// test for token containing AD or BC as part of the string
				else if((newTokenString = isADorBC(firstTokenString)) != null) {
					matched++;
				}
				// for cases like 1945/46
				else if (firstTokenString.matches("[0-9]{4}(-|/)[0-9]{2}")) {
					matched++;
					newTokenString = getCombinedYears(firstTokenString);
				}
				
				// for cases with just year, e.g. 1970, 250 BC
				else if(firstTokenString.matches("[0-9]{1,5}")) {
					matched++;
					if(secondToken != null) {
						if(secondTokenString.matches("(A[\\.]?D[\\.]?)")) {
							newTokenString = getEntireStringForYear(firstTokenString, 1);
							matched++;
						} else if(secondTokenString.matches("(B[\\.]?C[\\.]?)")) {
							newTokenString = getEntireStringForYear(firstTokenString, 2);
							matched++;
						} else {
							newTokenString = getEntireStringForYear(firstTokenString, 0);
						}
					}
					if(newTokenString == null) {
						matched = 0;
					}
				}
				
				
				if(matched == 0) {
					newTokenStream.add(firstToken);
				} else {
					newToken = new Token(newTokenString);
					newTokenStream.add(newToken);
				}
				
				if(ts.hasNext()) {
					switch(matched) {
					case 0:
					case 1:
						firstToken = secondToken;
						secondToken = thirdToken;
						thirdToken = ts.next();
						break;
						
					case 2:
						firstToken = thirdToken;
						secondToken = ts.next();
						if(ts.hasNext()) {
							thirdToken = ts.next();
						} else {
							thirdToken = null;
						}
						break;
						
					case 3:
						firstToken = ts.next();
						if(ts.hasNext()) {
							secondToken = ts.next();
						} else {
							secondToken = null;
							thirdToken = null;
							break;
						}
						if(ts.hasNext()) {
							thirdToken = ts.next();
						} else {
							thirdToken = null;
						}
						break;
					}
				} else {
					if(thirdToken != null) {
						if(matched == 3) {
							firstToken = null;
						} else if(matched == 2) {
							firstToken = thirdToken;
							secondToken = null;
						} else {
							firstToken = secondToken;
							secondToken = thirdToken;
						}
						thirdToken = null;
					} else if(secondToken != null) {
						if(matched == 2) {
							firstToken = null;
						} else {
							firstToken = secondToken;
							secondToken = null;
						}
					} else {
						firstToken = null;
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		
		ts = newTokenStream;
		ts.reset();
		System.out.println("\nProcessed:");
		while(ts.hasNext()) {
			Token token = ts.next();
			System.out.print("[" + token.toString() + "]");
		}
		System.out.println();
		return false;
	}

	@Override
	public TokenStream getStream() {
		return ts;
	}

	
	
	public static String isMonth(String token)
	{
		for (Month m : Month.values()) 
		{			
			if (m.name().equalsIgnoreCase(token)) 
			{				
				return m.getValue();
			}
		}		
		return null;		
	}

	public static String getDate(String token)
	{
		if (token.matches("[0-9]{1,2}"))
		{
			if(token.length() == 1)
			{
				token = "0" + token;
			}
			return token;
		}
		return null;		
	}

	public static String getYear(String token)
	{		
		if (token.matches("[0-9]{4}"))
		{	
			return token;
		}
		return null;		
	}

	
	public static String getCombinedYears(String token)
	{
		String splitter = StringPool.BLANK;
		if(token.contains(StringPool.SLASH)) {
			splitter = StringPool.SLASH;
		} else if(token.contains(StringPool.DASH)) {
			splitter = StringPool.DASH;
		}
			String firstYear = token.split(splitter)[0] + DEFAULT_MONTH + DEFAULT_DATE;
			String secondYear = token.split(splitter)[1];
			
			secondYear = firstYear.substring(0, 2) + secondYear + DEFAULT_MONTH + DEFAULT_DATE;
			return firstYear + StringPool.DASH + secondYear;
	}

	public static String isADorBC(String token)
	{		
		boolean isBC = false;
		int tokenLen;
		if (token.matches("[0-9]{1,4}AD") || token.matches("[0-9]{1,5}BC"))
		{	
			if(token.contains("BC")) {
				isBC = true;
			}
			token = token.replaceAll("(AD|BC)", StringPool.BLANK);

			tokenLen = token.length();
			if(tokenLen < 4)
			{
				int lenghthShortBy = 4 - tokenLen;
				for(int i=0; i<lenghthShortBy; i++)
				{
					token = "0" + token;
				}
			}
			if(isBC) {
				token = StringPool.HYPHEN + token;
			}
			return token + DEFAULT_MONTH + DEFAULT_DATE;
		}
		return null;		
	}
	
	public static String getEntireStringForYear(String token, int identifier)
	{
		int tokenLen = token.length();
		int year = Integer.parseInt(token);
		
		if((year < LOWER_LIMIT_FOR_YEAR || year > UPPER_LIMIT_FOR_YEAR) && (identifier == 0)) {
			return null;
		}
		
		if (tokenLen < 4)
		{
			int lenghthShortBy = 4 - tokenLen;
			for(int i=0; i<lenghthShortBy; i++)
			{
				token = "0" + token;
			}
		}
		
		if(identifier == 2) { 
			token = StringPool.DASH + token;
		}

		return token + DEFAULT_MONTH + DEFAULT_DATE;
	}
	
	
	private static final String DEFAULT_DATE = "01";
	private static final String DEFAULT_MONTH = "01";
	private static final String DEFAULT_YEAR = "1900";
	private static int LOWER_LIMIT_FOR_YEAR = 1000;
	private static int UPPER_LIMIT_FOR_YEAR = 2100;
}
