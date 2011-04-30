package local.radioschedulers.serial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.Schedule;

/**
 * select job(combination) with least last-finish time, i.e. distance between
 * number of possible slots and time left:
 * 
 * Laxity := NumberOfPossibleTimeslots - TaskDuration
 * 
 * @author Johannes Buchner
 */
public class MinimumLaxitySelector extends PrioritizedSelector {

	@Override
	protected Comparator<JobCombination> generateComparator(
			final Map<Job, Double> timeleft) {
		return new Comparator<JobCombination>() {

			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Double n1 = getMinimumLaxity(o1);
				Double n2 = getMinimumLaxity(o2);

				return n1.compareTo(n2);
			}

			private Double getMinimumLaxity(JobCombination jc) {
				List<Double> h = new ArrayList<Double>();
				for (Job j : jc.jobs) {
					h.add(possibles.get(j).size() * 1.
							/ Schedule.LST_SLOTS_PER_HOUR - timeleft.get(j));
				}
				return Collections.min(h);
			}
		};
	}
}
