package edu.buffalo.cse.irf14.index;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.cse.irf14.dictionary.CategoryDictionary;
import edu.buffalo.cse.irf14.document.PostingsList;
import edu.buffalo.cse.irf14.util.Constants;
import edu.buffalo.cse.irf14.util.StringPool;

public class CategoryIndex implements Index {

	private Map<Integer, PostingsList> index;
	private static final String TYPE = IndexType.CATEGORY.toString();
		
	public CategoryIndex() {
		index = new HashMap<Integer, PostingsList>();
	}
	
	@Override
	public void add(String token, int docId, int position) {
		PostingsList ps;
		Integer catId = CategoryDictionary.getCatId(token);
		if(catId == null) {
			catId = CategoryDictionary.addCategory(token);
			ps = new PostingsList();
			ps.insert(docId, position);
			index.put(catId, ps);
		} else {
			index.get(catId).insert(docId, position);
		}

	}

	@Override
	public void writeIndexToDisk(String rootIndexDir) throws Exception {
		
		// write category index
		FileOutputStream fileOutStream;
		ObjectOutputStream objectOutStream;
		BufferedOutputStream bufferOutStream;
		String fileName = rootIndexDir + File.separator + TYPE.toLowerCase() + File.separator + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(index);
		objectOutStream.close();
		fileOutStream.close();
		
		// write category dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(CategoryDictionary.getDictionary());
		objectOutStream.close();
		fileOutStream.close();		
		
		// write reverse category dictionary
		fileName = rootIndexDir + File.separator + Constants.DICTIONARY + File.separator + Constants.REVERSE + StringPool.UNDERSCORE + TYPE.toLowerCase();
		fileOutStream = new FileOutputStream(fileName);
		bufferOutStream = new BufferedOutputStream(fileOutStream);
		objectOutStream = new ObjectOutputStream(bufferOutStream);
		objectOutStream.writeObject(CategoryDictionary.getReverseCatDictionary());
		objectOutStream.close();
		fileOutStream.close();		
	}

}
