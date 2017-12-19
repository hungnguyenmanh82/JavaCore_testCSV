package csv;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.String;

/*
 * Created on Sep 15, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author HungNM
 * This Class use to write data to CSV file.
 * + cần đọc chuẩn CSV
 * + khi 1 cell chứa ký tự đặc biệt như "\n" và "," thì cell đó phải ở trong double quote
 * 
 */
public class CSVWriter {
	/**
	 * Default charset "Shift-JIS"
	 */
	public static final String DEFAULT_CHARSET = "UTF-8";
	/**
	 * Charater Enter '\n'
	 */
	public static final char CHAR_LINE_FEED = '\n';
	/**
	 * Character Double quote '"'
	 */
	public static final char CHAR_QUOTE = '"';
	/**
	 * Character comma ','
	 */
	public static final char CHAR_SEPARATOR = ',';
	
	public static final char CHAR_CR = '\r';
	/**
	 * Convert one character Quote into a string including 2 Quote = "\"\"" 
	 */
	public static final String STR_TWO_QUOTE = "\"\"";
	public static final String STR_LINE_BREAK = "\r\n";
	public static final String STR_LINE_FEED = "\n";
	
	private String charSet;
	
	private File file;
	
	private OutputStreamWriter writer;
	/**
	 * 
	 * @param filename 	the filename of output CSV source
	 * @param charsetName the name of supported charset. Default is "Shift-JIS"
	 */
	public CSVWriter(String filename)throws IOException{
		this(filename,DEFAULT_CHARSET);
	}

	/**
	 * 
	 * @param filename 	the filename of output CSV source
	 * @param charsetName the name of supported charset.
	 */
	public CSVWriter(String filename, String charsetName)throws IOException{
		//check file before processing
		file = new File(filename);
		
		FileOutputStream outputStream = new FileOutputStream(file,true);
		writer = new OutputStreamWriter(outputStream,charsetName);
	}
	
	/**
	 * Writes all contents to a CSV file.
	 *
	 * @param  buffer   a list of string arrays 
	 * which includes all the contents of a CSV file
	 *
	 */
	public void appendRows(List<List<String>> listRow) throws IOException{
		if(listRow == null){
			return;
		}
		
        for (List<String> nextRow: listRow) {
            appendRow(nextRow);     
        }
	}

	/**
	 * Append a row to a CSV file
	 *
	 * @param  cells   a string array which includes all the cell of a row. 
	 * Each cell is an entry of array.
	 *
	 */
	public void appendRow(List<String> cells) throws IOException{
		
		if(cells == null || cells.size() == 0){
			writer.write(CHAR_LINE_FEED);
			return;
		}
		StringBuilder strRow = new StringBuilder();
		int i;

		for( i = 0; i < cells.size() - 1; i++ ){
			strRow.append(processCell(cells.get(i)));
			strRow.append(CHAR_SEPARATOR);
		}
		strRow.append(processCell(cells.get(cells.size()-1)));	
		writer.write(strRow.toString());
		writer.write(CHAR_LINE_FEED);
	}

