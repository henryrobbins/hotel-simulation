package com.henryrobbins.decision;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.henryrobbins.hotel.Guest;
import com.henryrobbins.hotel.Instance;
import com.henryrobbins.hotel.Room;

/** Maintains both a housekeeping schedule and room assignment for a given instance. */
public class Solution implements Decision {

	/** The instance that this solution is for */
	private Instance instance;
	/** Housekeeping schedule for the relevant instance */
	private Schedule schedule;
	/** Room assignment for the relevant instance */
	private Assignment assignment;

	// MAINTAINS STATISTICS
	/** Set of tardiness' for every guest */
	private DescriptiveStatistics tardiness;
	/** Set of lateness' for every guest */
	private DescriptiveStatistics lateness;

	/** Construct a solution for the given instance consisting of the specified housekeeping <br>
	 * schedule and room assignment. Update the maintained statistics.
	 *
	 * @param instance   The instance that this solution will be for (not null)
	 * @param schedule   The housekeeping schedule for this instance (not null)
	 * @param assignment The room assignment for this instance (not null) */
	public Solution(Instance instance, Schedule schedule, Assignment assignment) {
		if (instance == null) throw new IllegalArgumentException("Instance is null");
		if (schedule == null) throw new IllegalArgumentException("Schedule was null");
		if (!schedule.isScheduleFor(instance)) throw new IllegalArgumentException("Invalid schedule");
		if (assignment == null) throw new IllegalArgumentException("Assignment was null");
		if (!assignment.isAssignmentFor(instance)) throw new IllegalArgumentException("Invalid assignment");
		this.instance= instance;
		this.schedule= schedule;
		this.assignment= assignment;
		setStats();
	}

	/** Returns a copy of the housekeeping schedule */
	public Schedule schedule() {
		return new Schedule(schedule);
	}

	/** Return a copy of the room assignment */
	public Assignment assignment() {
		return new Assignment(assignment);
	}

	/** Return the tardiness statistics */
	public DescriptiveStatistics tardinessStats() {
		return new DescriptiveStatistics(tardiness);
	}

	/** Return the lateness statistics */
	public DescriptiveStatistics latenessStats() {
		return new DescriptiveStatistics(lateness);
	}

	/** Set statistics */
	private void setStats() {
		tardiness= new DescriptiveStatistics();
		lateness= new DescriptiveStatistics();
		for (Guest guest : instance.guests()) {
			Room assigned= assignment.assignment().get(guest);
			tardiness.addValue(schedule.tardinessOf(guest, assigned));
			lateness.addValue(schedule.latenessOf(guest, assigned));
		}
	}

	@Override
	public String toString() {
		return schedule.toString() + "\n" + assignment.toString();
	}

}
