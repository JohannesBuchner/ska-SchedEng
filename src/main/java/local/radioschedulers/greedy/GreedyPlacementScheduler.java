package local.radioschedulers.greedy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

public class GreedyPlacementScheduler implements IScheduler {
	protected static final boolean IGNORE_LONG_TASKS = true;

	private static Logger log = Logger
			.getLogger(GreedyPlacementScheduler.class);

	protected Map<Job, Collection<LSTTime>> possibleSlots = new HashMap<Job, Collection<LSTTime>>();
	private JobSortCriterion sortFunction;

	public GreedyPlacementScheduler(JobSortCriterion sortFunction) {
		this.sortFunction = sortFunction;
		this.sortFunction.setPossibleSlots(possibleSlots);
	}

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
			LSTTime t = e.getKey();
			Set<JobCombination> jcs = e.getValue();
			if (jcs.size() == 0) {
				log.debug("@" + t + " empty");
			}
			if (jcs.size() == 1) {
				// make trivial choice now
				// log.debug("@" + t +
				// ": only one possible job, selecting it.");
				JobCombination jc = jcs.iterator().next();
				for (Job j : jc.jobs) {
					// log.debug("     " + j);
					if (!timeleft.containsKey(j)) {
						s.add(t, jc);
						timeleft.put(j, j.hours - 1.
								/ Schedule.LST_SLOTS_PER_HOUR);
					} else {
						if (timeleft.get(j) > 0) {
							s.add(t, jc);
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

					Collection<LSTTime> l = possibleSlots.get(j);
					if (l == null) {
						l = new ArrayList<LSTTime>();
						possibleSlots.put(j, l);
					}
					l.add(t);
				}
			}
		}

		log.debug("sorting jobs by pressure");
		SortedCollection<Job> jobsSortedByPressure = new SortedCollection<Job>(
				possibleSlots.keySet(), getSortCriterion());

		log.debug("assigning job order in schedulespace based on pressure");

		for (Job j : jobsSortedByPressure) {
			log.debug("placing " + j);
			Double timeleftj = timeleft.get(j);
			log.debug("need to find an additional "
					+ (timeleftj * Schedule.LST_SLOTS_PER_HOUR) + " slots");
			log.debug("have npossibles = " + possibleSlots.get(j).size());
			for (LSTTime t : possibleSlots.get(j)) {
				// check slot e
				Set<JobCombination> jcs = timeline.get(t);
				JobCombination jc = s.get(t);

				if (jc != null && jc.jobs.contains(j)) {
					// already in there, already selected
					continue;
				}

				// find something that contains j and everything in jc
				JobCombination jc2 = findSupersetJobCombination(j, jc, jcs);
				if (jc2 != null) {
					timeleftj -= 1. / Schedule.LST_SLOTS_PER_HOUR;
					s.clear(t);
					s.add(t, jc2);
					if (timeleftj <= 0) {
						log.debug("placing " + j + " was successful");
						break;
					}
				}
			}
			if (timeleftj > 0) {
				log.debug("placing " + j + " is incomplete.");
			}
		}

		return s;
	}

	private JobCombination findSupersetJobCombination(Job j, JobCombination jc,
			Set<JobCombination> jcs) {

		for (JobCombination jc2 : jcs) {
			if (jc2.jobs.size() != 1 + (jc == null ? 0 : jc.jobs.size()))
				continue;
			if (jc2.jobs.contains(j)) {
				boolean containsall = true;
				if (jc != null) {
					for (Job j2 : jc.jobs) {
						if (!jc2.jobs.contains(j2)) {
							containsall = false;
							break;
						}
					}
				}
				if (containsall) {
					return jc2;
				}
			}
		}
		return null;
	}

	protected MappingFunction<Job, Double> getSortCriterion() {
		return sortFunction.getSortFunction();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " with jobSortFunction "
				+ sortFunction + " instance " + hashCode();
	}

}
