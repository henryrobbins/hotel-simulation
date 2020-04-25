package com.henryrobbins.decision;

import java.util.ArrayList;
import java.util.Arrays;

/** A statistic for a given type of Result */
public interface Statistic<T extends Decision> {

	/** Get the statistic */
	Double getStat(T t);

	ArrayList<Statistic<Assignment>> ASSIGNMENT_STATS= new ArrayList<>(Arrays.asList(
		new MaxSatisfaction(),
		new MinSatisfaction(),
		new PercentBelowTau(0.8),
		new MeanSatisfaction(),
		new MaxUpgrade(),
		new MinUpgrade(),
		new MeanUpgrade(),
		new SumUpgrade()));

	public class MaxSatisfaction implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.satisfactionStats().getMax();
		}

		@Override
		public String toString() {
			return "Max Satisfaction";
		}
	}

	public class MinSatisfaction implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.satisfactionStats().getMin();
		}

		@Override
		public String toString() {
			return "Min Satisfaction";
		}
	}

	public class PercentBelowTau implements Statistic<Assignment> {

		private double tau;

		public PercentBelowTau(double tau) {
			this.tau= tau;
		}

		@Override
		public Double getStat(Assignment assignment) {
			int belowCount= 0;
			double[] sortedSats= assignment.satisfactionStats().getSortedValues();
			int numOfArrivals= sortedSats.length;
			int i= 0;
			while (i < numOfArrivals && sortedSats[i] < tau) {
				belowCount++ ;
				i++ ;
			}
			return (double) belowCount / (double) numOfArrivals;
		}

		@Override
		public String toString() {
			return "Percent Below " + tau;
		}
	}

	public class MeanSatisfaction implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.satisfactionStats().getMean();
		}

		@Override
		public String toString() {
			return "Mean Satisfaction";
		}
	}

	public class MaxUpgrade implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.upgradeStats().getMax();
		}

		@Override
		public String toString() {
			return "Max Upgrade";
		}
	}

	public class MinUpgrade implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.upgradeStats().getMin();
		}

		@Override
		public String toString() {
			return "Min Upgrade";
		}
	}

	public class MeanUpgrade implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.upgradeStats().getMean();
		}

		@Override
		public String toString() {
			return "Mean Upgrade";
		}
	}

	public class SumUpgrade implements Statistic<Assignment> {
		@Override
		public Double getStat(Assignment assignment) {
			return assignment.upgradeStats().getSum();
		}

		@Override
		public String toString() {
			return "Sum Upgrade";
		}
	}

	ArrayList<Statistic<Schedule>> SCHEDULE_STATS= new ArrayList<>(Arrays.asList(
		new Makespan(),
		new MinCompletionTime(),
		new MeanCompletionTime(),
		new SumCompletionTime()));

	public class Makespan implements Statistic<Schedule> {
		@Override
		public Double getStat(Schedule schedule) {
			return (double) schedule.makespan();
		}

		@Override
		public String toString() {
			return "Makespan";
		}
	}

	public class MinCompletionTime implements Statistic<Schedule> {
		@Override
		public Double getStat(Schedule schedule) {
			return schedule.completionStats().getMin();
		}

		@Override
		public String toString() {
			return "Min Completion Time";
		}
	}

	public class MeanCompletionTime implements Statistic<Schedule> {
		@Override
		public Double getStat(Schedule schedule) {
			return schedule.completionStats().getMean();
		}

		@Override
		public String toString() {
			return "Mean Completion Time";
		}
	}

	public class SumCompletionTime implements Statistic<Schedule> {
		@Override
		public Double getStat(Schedule schedule) {
			return schedule.completionStats().getSum();
		}

		@Override
		public String toString() {
			return "Sum Completion Time";
		}
	}

	ArrayList<Statistic<Solution>> SOLUTION_STATS= new ArrayList<>(Arrays.asList(
		new MaxLateness(),
		new MinLateness(),
		new MeanLateness(),
		new MaxTardiness(),
		new MinTardiness(),
		new MeanTardiness(),
		new SumTardiness()));

	public class MaxLateness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.latenessStats().getMax();
		}

		@Override
		public String toString() {
			return "Max Lateness";
		}
	}

	public class MinLateness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.latenessStats().getMin();
		}

		@Override
		public String toString() {
			return "Min Lateness";
		}
	}

	public class MeanLateness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.latenessStats().getMean();
		}

		@Override
		public String toString() {
			return "Mean Lateness";
		}
	}

	public class MaxTardiness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.tardinessStats().getMax();
		}

		@Override
		public String toString() {
			return "Max Tardiness";
		}
	}

	public class MinTardiness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.tardinessStats().getMin();
		}

		@Override
		public String toString() {
			return "Min Tardiness";
		}
	}

	public class MeanTardiness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.tardinessStats().getMean();
		}

		@Override
		public String toString() {
			return "Mean Tardiness";
		}
	}

	public class SumTardiness implements Statistic<Solution> {
		@Override
		public Double getStat(Solution solution) {
			return (double) solution.tardinessStats().getSum();
		}

		@Override
		public String toString() {
			return "Sum Tardiness";
		}
	}
}
