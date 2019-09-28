import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/** Contains the method to read a Guest CSV file */
public class GuestCSVReader {

	/** Returns the ArrayList of Guests associated with the CSV file at the given <br>
	 * directory represented by the String argument <br>
	 * Precondition: The CSV is of the following format: <br>
	 * unique ID, unique arrival position, preferences <br>
	 * EX: 1,1,A:B:C */
	static public ArrayList<Guest> read(String guestsCSV) {

		ArrayList<Guest> guests= new ArrayList<>();

		BufferedReader br= null;
		try {
			br= new BufferedReader(new FileReader(guestsCSV));

			// Essentially skips first line (HEADER)
			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				// Convert each row to an array of Strings
				String[] values= contentLine.split(",");

				// Get guest ID
				int id= Integer.parseInt(values[0]);

				// Get guest arrival position
				int position= Integer.parseInt(values[1]);

				// Get guest preferences
				ArrayList<String> preferences= new ArrayList<>();
				if (values.length > 2) {
					String[] prefs= values[2].split(":");
					for (int i= 0; i < prefs.length; i++ ) {
						preferences.add(prefs[i]);
					}
				}

				// Add guest to the list of guests
				guests.add(new Guest(id, position, preferences));
				contentLine= br.readLine();
			}
		} catch (

		IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				System.out.println("Error in closing the BufferedReader");
			}
		}
		return guests;
	}

}
