package local.radioschedulers.cpu;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import local.radioschedulers.Job;

public class ShortestFirstSelector extends JobSelector {

	@Override
	public void setTimeleft(HashMap<Job, Double> timeleft) {
		super.setTimeleft(timeleft);
		this.cmp = generateComparator(timeleft);
	}
	
	protected Comparator<Job> cmp;

	protected Comparator<Job> generateComparator(final HashMap<Job, Double> timeleft) {
		return new Comparator<Job>() {
			@Override
			public int compare(Job o1, Job o2) {
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

	public Collection<Job> select(Collection<Job> list) {
		List<Job> jobs = pruneDone(list);
		Collections.sort(jobs, cmp);
		return super.select(jobs);
	};
}
