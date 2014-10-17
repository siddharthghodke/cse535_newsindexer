package edu.buffalo.cse.irf14.query;

/**
 * Class that represents a parsed query
 * @author nikhillo
 *
 */
public class Query {
	
	private String query;
	private String defOp;
	private String parsedQuery;
	
	
	public Query(String query, String defOp) {
		this.query = query;
		this.defOp = defOp;
	}
	
	/**
	 * Method to convert given parsed query into string
	 */
	public String toString() {
		parsedQuery = new QueryBuilder(query, defOp).buildQuery();
		return "{ " + parsedQuery + " }";
	}
	
	public String getParsedQuery() {
		return parsedQuery;
	}
}
