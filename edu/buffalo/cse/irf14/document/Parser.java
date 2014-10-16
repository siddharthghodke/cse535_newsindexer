/**
 * 
 */
package edu.buffalo.cse.irf14.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.util.StringPool;

/**
 * Class that parses a given file into a Document
 * @author nikhillo, sghodke, amitpuru
 */
public class Parser {
	/**
	 * Static method to parse the given file into the Document object
	 * @param filename : The fully qualified filename to be parsed
	 * @return The parsed and fully loaded Document object
	 * @throws ParserException In case any error occurs during parsing
	 */
	
	public static Document parse(String filename) throws ParserException {
		
		// TODO property file which maps exception properties
		if(null == filename) {
			throw new ParserException("Filename not provided");
		}
		if(StringPool.BLANK.equals(filename)) {
			throw new ParserException("Blank filename provided");
		}
		
		Document doc = new Document();
		String title = StringPool.BLANK;
		String[] author = null;
		String[] org = null;
		String place = StringPool.BLANK;
		String date = StringPool.BLANK;
		String content = StringPool.BLANK;
		
		String pattern = Pattern.quote(File.separator);
		String[] filePathElements = filename.split(pattern);
		if(filePathElements.length < 2) {
			throw new ParserException("Invalid File Path");
		}
		String fileName = filePathElements[filePathElements.length - 1];
		String fileCategory = filePathElements[filePathElements.length - 2];
		
		
		doc.setField(FieldNames.FILEID, fileName);
		doc.setField(FieldNames.CATEGORY, fileCategory);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			title = getTitle(br);
			String[] authorAndOrg = getAuthorOrg(br);
			String authorString, orgString;
			if(authorAndOrg != null) {
				authorString = authorAndOrg[0].trim();
				author = new String[]{authorString};
				
				if(authorAndOrg.length > 1) {
					orgString = authorAndOrg[1].trim();
					org = new String[]{orgString};
				}
			}
			
			String[] placeDateContent = getPlaceDateContent(br);
			if(placeDateContent != null) {
				place = placeDateContent[0];
				date = placeDateContent[1];
				content = placeDateContent[2];
			}
			
			String line = null;
			StringBuilder sb = new StringBuilder();
			sb.append(content);
			while((line = br.readLine()) != null) {
				sb.append(StringPool.SPACE + line);
			}
			content = sb.toString();
			
			doc.setField(FieldNames.TITLE, title);
			doc.setField(FieldNames.AUTHOR, author);
			doc.setField(FieldNames.AUTHORORG, org);
			doc.setField(FieldNames.PLACE, place);
			doc.setField(FieldNames.NEWSDATE, date);
			doc.setField(FieldNames.CONTENT, content);
			
			br.close();
			return doc;
			
		} catch (FileNotFoundException e) {
			throw new ParserException(e.getMessage());
		} catch (IOException e) {
			throw new ParserException(e.getMessage());
		} catch (Exception e) {
			throw new ParserException(e.getMessage());
		}
	}
	
	private static String getTitle(BufferedReader br) throws IOException {
		String line = null;
		
		br.mark(BUFFER_SIZE);
		while((line = br.readLine()) != null) {
			
			// ignore empty line
			if(line.isEmpty() || line.trim().equals(StringPool.BLANK) || line.trim().equals(StringPool.NEW_LINE)) {
				br.mark(BUFFER_SIZE);
				continue;
			}
			else {
				return line;
			}
		}
		return null;
	}
	
	private static String[] getAuthorOrg(BufferedReader br) throws IOException {
		String line = null;
		
		br.mark(BUFFER_SIZE);
		while((line = br.readLine()) != null) {
			// ignore empty line
			if(line.isEmpty() || line.trim().equals(StringPool.BLANK) || line.trim().equals(StringPool.NEW_LINE)) {
				br.mark(BUFFER_SIZE);
				continue;
			}
			
			if(line.contains(AUTHOR_START_TAG)) {
				String authorOrg = trimAuthorTags(line);
				// TODO better way to handle 3 cases of 'By'
				if(authorOrg.contains("BY") || authorOrg.contains("By") || authorOrg.contains("by")) {
					authorOrg = authorOrg.substring(3);
				}
				return authorOrg.split(StringPool.COMMA);
			}
			else {
				br.reset();
				return null;
			}
		}
		return null;
	}
	
	private static String trimAuthorTags(String authorStream) {
		
		int indexOfStartTag = authorStream.indexOf(AUTHOR_START_TAG);
		int indexOfEndTag = authorStream.indexOf(AUTHOR_END_TAG);
		
		if(indexOfStartTag != -1 && indexOfEndTag != -1) {
			String trimmedString = authorStream.substring(indexOfStartTag + 8, indexOfEndTag);
			return trimmedString.trim();
		}
		
		return authorStream;
	}

	
	/**
	 * Extract the first non empty line after author and sorts it into place, date and content
	 * @param br - BufferedReader pointer
	 * @return String[] placeDateStream where:</br> 0th location contains place</br>
	 * 1st location contains date</br>
	 * 2nd location contains content
	 * @throws IOException 
	 */
	private static String[] getPlaceDateContent(BufferedReader br) throws IOException {
		String line = null;
		String place = StringPool.BLANK;
		String date = StringPool.BLANK;
		String content = StringPool.BLANK;
		String dateAndPlace = null;
		String[] placeDateContent = new String[3];
		
		br.mark(BUFFER_SIZE);
		while((line = br.readLine()) != null) {
			if(line.isEmpty() || line.trim().equals(StringPool.BLANK) || line.trim().equals(StringPool.NEW_LINE)) {
				br.mark(BUFFER_SIZE);
				continue;
			}
		
			// found first non-empty line
			// check if it contains date and place
			// we'll be using hyphen as a delimiter to check if date and place exists
			
			int indexOfHyphen = line.indexOf(StringPool.HYPHEN);
			if(indexOfHyphen != -1) {
				dateAndPlace = line.substring(0, indexOfHyphen);
				content = line.substring(indexOfHyphen + 1);
				
				// find if place contains single place or comma separated 2 places
				int indexOfFirstComma = dateAndPlace.indexOf(StringPool.COMMA);
				int indexOfSecondComma = -1;
				if(indexOfFirstComma != -1) {
					indexOfSecondComma = dateAndPlace.indexOf(StringPool.COMMA, indexOfFirstComma + 1);
				}
				
				if(indexOfFirstComma == -1) {
					date = verifyDate(dateAndPlace);
					if(date == StringPool.BLANK) {
						place = verifyPlace(dateAndPlace);
					}
				} else if(indexOfSecondComma == -1) {
					place = dateAndPlace.substring(0, indexOfFirstComma);
					date = dateAndPlace.substring(indexOfFirstComma + 1);
				} else {
					place = dateAndPlace.substring(0, indexOfSecondComma);
					date = dateAndPlace.substring(indexOfSecondComma + 1);
				}
				
				date = verifyDate(date);
				if(StringPool.BLANK.equals(date) && StringPool.BLANK.equals(place)) {
					content = line;
				}
				
				placeDateContent[0] = place.trim();
				placeDateContent[1] = date.trim();
				placeDateContent[2] = content.trim();
			} else {
				// date and time do not exist
				placeDateContent[0] = StringPool.BLANK;
				placeDateContent[1] = StringPool.BLANK;
				placeDateContent[2] = line;
			}
			
			return placeDateContent;
		}
		
		return null;
	}
	
	/**
	 * Verify the place stream if it actually consists of place(s)
	 * @param placeStream
	 * @return
	 */
	private static String verifyPlace(String placeStream) {
		String place = placeStream.trim();
		if(place.split(REGEX_FOR_WHITESPACE_CHARACTERS).length > 2) {
			return StringPool.BLANK;
		}
		// TODO any other verifications for place??
		return place;
	}
	
	/**
	 * Verify the dateStream if it is actually a date
	 * @param dateStream
	 * @return
	 */
	private static String verifyDate(String dateStream) {
		dateStream = dateStream.trim();
		String[] dateContents = dateStream.split(REGEX_FOR_WHITESPACE_CHARACTERS);
		
		if(dateContents.length > 2) {
			return StringPool.BLANK;
		} else {
			try {
				Integer.parseInt(dateContents[1]);
			} catch (NumberFormatException e) {
				// Exception indicates that the date stream is not in the expected date format
				return StringPool.BLANK;
			} catch (ArrayIndexOutOfBoundsException e) {
				// Exception indicates there is only one field, hence not a valid date format
				return StringPool.BLANK;
			}
		}
		// TODO verify month as well
		return dateStream;
	}
	
	
	/*
	 * CONSTANTS/ Static variables
	 */
	
	// arbitrary value to reset buffered reader pointer
	final static int BUFFER_SIZE = 1000;
	
	// author start tag <AUTHOR>
	final static String AUTHOR_START_TAG = StringPool.LESS_THAN + FieldNames.AUTHOR + StringPool.GREATER_THAN;
	
	// author end tag </AUTHOR>
	final static String AUTHOR_END_TAG = StringPool.LESS_THAN + StringPool.SLASH + FieldNames.AUTHOR + StringPool.GREATER_THAN;
	
	// reg-ex to check presence of small case character
	final static String REGEX_FOR_LOWER_CASE = "[a-z]+";
	
	// reg-ex to check one or more occurences of whitespace characters
	final static String REGEX_FOR_WHITESPACE_CHARACTERS = "\\s+";
}
