package com.henryrobbins.solver;

import java.util.ArrayList;
import java.util.Arrays;

import com.henryrobbins.decision.Assignment;
import com.henryrobbins.decision.Decision;
import com.henryrobbins.decision.Schedule;
import com.henryrobbins.decision.Solution;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.solver.assignment.AssignmentIPSolver;
import com.henryrobbins.solver.assignment.BestFirst;
import com.henryrobbins.solver.assignment.Linear;
import com.henryrobbins.solver.assignment.MaxMeanSatSTMinIP;
import com.henryrobbins.solver.assignment.MinUpgradesSTSatIP;
import com.henryrobbins.solver.assignment.WorstFirst;
import com.henryrobbins.solver.schedule.FirstAvailable;
import com.henryrobbins.solver.schedule.NeededFirst;
import com.henryrobbins.solver.schedule.NeededFirstNoWait;
import com.henryrobbins.solver.schedule.ScheduleIPSolver;
import com.henryrobbins.solver.solution.SolutionIPSolver;

public interface Solver<T extends Decision> {

	// T will only be of type Assignment, Schedule, or Solution
	T solve(Instance instance);

	ArrayList<Solver<Assignment>> ASSIGNMENT_SOLVERS= new ArrayList<>(Arrays.asList(
		new Linear(),
		new BestFirst(),
		new WorstFirst(),
		new AssignmentIPSolver("Mean_Satisfaction"),
		new AssignmentIPSolver("Min_Satisfaction"),
		new AssignmentIPSolver("Satisfaction"),
		new AssignmentIPSolver("Upgrades"),
		new AssignmentIPSolver("Feasible"),
		new MaxMeanSatSTMinIP(),
		new MinUpgradesSTSatIP(1, 1)));

	ArrayList<Solver<Schedule>> SCHEDULE_SOLVERS= new ArrayList<>(Arrays.asList(
		new FirstAvailable(),
		new NeededFirst(),
		new NeededFirstNoWait(),
		new ScheduleIPSolver("Makespan"),
		new ScheduleIPSolver("Sum_Completion_Time"),
		new ScheduleIPSolver("Sum_Tardiness"),
		new ScheduleIPSolver("Max_Tardiness")));

	ArrayList<Solver<Solution>> SOLUTION_SOLVERS= new ArrayList<>(Arrays.asList(
		new SolutionIPSolver("Sum_Tardiness"),
		new SolutionIPSolver("Max_Tardiness"),
		new SolutionIPSolver("Mean_Satisfaction"),
		new SolutionIPSolver("Mean_Satisfaction_And_Sum_Tardiness")));

}
