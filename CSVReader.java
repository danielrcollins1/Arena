import java.io.*; 
import java.util.*;
import java.nio.charset.Charset;

/******************************************************************************
*  CSV-file reader help functions.
*  See: wikipedia.org/wiki/Comma-separated_values#Toward_standardization
*
*  @author   Daniel R. Collins (dcollins@superdan.net)
*  @since    2014-07-16
******************************************************************************/

public class CSVReader {

	/** Standard delimiter for a CSV file (comma). */
	static private char COMMA = ',';

	/** Quotes used for special field containers. */
	static private char QUOTE = '\"';

	/**
	*  Read in a text file.
	*  Assumes default charset.
	*  @param filename File to read.
	*  @return Array of split string arrays.
	*/
	public static String[][] readFile (String filename) throws IOException {
		return readFile(filename, Charset.defaultCharset().name());
	}

	/**
	*  Read in a text file.
	*  @param filename File to read.
	*  @param charset Name of encoding type.
	*  @return Array of split string arrays.
	*/
	public static String[][] readFile (String filename, String charset) throws IOException {
		File file = new File(filename);
		Scanner scan = new Scanner(file, charset);
		List<String[]> lines = new ArrayList<String[]>();
		while (scan.hasNextLine()) {
			lines.add(splitLine(scan.nextLine()));
		}
		scan.close();
		return lines.toArray(new String[0][]);
	}

	/**	
	*  Split one line with proper quote handling.
	*  @param line Line to split.
	*  @return Array of split strings.
	*/
	public static String[] splitLine (String line) {
		int ptr = 0;
		line = trimTrailingDelimit(line);
		List<String> fieldList = new ArrayList<String>();
		while (ptr < line.length()) {

			// Field is non-quoted
			if (line.charAt(ptr) != QUOTE) {
				int count = 0;
				char[] chars = new char[line.length()];
				while (ptr < line.length() && line.charAt(ptr) != COMMA) {
					chars[count++] = line.charAt(ptr++);									
				}				
				fieldList.add(new String(chars, 0, count));
				ptr++;
			}

			// Field is quoted
			else {
				ptr++;
				int count = 0;
				char[] chars = new char[line.length()];
				while (ptr < line.length()) {
				
					// Handle non-quote character
					if (line.charAt(ptr) != QUOTE) {
						chars[count++] = line.charAt(ptr++);									
					}

					// Handle quote markers
					else {

						// Double quotes become one quote
						if (ptr+1 < line.length() && line.charAt(ptr+1) == QUOTE) {
							chars[count++] = QUOTE;
							ptr += 2;													
						}					

						// Single quotes mark end of field
						else {
							fieldList.add(new String(chars, 0, count));
							while (ptr < line.length() && line.charAt(ptr) != COMMA)
								ptr++; // Eat to next comma
							ptr++;
							break;
						}
					}
				}
			}
 		}
		
		// Return list as normal array
		String[] format = new String[0];
		return fieldList.toArray(format);
	}

	/**
	*  Trim off trailing delimiters of a string.
	*  @param s String to trim.
	*  @return Trimmed String.
	*/
	public static String trimTrailingDelimit (String s) {
		int ptr = s.length()-1;
		while (ptr > -1 && s.charAt(ptr) == COMMA) {
			ptr--;
		}
		return s.substring(0, ptr+1);
	}

	/**	
	*  Split one line with no quote-handling (for testing)
	*  @param line Line to split.
	*  @return Array of split Strings.
	*/
	public static String[] splitLineNoQuotes (String line) {
		line = trimTrailingDelimit(line);
		return line.split("" + COMMA);
	}

	/**
	*  Parse string to integer, treating dash as zero.
	*  @param s String to parse.
	*  @return Parsed integer.
	*/
	public static int parseInt (String s) {
		return s.equals("-") ? 0 : Integer.parseInt(s);		
	};

	/**
	*  Make a CSV line from strings (for writing)
	*  @param list String list.
	*  @return Comma-separated string.
	*/
	public static String makeLineFromStrings (String ... list) {
		String s = "";
		for (int i = 0; i < list.length; i++) {
			s += "\"" + list[i] + "\"";
			if (i < list.length-1) {
				s += ",";
			}
		}	
		return s;
	}

	/**
	*  Main test function.
	*/
	public static void main (String[] args) {

		// Test trim trailing delimiters
		String s1 = "Light,Darkness,,,,,";
		System.out.println(trimTrailingDelimit(s1));
		String s2 = ",,,,,,";
		System.out.println(trimTrailingDelimit(s2));
		
		// Split a CSV string
		String line = "Sleep,Charm,\"Silence, 15' Radius\","
			+ "\"Giant, Hill\",\"He says \"\"boo\"\"\"";
		System.out.println(line + "\n");
		String fields[] = splitLine(line);
		for (String s: fields) {
			System.out.println(s);		
		}
	}
}

