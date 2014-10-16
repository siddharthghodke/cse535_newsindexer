/**
 * 
 */
package edu.buffalo.cse.irf14;

import java.io.File;

import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexWriter;
import edu.buffalo.cse.irf14.index.IndexerException;

/**
 * @author nikhillo, sghodke, amitpuru
 *
 */
public class Runner {

	/**
	 * 
	 */
	public Runner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ipDir = args[0];
		String indexDir = args[1];
		//more? idk!
		
		File ipDirectory = new File(ipDir);
		String[] catDirectories = ipDirectory.list();
		
		String[] files;
		File dir;
		
		Document d = null;
		IndexWriter writer = new IndexWriter(indexDir);
		System.out.println("Parsing....");
		try {
			long t1 = System.currentTimeMillis();
			for (String cat : catDirectories) {
				dir = new File(ipDir+ File.separator+ cat);
				files = dir.list();
				
				if (files == null)
					continue;
				
				for (String f : files) {
					try {
						d = Parser.parse(dir.getAbsolutePath() + File.separator + f);
						writer.addDocument(d);
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				
				}
			}
			long t2 = System.currentTimeMillis();
			
			System.out.println("Time Required for parsing and in memory indexing: " + (t2-t1) + "ms");
			
			System.out.println("\nwriting index to file...");
			writer.close();
			long t3 = System.currentTimeMillis();
			System.out.println("Time Required for writing index to file: " + (t3-t2) + "ms");
			
			System.out.println("\nreading index..");
			new IndexReader(indexDir, IndexType.TERM);
			long t4 = System.currentTimeMillis();
			System.out.println("Time Required for reading index from file: " + (t4-t3) + "ms");
			
			System.out.println("\nTOTAL TIME: " + (t4-t1) + "ms");
		} catch (IndexerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
