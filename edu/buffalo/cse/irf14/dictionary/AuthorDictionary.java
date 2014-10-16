package edu.buffalo.cse.irf14.dictionary;

import java.util.HashMap;
import java.util.Map;

public class AuthorDictionary {

	private static Map<String, Integer> authorDictionary;
	private static Map<Integer, String> reverseAuthorDictionary;
	private static Integer authorId;
	
	static {
		authorDictionary = new HashMap<String, Integer>();
		reverseAuthorDictionary = new HashMap<Integer, String>();
		authorId = -1;
	}

	public static Integer addAuthor(String key) {
		authorDictionary.put(key, ++authorId);
		reverseAuthorDictionary.put(authorId, key);
		return authorId;
	}
	
	public static Map<String, Integer> getDictionary() {
		return authorDictionary;
	}

	public static String getAuthor(Integer authorId) {
		return reverseAuthorDictionary.get(authorId);
	}
	
	public static Integer getAuthorId(String key) {
		return authorDictionary.get(key);
	}
	

	public static Map<Integer, String> getReverseDictionary() {
		return reverseAuthorDictionary;
	}
	
	public static void setReverseDictionary(Map<Integer, String> reverseDictionary) {
		reverseAuthorDictionary = reverseDictionary;
	}
	
	public static void setDictionary(Map<String, Integer> dictionary) {
		authorDictionary = dictionary;
	}
	
	public static int size() {
		return authorDictionary.size();
	}
}
