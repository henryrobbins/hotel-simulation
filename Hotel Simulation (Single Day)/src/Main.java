public class Main {

	public static void main(String[] args) {

		// int[] roomNumbers= { 10, 100, 200, 300, 400, 500, 750, 1000 };

		// Test test= new Test(roomNumbers, 50, "/Users/Henry/Downloads");

		Simulation sim= new Simulation("test3");

		System.out.println(sim.assignLinearly());
		sim.printStats();
		sim.reset();
		System.out.println(sim.assignLexicographically());
		sim.printStats();
		sim.reset();
		System.out.println(sim.assignIP());
		sim.printStats();

	}

}
