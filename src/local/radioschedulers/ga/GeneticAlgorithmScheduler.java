package local.radioschedulers.ga;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.cpu.RandomizedScheduler;

public class GeneticAlgorithmScheduler implements IScheduler {
	public static final int LST_SLOTS = 24 * 4;
	public static final int LST_SLOTS_MINUTES = 24 * 60 / LST_SLOTS;

	protected HashMap<LSTTime, Vector<Job>> possibles = new HashMap<LSTTime, Vector<Job>>();
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(Collection<Proposal> proposals) {
		Schedule[] s = getStartSchedules(proposals);

		Schedule bestschedule = evolveSchedules(s);

		return bestschedule;
	}

	protected Schedule evolveSchedules(Schedule[] s) {
		return s[0];
	}

	protected Schedule[] getStartSchedules(Collection<Proposal> proposals) {
		RandomizedScheduler rs = new RandomizedScheduler();
		Schedule[] s = new Schedule[1];
		s[0] = rs.schedule(proposals);
		return s;
	}
}
