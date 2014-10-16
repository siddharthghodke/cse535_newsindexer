package edu.buffalo.cse.irf14.dictionary;

import java.util.HashMap;
import java.util.Map;

public class TermDictionary {
	
	private static Map<String, Integer> termDictionary;
	private static Map<Integer, String> reverseTermDictionary;
	private static Integer termId;
	
	static {
		termDictionary = new HashMap<String, Integer>();
		reverseTermDictionary = new HashMap<Integer, String>();
		termId = -1;
	}

	public static Integer addTerm(String key) {
		termDictionary.put(key, ++termId);
		reverseTermDictionary.put(termId, key);
		return termId;
	}
	
	public static Map<String, Integer> getDictionary() {
		return termDictionary;
	}
	
	public static Map<Integer, String> getReverseDictionary() {
		return reverseTermDictionary;
	}

	public static Integer getTermId(String key) {
		return termDictionary.get(key);
	}
	
	public static String getTerm(int termId) {
		return reverseTermDictionary.get(termId);
	}
	
	public static int size() {
		return termDictionary.size();
	}
	
	public static void setReverseDictionary(Map<Integer, String> reverseDictionary) {
		reverseTermDictionary = reverseDictionary;
	}
	
	public static void setDictionary(Map<String, Integer> dictionary) {
		termDictionary = dictionary;
	}
}
