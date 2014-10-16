package edu.buffalo.cse.irf14.analysis;

import java.text.DecimalFormat;

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
		String punctuationMark = StringPool.BLANK;

		TokenStream newTokenStream = new TokenStream();
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
				punctuationMark = StringPool.BLANK;

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
								if(year.substring(year.length()-1, year.length()).matches(REGEX_FOR_PUNCTUATION_AT_END))
								{
									punctuationMark = year.substring(year.length()-1, year.length());
									year = year.replaceAll("[\\.,?!:;-]?", StringPool.BLANK);
								}
								newTokenString = year + month + date + punctuationMark;
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
						// not considering match for only month
						matched--;
					}
				} else if((secondToken!=null) && (month = isMonth(secondTokenString)) != null) {
					// test case for date-month-year
					matched++;
					if((date = getDate(firstTokenString)) != null) {
						matched++;
						if((thirdToken != null) && (year = getYear(thirdTokenString)) != null) {
							matched++;
							if(year.substring(year.length()-1, year.length()).matches(REGEX_FOR_PUNCTUATION_AT_END))
							{
								punctuationMark = year.substring(year.length()-1, year.length());
								year = year.replaceAll(REGEX_FOR_PUNCTUATION_AT_END, StringPool.BLANK);
							}
							newTokenString = year + month + date + punctuationMark;
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
				else if(firstTokenString.matches("([0-9]{1,4}AD[\\.,;:?!]?)|([0-9]{1,4}BC[\\.,;:?!]?)")) {	
					newTokenString = isADorBC(firstTokenString);

					matched++;
				}
				// for cases like 1945/46
				else if (firstTokenString.matches("[0-9]{4}(-|/)[0-9]{2}[//.,;:?!]?")) {
					matched++;
					if(firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length()).matches(REGEX_FOR_PUNCTUATION_AT_END))
					{
						punctuationMark = firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length());
						firstTokenString = firstTokenString.replace(firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length()), StringPool.BLANK);
					}
					newTokenString = getCombinedYears(firstTokenString)+punctuationMark;
				}

				// for cases with just year, e.g. 1970, 250 BC
				else if(firstTokenString.matches("[0-9]{1,5}[//.,;:?!]?")) {
					matched++;
					if(secondToken != null) {
						if(secondTokenString.matches("(A[\\.]?D[\\.]?[\\.]?)")) {
							if(secondTokenString.substring(secondTokenString.length()-1, secondTokenString.length()).matches(REGEX_FOR_PUNCTUATION_AT_END)){
								{
									punctuationMark = secondTokenString.substring(secondTokenString.length()-1, secondTokenString.length());																		
								}		
							} 
							newTokenString = getEntireStringForYear(firstTokenString, 1);
							matched++;
						} else if(secondTokenString.matches("(B[\\.]?C[\\.]?)")) {
							if(firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length()).matches(REGEX_FOR_PUNCTUATION_AT_END)){
								{
									punctuationMark = secondTokenString.substring(secondTokenString.length()-1, secondTokenString.length());																		
								}		
							} 
							newTokenString = getEntireStringForYear(firstTokenString, 2);
							matched++;
						} else{
							if(firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length()).matches(REGEX_FOR_PUNCTUATION_AT_END)){
								{
									punctuationMark = firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length());
									firstTokenString = firstTokenString.replaceAll(REGEX_FOR_PUNCTUATION_AT_END, StringPool.BLANK);									
								}		
							} 
							newTokenString = getEntireStringForYear(firstTokenString, 0);
						}
					}else{ 
						if(firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length()).matches(REGEX_FOR_PUNCTUATION_AT_END)){
							{
								punctuationMark = firstTokenString.substring(firstTokenString.length()-1, firstTokenString.length());
								firstTokenString = firstTokenString.replaceAll(REGEX_FOR_PUNCTUATION_AT_END, StringPool.BLANK);									
							}		
						} 
						newTokenString = getEntireStringForYear(firstTokenString, 0);
					}

					if(newTokenString == null) {
						matched = 0;
					} else {
						newTokenString = newTokenString + punctuationMark;
					}
				}
				// for cases with time HH:MM:SS [space] PM/AM
				else if(secondTokenString!=null && (firstTokenString.matches("[0-9]{1,2}[(.|:)[0-9]{2}]*[(.|:)[0-9]{2}]*")) && (secondTokenString.matches("(AM|PM|am|pm)[\\.,?:;!]?"))){
					matched++;

					String timeToken;
					String lastSymbol = StringPool.BLANK;

					if(secondTokenString.matches("(AM|PM|am|pm)[\\.,;:!?]?"))
					{
						matched++;
						timeToken = secondTokenString.replaceAll(REGEX_FOR_PUNCTUATION_AT_END, StringPool.BLANK);
						timeToken = secondTokenString.replace(StringPool.COMMA, StringPool.BLANK);

						if(secondTokenString.substring(secondTokenString.length() - 1).matches(REGEX_FOR_PUNCTUATION_AT_END))
						{
							lastSymbol = secondTokenString.substring(secondTokenString.length() - 1);
						}

						newTokenString = getTimeString(firstTokenString,timeToken)+lastSymbol;
					}
					else
					{
						newTokenString = getTimeString(firstTokenString,null);
					}					
				}
				
				// for cases with time HH:MM:SSPM/AM    i.e AM/PM attached without space
				else if(firstTokenString.matches(REGEX_TO_MATCH_TIME))
				{			
					matched++;
					String lastSymbol=StringPool.BLANK;

					if(firstTokenString.substring(firstTokenString.length() - 1).matches(REGEX_FOR_PUNCTUATION_AT_END))
					{
						lastSymbol = firstTokenString.substring(firstTokenString.length() - 1);
					}

					newTokenString = getTimeString(firstTokenString,null) + lastSymbol;											

				}


				if(matched == 0) {
					newTokenStream.add(firstToken);
				} else {
					newToken = new Token(newTokenString);
					newToken.setPos(firstToken.getPos());
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
		if (token.matches("[0-9]{1,2}[\\.,?!:;]?"))
		{
			token = token.replaceAll(REGEX_FOR_PUNCTUATION_AT_END, StringPool.BLANK);
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
		if (token.matches("[0-9]{4}[\\.,?!:;]?"))
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
		String punctuationMark = StringPool.BLANK;

		if(token.contains("BC")) {
			isBC = true;
		}

		if(token.substring(token.length()-1, token.length()).matches(REGEX_FOR_PUNCTUATION_AT_END) )
		{
			punctuationMark = token.substring(token.length()-1, token.length());				
		}

		token = token.replaceAll("(AD|BC|,|;|:|!|\\?|\\.)", StringPool.BLANK);

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
		return token + DEFAULT_MONTH + DEFAULT_DATE + punctuationMark;				
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

	public static String getTimeString(String token,String nextToken)
	{
		String hour = null;
		String minutes = null;
		String seconds = null;
		String time = null;
		boolean isPM = false;
		int hourValue = 00;
		DecimalFormat formatter = new DecimalFormat("00");

		if(nextToken!=null)
		{
			if (nextToken.equals("PM")||nextToken.equals("pm"))
			{
				isPM=true;
			}
		}
		else
		{
			if(token.contains("AM")||token.contains("am"))
			{
				token = token.replaceAll("(AM|am)[\\.,;:!?]?", StringPool.BLANK);
			}else{

				isPM = true;
				token = token.replaceAll("(PM|pm)[\\.,;:!?]?", StringPool.BLANK);
			}
		}

		// if time is in format HH:MM
		if(token.matches("[0-9]{1,2}(\\.|:)[0-9]{2}"))
		{			
			// if time values are seperated by colon
			if(token.contains(":"))
			{			
				if(isPM && Integer.parseInt(token.split(StringPool.COLON)[0])<12)
				{
					hourValue = Integer.parseInt(token.split(StringPool.COLON)[0])+12;
				}
				else
				{
					hourValue = Integer.parseInt(token.split(StringPool.COLON)[0]);
				}

				hour = formatter.format(hourValue);
				minutes = token.split(StringPool.COLON)[1];
				seconds = DEFAULT_SECONDS;
			}
			// if time values are seperated by period ('.')
			else if(token.contains(StringPool.PERIOD))
			{
				if(isPM && (Integer.parseInt(token.split(REGEX_TO_SPLIT_ON_PERIOD)[0])<12))
				{
					hourValue = Integer.parseInt(token.split(REGEX_TO_SPLIT_ON_PERIOD)[0])+12;
				}
				else
				{
					hourValue = Integer.parseInt(token.split(REGEX_TO_SPLIT_ON_PERIOD)[0]);
				}

				hour = formatter.format(hourValue);
				minutes = token.split(REGEX_TO_SPLIT_ON_PERIOD)[1];
				seconds = DEFAULT_SECONDS;
			}								
			time = hour +  StringPool.COLON + minutes + StringPool.COLON + seconds;
			return time;
		} //END of if time is in format HH:MM
		
		//If time is in format HH:MM::SS
		else if(token.matches("[0-9]{1,2}(.|:)[0-9]{2}(.|:)[0-9]{2}"))
		{
			// if time values are seperated by colon
			if(token.contains(":"))
			{			
				if(isPM && Integer.parseInt(token.split(StringPool.COLON)[0])<12)
				{
					hourValue = Integer.parseInt(token.split(StringPool.COLON)[0])+12;
				}
				else
				{
					hourValue = Integer.parseInt(token.split(StringPool.COLON)[0]);
				}

				hour = formatter.format(hourValue);
				minutes = token.split(StringPool.COLON)[1];
				seconds = token.split(StringPool.COLON)[2];

			}
			// if time values are seperated by period ('.')
			else if(token.contains(StringPool.PERIOD))
			{
				if(isPM && Integer.parseInt(token.split(REGEX_TO_SPLIT_ON_PERIOD)[0])<12)
				{
					hourValue = Integer.parseInt(token.split(REGEX_TO_SPLIT_ON_PERIOD)[0])+12;
				}
				else
				{
					hourValue = Integer.parseInt(token.split(REGEX_TO_SPLIT_ON_PERIOD)[0]);
				}

				hour = formatter.format(hourValue);
				minutes = token.split(REGEX_TO_SPLIT_ON_PERIOD)[1];
				seconds = token.split(REGEX_TO_SPLIT_ON_PERIOD)[2];
			}								
			time = hour +  StringPool.COLON + minutes + StringPool.COLON + seconds;			
			return time;
		}//END of if time is in format HH:MM:SS			
		// if time does not have minutes and seconds
		else
		{
			if(isPM && Integer.parseInt(token)<12)
			{
				hourValue = Integer.parseInt(token)+12;
			}
			else
			{
				hourValue = Integer.parseInt(token);
			}

			hour = formatter.format(hourValue);
			minutes = DEFAULT_MINUTES;
			seconds = DEFAULT_SECONDS;
			time = hour +  StringPool.COLON + minutes + StringPool.COLON + seconds;		
			return time;
		}
	}


	private static final String DEFAULT_DATE = "01";
	private static final String DEFAULT_MONTH = "01";
	private static final String DEFAULT_YEAR = "1900";
	private static int LOWER_LIMIT_FOR_YEAR = 1700;
	private static int UPPER_LIMIT_FOR_YEAR = 2100;
	private static final String DEFAULT_MINUTES = "00";
	private static final String DEFAULT_SECONDS = "00";
	private static final String REGEX_FOR_PUNCTUATION_AT_END = "[//.,;:?!]?";
	private static final String REGEX_TO_MATCH_TIME = "[0-9]{1,2}[(.|:)[0-9]{2}]*[(.|:)[0-9]{2}]*(AM|PM|am|pm)[//.,!?:;]?";
	private static final String REGEX_TO_SPLIT_ON_PERIOD = "\\.";
}