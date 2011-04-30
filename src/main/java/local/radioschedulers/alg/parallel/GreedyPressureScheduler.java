package local.radioschedulers.alg.parallel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.watchmaker.SortedCollection;
import local.radioschedulers.alg.ga.watchmaker.SortedCollection.MappingFunction;

import org.apache.log4j.Logger;

/**
 * Scan ScheduleSpace and annotate how many possible slots are available for
 * each Job (= pressure).
 * 
 * At each timeslot, order possible jobs by pressure.
 * 
 * Go through and assign first choice.
 * 
 * The result should be close to the MinimumLaxity rule. The main difference is
 * that we select as many parallel jobs as possible, but keep strictly to the
 * pressure order
 * 
 * @author Johannes Buchner
 */
public class GreedyPressureScheduler extends ParallelListingScheduler {
	public GreedyPressureScheduler() {
		super(new PressureJobSortCriterion());
	}

	protected static final boolean IGNORE_LONG_TASKS = true;

	private static Logger log = Logger
			.getLogger(GreedyPressureScheduler.class);

	private Map<LSTTime, List<Job>> pressureOrderedTasks;

	@Override
	protected void placeJob(ScheduleSpace timeline, Schedule s, Job j) {
		for (LSTTime t : possibleSlots.get(j)) {
			List<Job> l = pressureOrderedTasks.get(t);
			if (l == null)
				l = new ArrayList<Job>(timeline.get(t).size());

			l.add(j);
			pressureOrderedTasks.put(t, l);
		}
		super.placeJob(timeline, s, j);
	}

	@Override
	protected Schedule doSchedule(ScheduleSpace timeline, Schedule s) {
		pressureOrderedTasks = new HashMap<LSTTime, List<Job>>();
		super.doSchedule(timeline, s);
		assignTasks(timeline, s, pressureOrderedTasks);
		return s;
	}

	protected void assignTasks(ScheduleSpace timeline, Schedule s,
			Map<LSTTime, List<Job>> pressureOrderedTasks) {
		log
				.debug("selecting suitable JobCombination for each slot in schedulespace");
		// now we have for each time the ordered tasks there.
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			LSTTime t = e.getKey();
			if (e.getValue().isEmpty())
				continue;
			// we want these jobs:
			List<Job> jl1 = pressureOrderedTasks.get(t);
			if (jl1 == null)
				continue;
			List<Job> jl = new ArrayList<Job>(jl1.size());
			// clean fulfilled tasks
			for (Job j : jl1) {
				if (timeleft.get(j) > 0) {
					jl.add(j);
				}
			}
			if (jl.isEmpty())
				continue;

			JobCombination bestJobCombination = new SortedCollection<JobCombination>(
					e.getValue(), getCoveredCountMapper(jl)).first();

			if (bestJobCombination != null) {
				s.add(t, bestJobCombination);

				for (Job j : bestJobCombination.jobs) {
					// log.debug("timeleft of " + j + ": " + timeleft.get(j));
					timeleft.put(j, timeleft.get(j) - 1.
							/ Schedule.LST_SLOTS_PER_HOUR);
				}
			}
		}
	}

	/**
	 * calculates how many jobs at the start of jl are covered by this
	 * JobCombination.
	 * 
	 * @param jl
	 * @return
	 */
	private MappingFunction<JobCombination, Integer> getCoveredCountMapper(
			final List<Job> jl) {
		return new MappingFunction<JobCombination, Integer>() {

			@Override
			public Integer map(JobCombination item) {
				int count = 0;
				for (Job j : jl) {
					if (item.jobs.contains(j)) {
						count++;
					} else {
						break;
					}
				}
				return -count;
			}
		};
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " instance " + hashCode();
	}

}
