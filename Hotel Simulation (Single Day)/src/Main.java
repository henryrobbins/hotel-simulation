
public class Main {

	public static void main(String[] args) {

		Simulation sim= new Simulation("sim1Rooms.csv", "sim1Guests.csv");

		sim.assignLinearly();
		sim.reset();
		sim.assignLexicographically();

	}

}
