package csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) throws Exception{
		readCsvFile();
		
		writeCsvFile();

	}
	
	public static void readCsvFile() throws IOException{
		CSVReader reader = new CSVReader("test.csv","UTF-8");
		List<String> cells = reader.getNextRow();
		while(cells != null){
			for(String cell: cells){
				System.out.print(cell+",");
			}
			System.out.println("");
			cells = reader.getNextRow();
		}
		
		reader.close();
	}
	
	public static void writeCsvFile() throws IOException{
		List<String> cells = new ArrayList<String>();
		cells.add(",col,umn1");
		cells.add("colum\"n2");
		cells.add("colu \n mn3");
		
		CSVWriter csvWriter = new CSVWriter("output.csv", "UTF-8");
		csvWriter.appendRow(cells);
		csvWriter.appendRow(cells);
		csvWriter.close();
		
	} 
}
