package local.radioschedulers.greedy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.watchmaker.SortedCollection;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

import org.apache.log4j.Logger;

/**
 * 
 * Scan ScheduleSpace and annotate how many possible slots are available for
 * each Job (= pressure).
 * 
 * Order jobs by pressure, priority, shortness
 * 
 * Starting from the highest pressure, assign.
 * 
 * @author Johannes Buchner
 */
public class GreedyDifficultyScheduler implements IScheduler {
	protected static final boolean IGNORE_LONG_TASKS = true;

	private static Logger log = Logger.getLogger(GreedyDifficultyScheduler.class);

	protected Map<Job, Collection<LSTTime>> possibleSlots = new HashMap<Job, Collection<LSTTime>>();
	protected Map<Job, Integer> npossibleSlots = new HashMap<Job, Integer>();

	/**
	 * how many hours are left for this job
	 */
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(ScheduleSpace timeline) {
		Schedule s = new Schedule();

		log
				.debug("scanning schedulespace for number of possible slot for each job");
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			Set<JobCombination> jcs = e.getValue();
			if (jcs.size() == 1) {
				// trivial choice
				JobCombination jc = jcs.iterator().next();
				for (Job j : jc.jobs) {
					if (!timeleft.containsKey(j)) {
						s.add(e.getKey(), jc);
						timeleft.put(j, j.hours - 1.
								/ Schedule.LST_SLOTS_PER_HOUR);
					} else {
						if (timeleft.get(j) > 0) {
							s.add(e.getKey(), jc);
							timeleft.put(j, timeleft.get(j) - 1.
									/ Schedule.LST_SLOTS_PER_HOUR);
						}
					}
				}
				continue;
			}
			for (JobCombination jc : jcs) {
				for (Job j : jc.jobs) {
					if (!timeleft.containsKey(j))
						timeleft.put(j, (double) j.hours);

					Integer n = npossibleSlots.get(j);
					if (n == null) {
						npossibleSlots.put(j, 1);
						possibleSlots.put(j, new ArrayList<LSTTime>());
					} else {
						npossibleSlots.put(j, n + 1);
					}
					possibleSlots.get(j).add(e.getKey());
				}
			}
		}

		log.debug("sorting jobs by pressure");
		SortedCollection<Job> jobsSortedByPressure = new SortedCollection<Job>(
				possibleSlots.keySet(), getOvershoot());

		log.debug("assigning job order in schedulespace based on pressure");
		Map<LSTTime, List<Job>> pressureOrderedTasks = new HashMap<LSTTime, List<Job>>();

		for (Job j : jobsSortedByPressure) {
			log.debug("# of slots: " + npossibleSlots.get(j) + " for " + j);

			// try to schedule it
			for (LSTTime t : possibleSlots.get(j)) {
				List<Job> l = pressureOrderedTasks.get(t);
				if (l == null)
					l = new ArrayList<Job>(timeline.get(t).size());

				l.add(j);
				pressureOrderedTasks.put(t, l);
			}
		}

		assignTasks(timeline, s, pressureOrderedTasks, jobsSortedByPressure);

		return s;
	}

	protected void assignTasks(ScheduleSpace timeline, Schedule s,
			Map<LSTTime, List<Job>> pressureOrderedTasks,
			SortedCollection<Job> jobsSortedByPressure) {
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

	/**
	 * calculates how many times this job could be repeated in the available
	 * timeslots. restricted, short tasks get a low number; unrestricted, long
	 * tasks get a high number
	 */
	protected MappingFunction<Job, Double> getOvershoot() {
		return new MappingFunction<Job, Double>() {

			@Override
			public Double map(Job item) {
				int npossiblehours = npossibleSlots.get(item)
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + " instance " + hashCode();
	}

}
