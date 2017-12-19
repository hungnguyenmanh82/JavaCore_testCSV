package apache;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/*
 * Thư viện apache ko hay lam
 * */
public class ApacheCsv {
	public static void main(String[] args) throws Exception{
		readCsvFile();
			
	}
	
	private static void readCsvFile() throws Exception{
		//file tại root folder của eclipse
		FileInputStream inputStream= new FileInputStream("test.csv"); //sequence byte of file no buffer
		InputStreamReader reader = new InputStreamReader(inputStream,"UTF-8"); 	

		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(reader);
		StringBuilder st = new StringBuilder();	
		
		for (CSVRecord record : records) {
			for(int i = 0; i < record.size(); i++){
				st.append(record.get(i));
				st.append(",");
			}
			st.append("\n");
		}
		
		System.out.print(st.toString());
		   
	}

}
