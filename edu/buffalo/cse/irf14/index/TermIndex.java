package edu.buffalo.cse.irf14.index;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.dictionary.DocumentDictionary;
import edu.buffalo.cse.irf14.dictionary.TermDictionary;
import edu.buffalo.cse.irf14.document.PostingsList;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

public class TermIndex implements Index {
	
	private Map<Integer, PostingsList> index;
	private static final String TYPE = IndexType.TERM.toString();
	
	public TermIndex() {
		index = new HashMap<Integer, PostingsList>();
	}

	public void add(String token, int docId, int position) {
		PostingsList ps;
		Integer termId = TermDictionary.getTermId(token);
		if(termId == null) {
			termId = TermDictionary.addTerm(token);
			ps = new PostingsList();
			ps.insert(docId, position);
			index.put(termId, ps);
		} else {
			index.get(termId).insert(docId, position);
		}
	}
	
	public void writeIndexToDisk(String rootIndexDir) throws Exception{
		FileOutputStream fileOutStream;
		ObjectOutputStream objectOutStream;
		BufferedOutputStream bufferOutStream;
		String fileName = rootIndexDir + File.separator + TYPE.toLowerCase() + File.separator + TYPE.toLowerCase();
		
		// write the term index
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(index);
		objectOutStream.close();
		fileOutStream.close();
		
		// write document dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.DOCUMENT;
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(DocumentDictionary.getDictionary());
		objectOutStream.close();
		fileOutStream.close();
		
		// write reverse document dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.REVERSE + StringPool.UNDERSCORE + Constants.DOCUMENT;
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(DocumentDictionary.getReverseDictionary());
		objectOutStream.close();
		fileOutStream.close();
		
		// write term dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(TermDictionary.getDictionary());
		objectOutStream.close();
		fileOutStream.close();
		
		// write term dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.REVERSE + StringPool.UNDERSCORE + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(TermDictionary.getReverseDictionary());
		objectOutStream.close();
		fileOutStream.close();
	}
}
