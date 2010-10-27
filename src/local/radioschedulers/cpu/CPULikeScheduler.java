package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;

public class CPULikeScheduler implements IScheduler {
	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();

	protected JobSelector jobselector;
	protected RequirementGuard requirementGuard;

	public CPULikeScheduler(JobSelector jobselector,
			RequirementGuard requirementGuard) {
		this.jobselector = jobselector;
		this.jobselector.setTimeleft(timeleft);
		this.requirementGuard = requirementGuard;
	}

	@Override
	public String toString() {
		return getClass().getName() + " with jobselector " + jobselector
				+ ", requirementGuard " + requirementGuard;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(Collection<Proposal> proposals, int ndays) {
		Schedule s = new Schedule();
		possibles = new HashMap<LSTTime, Vector<Job>>();
		new HashMap<Job, Double>();

		System.out.println("Possibles:");
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				timeleft.put(j, j.hours * 1.);
				for (long minute = Math.round(j.lstmin) * 60;; minute = (minute + LST_SLOTS_MINUTES)
						% (24 * 60)) {
					if (j.lstmin < j.lstmax) {
						if (minute < Math.round(j.lstmin) * 60
								|| minute > Math.round(j.lstmax) * 60)
							break;
					} else {
						if (minute < Math.round(j.lstmin) * 60
								&& minute > Math.round(j.lstmax) * 60)
							break;
					}

					LSTTime t = new LSTTime(0L, minute);
					Vector<Job> list;
					if (possibles.containsKey(t)) {
						list = possibles.get(t);
					} else {
						list = new Vector<Job>();
						possibles.put(t, list);
					}
					list.add(j);
					System.out.println("@" + t + " : " + j);
					possibles.put(t, list);
				}
			}
		}

		System.out.println("Allocating:");
		LSTTime t = new LSTTime(0L, 0L);
		for (t.day = 0L; !timeleft.isEmpty() && t.day < ndays; t.day++) {
			cleanup(timeleft);
			for (t.minute = 0L; t.minute < 24 * 60 && !timeleft.isEmpty(); t.minute += LST_SLOTS_MINUTES) {
				Collection<Job> list = possibles.get(new LSTTime(0L, t.minute));

				if (list == null || list.isEmpty()) {
					System.out.println("nothing to do @" + t);
					continue;
				}
				list = pruneForRequirements(list, t);

				// select next
				Collection<Job> selected = selectJobs(list);

				if (selected.isEmpty())
					possibles.remove(new LSTTime(0L, t.minute));
				else {
					for (Job j : selected) {
						s.add(new LSTTime(t.day, t.minute), j);
						// System.out.println("selected job " + j + " for " +
						// t);
						Double newtime = timeleft.get(j) - LST_SLOTS_MINUTES
								/ 60.;
						System.out.println("@" + t + " : " + j + " (" + newtime
								+ " left)");
						if (newtime <= 0) {
							timeleft.remove(j);
							s.get(t).remove(j);
						} else {
							timeleft.put(j, newtime);
						}
					}
				}
			}
		}

		return s;

	}

	protected Collection<Job> pruneForRequirements(Collection<Job> list,
			LSTTime date) {
		return this.requirementGuard.pruneDate(list, date);
	}

	protected Collection<Job> selectJobs(Collection<Job> list) {
		Collection<Job> jobsByPreference = this.jobselector.select(list);
		return this.requirementGuard.prune(jobsByPreference);
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