	/**
	 * This function check whether a cell contain special characters 
	 * from the second charactor to the end of strCell.
	 * @param strCell input a string of cell 
	 * @return true if cell contain special characters. 
	 * return false if cell has no special character.  
	 */
	private boolean checkSpecialChar(String strCell){
		
		if( strCell == null || strCell.length() == 0){
			return false;
		}
		int i, len;
		len = strCell.length();
		for(i = 1; i < len; i++ ){
			if(strCell.charAt(i) == CHAR_QUOTE ||
					strCell.charAt(i) == CHAR_SEPARATOR||			
					strCell.charAt(i) == CHAR_LINE_FEED){
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * Check whether Cell is standard CSV format.
	 * a cell is a standard CSV format if it has first quote and last quote.
	 * if cell is not standard CSV format, convert it to standard CSV format. 
	 * 
	 * @param strCell input
	 * @return  = String was converted to CSV standard format.
	 */
	public String processCell(String strCell){//
		if( strCell == null || strCell.length() == 0){
			return "";
		}

		StringBuilder cellBuff = new StringBuilder();
		String str;
		
		str = replaceToLineFeed(strCell);
		
		if( isCellInQuote(str)){
			cellBuff.append(CHAR_QUOTE);
			cellBuff.append(fixOddQuotes(str,1,str.length()-1));
			cellBuff.append(CHAR_QUOTE);
			return cellBuff.toString();
		}
		return normalizeCell(str);
	}

	/**
	 * This function will convert a quote in the sequence of odd quotes into 2 quote.
	 * @param str input a string.
	 * @param idexBegin  the position start checking.
	 * @param indexEnd   the postion end the check.
	 * @return
	 */
	private String fixOddQuotes(String str,int idexBegin, int indexEnd){
		StringBuilder strBuf = new StringBuilder();
		char c;
		
		for( int i = idexBegin; i < indexEnd ; i++){
			c = str.charAt(i);
			if(c == CHAR_QUOTE){
				i++;
				if( i >= indexEnd ){
					strBuf.append(STR_TWO_QUOTE);
					break;
				}
				if(str.charAt(i) == CHAR_QUOTE){
					strBuf.append(STR_TWO_QUOTE);
				}else{
					strBuf.append(STR_TWO_QUOTE);
					strBuf.append(str.charAt(i));
				}				
			}else{
				strBuf.append(c);
			}						
		}
		return strBuf.toString();
	}

	/**
	 * This function replace "\r\n" -> "\n" and "\r" ->"\n" if \r stand alone in 
	 * the str String.
	 * @param str  input:  a tring
	 * @return a string after replace.
	 */
	public static String replaceToLineFeed(String str){
		
		if(str == null) return null;
		String str1,str2;
		//Hungnm: it is a copy of memory => it consume memory
		str1 = str.replaceAll(STR_LINE_BREAK,STR_LINE_FEED);
		str2 = str1.replace(CHAR_CR,CHAR_LINE_FEED);
		return str2;
	}

	/**
	 * Check if the cell is enclosed in double quotation (assume that the SEPARATOR of cell is not included)
	 * @param strCell
	 * @return
	 *    true : if cell is enclosed in double quotation " "
	 *    false: else
	 */
	private boolean isCellInQuote(String strCell){
		int len;
		int i;
		if(strCell == null || strCell.length() == 0){
			return false;
		}

		len = strCell.length();
		int checkFirstQuote = 0;
		// Check first quote
		i = 0;
		while(i < len && strCell.charAt(i++) == CHAR_QUOTE){
			checkFirstQuote++;
		}

		if(checkFirstQuote % 2 == 0){
			// the cell has only quotes and enclosed in double quotation
			if(i == len){
				return true;
			}
			return false;
		}

		// check last quote
		i = len - 1;
		int checkLastQuote = 0;
		while(i > 0 && i > checkFirstQuote && strCell.charAt(i--) == CHAR_QUOTE){
			checkLastQuote++;
		}
		
		if(checkLastQuote  % 2 == 0){
			return false;
		}
		return true;
	}

	
	/**
	 * Check if the current quote is the first quote of cell. Assume that the string (str) is a row.
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
	 * Check if the quote is the last quote of cell. Assume that the string (str) is a row.
	 * 
	 * @return true if yes
	 * Condition of lasst quote:
	 *   . followed by a SEPARATOR or line-feed
	 *   . the number of preceded quotes (if exist) must be even
	 *
	 * @throws IOException 
	 */
	private boolean isLastQuote(int pos, String str){
		// if quote is followed by SEPARATOR or line-feed (only last cell)
		if ( ( (str.length() > pos + 1) && (str.charAt(pos+1) == CHAR_SEPARATOR) )
			|| ( (pos + 1 == str.length() -1) && (str.charAt(pos+1) == CHAR_LINE_FEED) ) //line-feed at the end of row
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
	 * @return a string  is converted to CSV format
	 */
	public static String normalizeCell(String strCell){
		StringBuilder strBuf = new StringBuilder();
		boolean checkSpecialChar = false;
		int i;
		char c;

		if(strCell == null || strCell.length() == 0){
			return "";
		}

		int size = strCell.length();
		for(i = 0; i < size; i++){
			c = strCell.charAt(i);
			if( c == CHAR_QUOTE){
				strBuf.append(STR_TWO_QUOTE);
				checkSpecialChar = true;
			}else if(c == CHAR_SEPARATOR || 
						c == CHAR_LINE_FEED){
				strBuf.append(c);
				checkSpecialChar = true;
				}else{
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
 	 * Append a row to a CSV file
 	 *
	 * @param  row   a string which includes all the cells of the row. 
	 * Each cell is separated by comma.
	 *
	 */
	public void appendRow(String row) throws IOException{
		
		appendRow(parseRow(row));

	  }	
	/**
	 *This function parses a row into a list of cells
	 */
	private List<String> parseRow(String row){
		StringBuilder cell = new StringBuilder();
		List<String> cellArray = new ArrayList<String>();
		boolean inCell = false;
		boolean cellInQuote = false;
		int size;

		// Replace the CHAR_CR to line-feed
		row = replaceToLineFeed(row);

		size = row.length();
		for (int i = 0; i <size; i++){
			char c = row.charAt(i);
			// add c to cell except the SEPARATOR
			if( (c != CHAR_SEPARATOR && c != CHAR_LINE_FEED) || inCell){
				cell.append(c);
			}

			// if CHAR_QUOTE, search for first quote or last quote of cell
			if (c == CHAR_QUOTE){
				if (!inCell){
					int j = i + 1;
					String quoteStr = "";
					int count = 0;
					// Get all the following quotes
					while ((row.length() > j)&&(row.charAt(j) == CHAR_QUOTE)) {
						quoteStr += row.charAt(j);
						j++;
						count++;
					}

					/* check if the first quote */
					if(isFirstQuote(i, row, count)){
						inCell = true;
					}
					else{
						// the cell has only quotes and enclosed in double quotation
						if( ( (row.length() > j) && (row.charAt(j) == CHAR_SEPARATOR) )
							|| ( (j == row.length() - 1) && (row.charAt(j) == CHAR_LINE_FEED) ) //line-feed at the end of row
						 	|| (j == row.length()) ){
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
					if(isLastQuote(i, row)){
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
			if (c == CHAR_SEPARATOR || (i+1 == row.length()) || c == CHAR_LINE_FEED) {
				// if current cell is not enclosed in double quotation " ",
				// modify cell if special character exists
				if(!cellInQuote){
					cellArray.add(normalizeCell(cell.toString()));
				}
				else{
					cellArray.add(cell.toString());
					cellInQuote = false;
				}
				cell = new StringBuilder();
			}
		}
		//have "first quote" and the end of file without "last quote"
		if( inCell == true){
			cellArray.add(normalizeCell(cell.toString()));
		}
		
		return cellArray;
	}
	/**
	 * Closes the underlying writer.
	 * 
	 * @throws java.io.Exception
	 */
	public void close() throws IOException{
		writer.close();	
	}
}
