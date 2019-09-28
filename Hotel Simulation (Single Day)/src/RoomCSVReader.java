import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/** Contains the method to read a Room CSV file */
public class RoomCSVReader {

	/** Returns the ArrayList of Rooms associated with the CSV file at the given <br>
	 * directory represented by the String argument <br>
	 * Precondition: The CSV is of the following format: <br>
	 * unique room number, attributes <br>
	 * EX: 1,A:B:C */
	static public ArrayList<Room> read(String roomsCSV) {

		ArrayList<Room> rooms= new ArrayList<>();

		BufferedReader br= null;
		try {
			br= new BufferedReader(new FileReader(roomsCSV));

			// Essentially skips first line (HEADER)
			String contentLine= br.readLine();
			contentLine= br.readLine();
			while (contentLine != null) {
				// Convert each row to an array of Strings
				String[] values= contentLine.split(",");

				// Get room number
				int num= Integer.parseInt(values[0]);

				// Get room attributes
				ArrayList<String> attributes= new ArrayList<>();
				if (values.length > 1) {
					String[] attr= values[1].split(":");
					for (int i= 0; i < attr.length; i++ ) {
						attributes.add(attr[i]);
					}
				}

				// Add guest to the list of guests
				rooms.add(new Room(num, attributes));
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
		return rooms;
	}

}
