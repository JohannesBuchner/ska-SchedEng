package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.RequirementGuard;

public class CPULikeScheduler implements IScheduler {
	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	private static Logger log = Logger.getLogger(CPULikeScheduler.class);

	/**
	 * how many hours are left for this job
	 */
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();

	protected JobSelector jobselector;

	public CPULikeScheduler(JobSelector jobselector,
			RequirementGuard requirementGuard) {
		this.jobselector = jobselector;
		this.jobselector.setTimeleft(timeleft);
	}

	@Override
	public String toString() {
		return getClass().getName() + " with jobselector " + jobselector;
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

		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			cleanup(timeleft);
			if (timeleft.isEmpty()) {
				break;
			}

			Set<JobCombination> list = e.getValue();
			LSTTime t = e.getKey();

			if (list == null || list.isEmpty()) {
				log.debug("nothing to do @" + t);
				continue;
			}

			// select next
			JobCombination selected = selectJobs(list);

			if (selected == null) {
				log.debug("@" + t + " : nothing");
			} else {
				log.debug("@" + t + " : #jobs: " + selected.jobs.size());
				s.add(new LSTTime(t.day, t.minute), selected);
				/* count down time left */
				for (Job j : selected.jobs) {
					Double newtime = timeleft.get(j) - LST_SLOTS_MINUTES / 60.;
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

	private void fillTimeLeft(ScheduleSpace timeline) {
		HashSet<JobCombination> alljobCombinations = new HashSet<JobCombination>();
		Set<Job> alljobs = new HashSet<Job>();
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			alljobCombinations.addAll(e.getValue());
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

	private void cleanup(HashMap<Job, Double> timeleft) {
		for (Job j2 : timeleft.keySet()) {
			if (timeleft.get(j2) <= 0) {
				timeleft.remove(j2);
				log.debug("only " + timeleft.size() + " proposals left");
			}
		}
		// log.debug(timeleft.size() + " proposals left");
	}
}
