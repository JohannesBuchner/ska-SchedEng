package local.radioschedulers.lp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.cpu.RandomizedScheduler;

public class LinearScheduler implements IScheduler {
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
		Schedule s;

		StringBuilder text = new StringBuilder();

		int nantennas = 42;
		int ndays = 3;
		int njobs = 0;

		/**
		 * 2) A job is on when it can do work, i.e. the source object is up:
		 */
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				StringBuilder jobsum = new StringBuilder();
				for (int kd = 0; kd < ndays; kd++) {
					for (int kh = 0; kh <= 0; kh += LST_SLOTS_MINUTES) {
						if ((j.lstmax > j.lstmin && kh * 60 > j.lstmax)
								|| (j.lstmax < j.lstmin && kh < j.lstmin && kh > j.lstmax)) {
							// outside
							for (int i = 0; i < nantennas; i++) {
								text.append(getVar(i, njobs,
										(kd * LST_SLOTS + kh
												/ LST_SLOTS_MINUTES))
										+ " = 0;\n");
							}
						} else {
							// inside
							for (int i = 0; i < nantennas; i++) {
								text.append(getVar(i, njobs,
										(kd * LST_SLOTS + kh
												/ LST_SLOTS_MINUTES))
										+ " >= 0;\n");
							}
							/**
							 * 3) A job gets its antennas, at the time it is
							 * running, i.e. resources must be allocated at the
							 * same time.
							 */
							for (int i = 0; i < nantennas; i++) {
								text.append(getVar(i, njobs,
										(kd * LST_SLOTS + kh
												/ LST_SLOTS_MINUTES))
										+ " +");
							}
							// TODO: resources
							// TODO: This is a very weak constraint, allowing
							// less-than-necessary resources
							text.append("0 <= " + 42 + ";\n");
						}

						/**
						 * 4) A job gets its hours:
						 */
						for (int i = 0; i < nantennas; i++) {
							jobsum.append(getVar(i, njobs, (kd * LST_SLOTS + kh
									/ LST_SLOTS_MINUTES))
									+ " +");
						}

					}
				}
				// TODO: this can be moved to the cost function
				jobsum.append("0 <= " + j.hours + ";\n");
				
				
				text.append(jobsum);

				njobs++;
			}
		}
		/**
		 * 1) No incompatible jobs are at the same time k, i.e. not requesting
		 * more resources than we have:
		 */
		for (int k = 0; k < ndays * LST_SLOTS; k++) {
			int i = 0;
			int j = 0;
			for (Proposal p : proposals) {
				for (Job job : p.jobs) {
					/* TODO: handle resources */
					text.append(42 + " " + getVar(i, j, k) + " <= " + nantennas
							+ ";\n");
					i++;
					j++;
				}
			}
		}
		
		System.out.println(text);

		return null;
	}

	private String getVar(int i, int j, int k) {
		return "x_" + i + "_" + j + "_" + k;
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
