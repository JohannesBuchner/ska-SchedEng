package local.radioschedulers.serial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import local.radioschedulers.deciders.JobSelector;

import org.apache.log4j.Logger;

public class SerialListingScheduler implements IScheduler {
	private static Logger log = Logger.getLogger(SerialListingScheduler.class);

	/**
	 * how many hours are left for this job
	 */
	protected Map<Job, Double> timeleft = new HashMap<Job, Double>();

	/**
	 * which timeslots are left for this job
	 */
	protected Map<Job, List<LSTTime>> possibles = new HashMap<Job, List<LSTTime>>();
	protected List<LSTTime> unassignedTimeslots = new ArrayList<LSTTime>();

	protected JobSelector jobselector;

	public SerialListingScheduler(JobSelector jobselector) {
		this.jobselector = jobselector;
		this.jobselector.setTimeleft(timeleft);
		this.jobselector.setPossibles(possibles);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " with jobselector " + jobselector
				+ " instance " + hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(ScheduleSpace timeline) {
		Schedule s = new Schedule();

		fillTimeLeft(timeline);

		log.debug("Allocating:");

		while(!unassignedTimeslots.isEmpty()) {
			LSTTime t = getNextUnassignedTimeslot();

			cleanup(timeleft);
			if (timeleft.isEmpty()) {
				break;
			}

			Set<JobCombination> list = timeline.get(t);

			if (list == null || list.isEmpty()) {
				log.debug("nothing to do @" + t);
				continue;
			}

			// select next
			JobCombination selected = selectJobs(list);

			for (JobCombination jc : list ) {
				for (Job j : jc.jobs) {
					possibles.get(j).remove(t);
				}
			}
			
			if (selected == null) {
				log.debug("@" + t + " : nothing");
			} else {
				log.debug("@" + t + " : #jobs: " + selected.jobs.size());
				s.add(new LSTTime(t.day, t.minute), selected);
				/* count down time left */
				for (Job j : selected.jobs) {
					Double newtime = timeleft.get(j) - 1.
							/ Schedule.LST_SLOTS_PER_HOUR;
					log.debug("@" + t + " : " + j + " (" + newtime + " left)");
					if (newtime <= 0) {
						timeleft.remove(j);
					} else {
						timeleft.put(j, newtime);
					}
				}
			}
		}

		return s;

	}

	private LSTTime getNextUnassignedTimeslot() {
		return unassignedTimeslots.remove(0);
	}

	private void fillTimeLeft(ScheduleSpace timeline) {
		HashSet<JobCombination> alljobCombinations = new HashSet<JobCombination>();
		Set<Job> alljobs = new HashSet<Job>();
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			unassignedTimeslots.add(e.getKey());
			alljobCombinations.addAll(e.getValue());
			if (e.getValue() != null) {
				for (JobCombination jc : e.getValue()) {
					for (Job j : jc.jobs) {
						List<LSTTime> l = possibles.get(j);
						if (l == null) {
							l = new ArrayList<LSTTime>();
							possibles.put(j, l);
						}
						l.add(e.getKey());
					}
				}
			}
		}
		for (JobCombination jc : alljobCombinations) {
			alljobs.addAll(jc.jobs);
		}
		for (Job j : alljobs) {
			timeleft.put(j, (double) j.hours);
		}
	}

	protected JobCombination selectJobs(Collection<JobCombination> list) {
		Collection<JobCombination> jcs = this.jobselector.select(list);
		if (jcs.isEmpty())
			return null;
		else
			return jcs.iterator().next();
	}

	private void cleanup(Map<Job, Double> timeleft) {
		for (Job j2 : timeleft.keySet()) {
			if (timeleft.get(j2) <= 0) {
				timeleft.remove(j2);
				log.debug("only " + timeleft.size() + " jobs left");
			}
		}
		// log.debug(timeleft.size() + " proposals left");
	}
}
