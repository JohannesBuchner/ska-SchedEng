package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

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

	protected HashMap<LSTTime, JobCombination> possibles = new HashMap<LSTTime, JobCombination>();
	protected ScheduleSpace timeline = new ScheduleSpace();
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
		this.timeline = timeline;
		int ndays = timeline.findLastEntry().day.intValue();

		System.out.println("Allocating:");
		LSTTime t = new LSTTime(0L, 0L);
		for (t.day = 0L; !timeleft.isEmpty() && t.day < ndays; t.day++) {
			cleanup(timeleft);
			for (t.minute = 0L; t.minute < 24 * 60 && !timeleft.isEmpty(); t.minute += LST_SLOTS_MINUTES) {
				Set<JobCombination> list = timeline.get(new LSTTime(0L,
						t.minute));

				if (list == null || list.isEmpty()) {
					System.out.println("nothing to do @" + t);
					continue;
				}

				// select next
				JobCombination selected = selectJobs(list);

				if (selected == null)
					possibles.remove(new LSTTime(0L, t.minute));
				else {
					s.add(new LSTTime(t.day, t.minute), selected);
					/* count down time left */
					for (Job j : selected.jobs) {
						Double newtime = timeleft.get(j) - LST_SLOTS_MINUTES
								/ 60.;
						System.out.println("@" + t + " : " + j + " (" + newtime
								+ " left)");
						if (newtime <= 0) {
							timeleft.remove(j);
						} else {
							timeleft.put(j, newtime);
						}
					}
				}
			}
		}

		return s;

	}

	protected JobCombination selectJobs(Collection<JobCombination> list) {
		Collection<JobCombination> jc = this.jobselector.select(list);
		if (jc.isEmpty())
			return null;
		else
			return jc.iterator().next();
	}

	private void cleanup(HashMap<Job, Double> timeleft) {
		for (Job j2 : timeleft.keySet()) {
			if (timeleft.get(j2) <= 0) {
				timeleft.remove(j2);
				System.out.println("only " + timeleft.size()
						+ " proposals left");
			} else {
				System.out.println("Job left: " + j2);
			}
		}
		System.out.println(timeleft.size() + " proposals left");
	}
}
