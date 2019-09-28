
public class Main {

	public static void main(String[] args) {

		Simulation sim= new Simulation("roomTest3.csv", "guestTest3.csv");

		sim.assignLinearly();
		sim.printFullReport();

		sim.reset();

		sim.assignLexicographically();
		sim.printFullReport();

		sim.convert("data3test.dat");

	}

}
