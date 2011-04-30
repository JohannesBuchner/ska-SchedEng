package local.radioschedulers.alg.parallel;

import local.radioschedulers.Job;
import local.radioschedulers.Schedule;
import local.radioschedulers.alg.ga.watchmaker.SortedCollection.MappingFunction;

public class PressureJobSortCriterion extends JobSortCriterion {

	protected static final boolean IGNORE_LONG_TASKS = true;

	/**
	 * calculates how many times this job could be repeated in the available
	 * timeslots. restricted, short tasks get a low number; unrestricted, long
	 * tasks get a high number
	 */
	public MappingFunction<Job, Double> getSortFunction() {
		return new MappingFunction<Job, Double>() {

			@Override
			public Double map(Job item) {
				int npossiblehours = possibleSlots.get(item).size()
						/ Schedule.LST_SLOTS_PER_HOUR;
				if (IGNORE_LONG_TASKS && item.hours > 6 * 20) {
					// long tasks can be carried on to the next quarters
					return 100 + npossiblehours * 1. / item.hours;
				} else {
					return npossiblehours * 1. / item.hours;
				}
			}
		};
	}

}
