/**
 * 
 */
package edu.buffalo.cse.irf14.document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.util.StringPool;

/**
 * @author nikhillo, sghodke, amitpuru
 * Class that parses a given file into a Document
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
		String author = null;
		String org = null;
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
			// use String split function
			//author = getAuthor(authorAndOrg);
			//org = getOrg(authorAndOrg);
			/// TODO return single author stream or author string array with multiple authors???
			if(authorAndOrg != null) {
				author = authorAndOrg[0].trim();
				if(authorAndOrg.length > 1) {
					org = authorAndOrg[1].trim();
				}
			}
			
			String[] placeDateContent = newGetPlaceDateContent(br);
			if(placeDateContent != null) {
				place = placeDateContent[0];
				date = placeDateContent[1];
				content = placeDateContent[2];
			}
			if(!StringPool.BLANK.equals(date)) {
				dateCount++;
			}
			if(!StringPool.BLANK.equals(place)) {
				writer.write(place + "\n");
				placeCount++;
			}
			
			String line = null;
			while((line = br.readLine()) != null) {
				content = content + line;
			}
			
			doc.setField(FieldNames.TITLE, title);
			doc.setField(FieldNames.AUTHOR, author);
			doc.setField(FieldNames.AUTHORORG, org);
			doc.setField(FieldNames.PLACE, place);
			doc.setField(FieldNames.NEWSDATE, date);
			doc.setField(FieldNames.CONTENT, content);
			
			/*
			System.out.println("\n\n#################################\n\n");
			System.out.println("FILEID:"  + fileName);
			System.out.println("FILE CATEGORY:" + fileCategory);
			System.out.println("TITLE:" + title);
			System.out.println("AUTHOR:" + author);
			System.out.println("ORG:" + org);
			System.out.println("PLACE:" + place);
			System.out.println("DATE:" + date);
			System.out.println("CONTENT:" + content);
			*/
			br.close();
			
			return doc;
			
		} catch (FileNotFoundException e) {
			// TODO
		} catch (IOException e) {
			// TODO 
		} catch (Exception e) {
			// TODO
		}
		 
		return null;
	}
	
	/*
	 * TODO inculcate changes for new rules for title
	 */
	private static String getTitle(BufferedReader br) {
		String line = null;
		// TODO check if this can be referenced at class level/ in static way
		Pattern titlePattern = Pattern.compile(REGEX_FOR_LOWER_CASE);
		Matcher matcher;
		
		try {
			br.mark(BUFFER_SIZE);
			while((line = br.readLine()) != null) {
				
				// ignore empty line
				if(line.isEmpty() || line.trim().equals(StringPool.BLANK) || line.trim().equals(StringPool.NEW_LINE)) {
					br.mark(BUFFER_SIZE);
					continue;
				}
				matcher = titlePattern.matcher(line);
				if(matcher.find()) {
					br.reset();
					return StringPool.BLANK;
				}
				else {
					//TODO remove this
					titleCount++;
					return line;
				}
			}
		} catch (IOException e) {
			// TODO
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	private static String[] getAuthorOrg(BufferedReader br) {
		String line = null;
		
		try {
			br.mark(BUFFER_SIZE);
			while((line = br.readLine()) != null) {
				// ignore empty line
				if(line.isEmpty() || line.trim().equals(StringPool.BLANK) || line.trim().equals(StringPool.NEW_LINE)) {
					br.mark(BUFFER_SIZE);
					continue;
				}
				
				if(line.contains(AUTHOR_START_TAG)) {
					authorCount++;
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
			
		} catch (IOException e) {
			// TODO
		}
		
		return null;
	}
	/*
	private static String getAuthor(String authorOrg) {
		String author = authorOrg;
		if(authorOrg.contains(StringPool.COMMA)) {
			int indexOfComma = authorOrg.indexOf(StringPool.COMMA);
			author = authorOrg.substring(0, indexOfComma);
			author = author.trim();
		}
		return author;
	}
	
	private static String getOrg(String authorOrg) {
		String org = StringPool.BLANK;
		if(authorOrg.contains(StringPool.COMMA)) {
			int indexOfComma = authorOrg.indexOf(StringPool.COMMA);
			org = authorOrg.substring(indexOfComma + 1);
			org = org.trim();
		}
		return org;
	}
	*/
	
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
	 */
	private static String[] getPlaceDateContent(BufferedReader br) {
		String line = null;
		String placeStream = StringPool.BLANK;
		String dateStream = StringPool.BLANK;
		String place = StringPool.BLANK;
		String date = StringPool.BLANK;
		String content = StringPool.BLANK;
		String[] placeDateContent = new String[3];
		
		try {
			br.mark(BUFFER_SIZE);
			while((line = br.readLine()) != null) {
				if(line.isEmpty() || line.trim().equals(StringPool.BLANK) || line.trim().equals(StringPool.NEW_LINE)) {
					br.mark(BUFFER_SIZE);
					continue;
				}
				
				int indexOfFirstComma = -1;
				int indexOfSecondComma = -1; 
				int indexOfHyphen = -1;
				indexOfFirstComma = line.indexOf(StringPool.COMMA);
				if(indexOfFirstComma > 0) {
					placeStream = line.substring(0, indexOfFirstComma);
					placeStream = verifyPlace(placeStream);
					if(!StringPool.BLANK.equals(placeStream)) {
						// place #1 exists
						place = line.substring(0, indexOfFirstComma);
						content = line.substring(indexOfFirstComma + 1);
						indexOfSecondComma = line.indexOf(StringPool.COMMA, indexOfFirstComma + 1);
						if(indexOfSecondComma > 0) {
							placeStream = line.substring(indexOfFirstComma + 1, indexOfSecondComma);
							placeStream = verifyPlace(placeStream);
							if(!StringPool.BLANK.equals(placeStream)) {
								// place #2 exists
								place = line.substring(0, indexOfSecondComma).trim();
								content = line.substring(0, indexOfSecondComma + 1);
							} else {
								// place #2 does not exist
								indexOfSecondComma = -1;
							}
							
						} 
					} else {	
						// no place and date present
						indexOfFirstComma = -1;
						content = line;
					}
				} else {
					// no place and date present
					content = line;
				}
				
				// TODO handle case where date exists but place does not
				if(!StringPool.BLANK.equals(place)) {
					// place exists. check for date.
					int indexForDate = (indexOfSecondComma != -1) ? indexOfSecondComma : indexOfFirstComma;
					indexOfHyphen = line.indexOf(StringPool.HYPHEN, indexForDate);
					if(indexOfHyphen != -1) {
						dateStream = line.substring(indexForDate + 1, indexOfHyphen);
						date = verifyDate(dateStream);
						if(!StringPool.BLANK.equals(date)) {
							// date exists
							content = line.substring(indexOfHyphen + 1);
						}
					}
				}				
				
				placeDateContent[0] = place;
				placeDateContent[1] = date;
				placeDateContent[2] = content;
				return placeDateContent;
			}
		} catch (IOException e) {
			// TODO
			System.err.println(e.getMessage());
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		
		return null;
	}
	
	
	private static String[] newGetPlaceDateContent(BufferedReader br) {
		String line = null;
		String place = StringPool.BLANK;
		String date = StringPool.BLANK;
		String content = StringPool.BLANK;
		String dateAndPlace = null;
		String[] placeDateContent = new String[3];
		
		try {
			br.mark(1000);
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
		} catch (IOException e) {
			// TODO
			System.out.println(e.getMessage());
		} catch (Exception e) {
			// TODO
			System.out.println(e.getMessage());
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
				// TODO
				//System.err.println("NUMBERFORMATEXCEPTION:\n" + e.getMessage());
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
	
	// remove these:
	public static int authorCount = 0;
	public static int titleCount = 0;
	public static int dateCount = 0;
	public static int placeCount = 0;
	
	static Writer writer = null;
	static {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/IR/testIndex/places"), "utf-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
