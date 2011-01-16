package local.radioschedulers.cpu;
import java.util.Collection;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.SpecificSchedule;

public class FIFOScheduler implements IScheduler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public SpecificSchedule schedule(Collection<Proposal> proposals, int ndays) {
		SpecificSchedule s = new SpecificSchedule();
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				JobCombination jc = new JobCombination();
				jc.jobs.add(j);
				
				log("placing " + j);

				double len = j.hours;
				LSTTime t = new LSTTime(0L, 0L);

				for (t.day = 0L;t.day < ndays; t.day++) {
					for (t.minute = 0L; t.minute < 24 * 60; t.minute += 15) {
						if (t.minute < j.lstmin * 60
								|| t.minute > j.lstmax * 60) {
							// System.out.println("Skipping " + t.minute);
							continue;
						}
						if (s.isEmpty(t)) {
							s.add(t, jc);
							len -= 15. / 60;
							log("assigned a bit of " + j
									+ " to " + t.day + ":" + t.minute + ". "
									+ len + " left.");
							t = new LSTTime(t.day, t.minute);
							if (len <= 0)
								break;
						}
					}
					if (len <= 0)
						break;
				}

			}

		}

		return s;

	}

	private void log(String string) {
		
	}
}
