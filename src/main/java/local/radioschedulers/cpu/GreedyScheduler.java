package local.radioschedulers.cpu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
public class GreedyScheduler implements IScheduler {
	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	private static Logger log = Logger.getLogger(GreedyScheduler.class);

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
			for (JobCombination jc : e.getValue()) {
				for (Job j : jc.jobs) {
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
			timeleft.put(j, (double) j.hours);

			// try to schedule it
			for (LSTTime t : possibleSlots.get(j)) {
				List<Job> l = pressureOrderedTasks.get(t);
				if (l == null)
					l = new ArrayList<Job>(timeline.get(t).size());

				l.add(j);
				pressureOrderedTasks.put(t, l);
			}
		}

		log
				.debug("selecting suitable JobCombination for each slot in schedulespace");
		// now we have for each time the ordered tasks there.
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			LSTTime t = e.getKey();
			if (e.getValue().isEmpty())
				continue;
			// we want these jobs:
			List<Job> jl = pressureOrderedTasks.get(t);
			for (Iterator<Job> it = jl.iterator(); it.hasNext();) {
				Job j = it.next();
				if (timeleft.get(j) <= 0) {
					it.remove();
				}
			}
			JobCombination bestJobCombination = new SortedCollection<JobCombination>(
					e.getValue(), getCoveredCountMapper(jl)).first();

			if (bestJobCombination != null) {
				s.add(t, bestJobCombination);

				for (Job j : bestJobCombination.jobs) {
					// log.debug("timeleft of " + j + ": " + timeleft.get(j));
					timeleft.put(j, timeleft.get(j)
							- Schedule.LST_SLOTS_MINUTES / 60.);
				}
			}
		}

		return s;
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
				return count;
			}
		};
	}

	/**
	 * calculates how many times this job could be repeated in the available
	 * timeslots. restricted, short tasks get a low number; unrestricted, long
	 * tasks get a high number
	 */
	private MappingFunction<Job, Double> getOvershoot() {
		return new MappingFunction<Job, Double>() {

			@Override
			public Double map(Job item) {
				return npossibleSlots.get(item) * 1. / item.hours;
			}
		};
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " instance " + hashCode();
	}

}
