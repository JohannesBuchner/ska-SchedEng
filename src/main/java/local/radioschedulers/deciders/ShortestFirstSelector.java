package local.radioschedulers.deciders;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;

import org.apache.log4j.Logger;

public class ShortestFirstSelector extends JobSelector {

	private static Logger log = Logger.getLogger(ShortestFirstSelector.class);

	@Override
	public void setTimeleft(Map<Job, Double> timeleft) {
		super.setTimeleft(timeleft);
		this.cmp = generateComparator(timeleft);
	}

	protected Comparator<JobCombination> cmp;

	protected Comparator<JobCombination> generateComparator(
			final Map<Job, Double> timeleft) {
		log.debug("generating ShortestFirstSelector Comparator");
		return new Comparator<JobCombination>() {
			@Override
			public int compare(JobCombination o1, JobCombination o2) {
				Double t1 = timeleft.get(o1);
				Double t2 = timeleft.get(o2);

				if (t1 == null)
					if (t2 == null)
						return 0;
					else
						return 1;
				else if (t2 == null)
					return -1;
				else
					return t1.compareTo(t2);
			}
		};
	};

	@Override
	public Collection<JobCombination> select(Collection<JobCombination> list) {
		List<JobCombination> jobs = pruneDone(list);
		Collections.sort(jobs, cmp);
		return super.select(jobs);
	};
}
