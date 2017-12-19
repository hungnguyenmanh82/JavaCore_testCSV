package csv;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
* Class CSVReader 
* This class provides function for reading and standardlize CSV file
* @version 
* @author Hai Nguyen Hoang
*/

public class CSVReader {

	/**
	 * Default charset "Shift-JIS"
	 */
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	/**
	 * Character Double quote '"'
	 */
	private static final char CHAR_QUOTE = '"';
	
	/**
	 * Character comma ','
	 */
	private static final char CHAR_SEPARATOR = ',';
	
	/**
	 * Charater Enter '\n'
	 */
	private static final char CHAR_LINE_FEED = '\n';
	
	/**
	 * for reading file
	 */
	private BufferedReader reader;
	
	/**
	 * charset of CSV file
	 */
	private String charSet;
	/**
	 * Constructs CSVReader with the specified character encoding
	 * 
	 * @param filename:
	 *            the filename of input CSV source
	 * @param charsetName:
	 *            the name of supported charset. Default is Shift-JIS
	 * @throws FileNotFoundException
	 */
	public CSVReader(String filename, String charSetName)
			throws FileNotFoundException,UnsupportedEncodingException  {
		this.charSet = charSetName;
		this.reader = new BufferedReader(new InputStreamReader(	new FileInputStream(filename),this.charSet));
	}

	public CSVReader(String filename) throws FileNotFoundException, 
										UnsupportedEncodingException {
		this(filename, DEFAULT_CHARSET);
	}
	
	/**
	 * Check if the quote is the first quote of cell
	 * Condition of first quote:
	 *   . the first character or follows a SEPARATOR
	 *   . the number of following quotes (if exist) must be even (do not include the first quote)
	 * 
	 * @return true if yes
	 *
	 * @throws IOException 
	 */
	private boolean isFirstQuote(int pos, String str, int nextQuotesCnt){
		// if quote is the first char or follows SEPARATOR
		 if ((pos == 0)||pos > 0 && (str.charAt(pos-1) == CHAR_SEPARATOR)) {
			// the number of following quotes is even
			if (nextQuotesCnt % 2 == 0) { 
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Check if the quote is the last quote of cell
	 * 
	 * @return true if yes
	 * Condition of lasst quote:
	 *   . followed by a SEPARATOR or line-feed
	 *   . the number of preceded quotes (if exist) must be even
	 *
	 * @throws IOException 
	 */
	private boolean isLastQuote(int pos, String str){
		// if quote is followed by SEPARATOR or line-feed
		if (((str.length() > pos + 1)&&(str.charAt(pos+1) == CHAR_SEPARATOR))
			|| (pos + 1 == str.length())) {
			int j = pos - 1;
			int count = 0;
			while ((j > 0) && (str.charAt(j) == CHAR_QUOTE)) {
				j--;
				count++;
			}

			// the number of preceded quotes is even (these quotes are used as normal char before lastquote)
			// or count == -1 (a comma used as normal char before last quote)
			if (count % 2 == 0) { 
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Convert a string to CSV format.
	 * 
	 * @param strCell  content of a cell need to convert to CSV format
	 * @return a string converted to CSV format
	 */
	
	private String normalizeCell(String strCell){
		StringBuffer strBuf = new StringBuffer();
		boolean checkSpecialChar = false;
		int i;
		char c;
		if(strCell == null){
			return "";
		}
		int size = strCell.length();
		for(i = 0; i < size; i++){
			c = strCell.charAt(i);
			if( c == CHAR_QUOTE){
				strBuf.append(CHAR_QUOTE);
				strBuf.append(c);
				checkSpecialChar = true;
			}
			else if(c == CHAR_SEPARATOR || c == CHAR_LINE_FEED){
				strBuf.append(c);
				checkSpecialChar = true;
			}
			else{
				strBuf.append(c);
			}
		}
		if(checkSpecialChar){
			strBuf.insert(0,CHAR_QUOTE);
			strBuf.insert(strBuf.length(),CHAR_QUOTE);		
		}
			
		return strBuf.toString();
	}

	/**
	 * Read the next row and convert to a string array
	 * 
	 * @return A string array of which each element is separated by comma. Null
	 *         if the reader reaches EOF
	 * @throws IOException 
	 */
	
	public List<String> getNextRow() throws IOException {
		StringBuffer cell = new StringBuffer();
		List<String> cellArray = new ArrayList<String>();
		boolean isRow = false;
		boolean inCell = false;
		boolean cellInQuote = false;
		int size;
		
		 while (!isRow) {
			String st = "";
			//Hungnm: đoạn này là copy memory => ko tối ưu bộ nhớ
			st = this.reader.readLine();
			if (st == null) return null;
			size = st.length();
			
			for (int i = 0; i < size; i++ ) { 
				char c = st.charAt(i);

				// add c to cell except the SEPARATOR
				if(c != CHAR_SEPARATOR || inCell){
					cell.append(c);
				}

				// if CHAR_QUOTE, search for first quote or last quote of cell
				if (c == CHAR_QUOTE){
					if (!inCell){
						int j = i + 1;
						String quoteStr = "";
						int count = 0;
						// Get all the following quotes
						while ((st.length() > j)&&(st.charAt(j) == CHAR_QUOTE)) {
							quoteStr += st.charAt(j);
							j++;
							count++;
						}

						/* check if the first quote */
						if(isFirstQuote(i, st, count)){
							inCell = true;
						}
						else{
							// the cell has only quotes and enclosed in double quotation
							if((st.length() > j) && (st.charAt(j) == CHAR_SEPARATOR) || (j == st.length())){
								cellInQuote = true;
							}
						}

						// Append the following quotes
						if(quoteStr.length() > 0){
							cell.append(quoteStr);
							i += count; // inclement to pass all the following quotes
						}
					}
					else {
						/* check if the last quote */
						if(isLastQuote(i, st)){
							inCell = false;
							cellInQuote = true;
						}
					}
				}

				// continue to search the last quote
				if(inCell){
					continue;
				}

				// if comma or last char (before line-feed), then 1 cell completed
				// add the cell to cell array
				if (c == CHAR_SEPARATOR || (i+1 == st.length())) {
					// if current cell is not enclosed in double quotation " ",
					// modify cell if special character exists
					if(!cellInQuote){
						cellArray.add(normalizeCell(cell.toString()));
					}
					else{
						cellArray.add(cell.toString());
						cellInQuote = false;
					}
					cell = new StringBuffer();
				}
			}
			/* check if it's necessary to read next line for this cell*/
			if (!inCell) {
				isRow = true;
			}
			else {
				if (!reader.ready()) { //if EOF
					cellArray.add(normalizeCell(cell.toString()));
					cell = new StringBuffer();
					isRow = true;
				}
				else {
					isRow = false;
					cell.append(CHAR_LINE_FEED);
				}
			}
		}		 
		return cellArray;
	}
	
	/**
	 * Closes the underlying reader.
	 * @throws IOException 
	 */
	public void close() throws IOException {
		reader.close();
	}
}
