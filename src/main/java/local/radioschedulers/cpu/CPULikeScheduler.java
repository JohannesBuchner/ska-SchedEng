package local.radioschedulers.cpu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.TimeLine;
import local.radioschedulers.parallel.CompatibleJobFactory;
import local.radioschedulers.parallel.ParallelRequirementGuard;

public class CPULikeScheduler implements IScheduler {
	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	protected HashMap<LSTTime, JobCombination> possibles = new HashMap<LSTTime, JobCombination>();
	protected TimeLine timeline = new TimeLine();
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();

	protected JobSelector jobselector;
	protected ParallelRequirementGuard requirementGuard;

	public CPULikeScheduler(JobSelector jobselector,
			ParallelRequirementGuard requirementGuard) {
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
		generatePossibles(proposals);

		System.out.println("Allocating:");
		LSTTime t = new LSTTime(0L, 0L);
		for (t.day = 0L; !timeleft.isEmpty() && t.day < ndays; t.day++) {
			cleanup(timeleft);
			for (t.minute = 0L; t.minute < 24 * 60 && !timeleft.isEmpty(); t.minute += LST_SLOTS_MINUTES) {
				List<JobCombination> list = timeline.possibles.get(new LSTTime(
						0L, t.minute));

				if (list == null || list.isEmpty()) {
					System.out.println("nothing to do @" + t);
					continue;
				}
				list = pruneForRequirements(list, t);

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

	private void generatePossibles(Collection<Proposal> proposals) {

		List<Job> alljobs = new ArrayList<Job>();
		for (Proposal p : proposals) {
			alljobs.addAll(p.jobs);
			for (Job j : p.jobs) {
				timeleft.put(j, j.hours * 1.);
			}
		}
		CompatibleJobFactory compatibles = new CompatibleJobFactory(alljobs,
				requirementGuard);
		timeline = compatibles.getPossibleTimeLine(alljobs);
	}

	protected List<JobCombination> pruneForRequirements(
			Collection<JobCombination> list, LSTTime date) {
		List<JobCombination> selected = new ArrayList<JobCombination>();
		for (JobCombination jc : list) {
			if (this.requirementGuard.isDateCompatible(jc, date)) {
				selected.add(jc);
			}
		}
		return selected;
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
