package local.radioschedulers.alg.parallel;

import local.radioschedulers.Job;
import local.radioschedulers.alg.ga.watchmaker.SortedCollection.MappingFunction;

public class PriorityJobSortCriterion extends JobSortCriterion {

	@Override
	public MappingFunction<Job, Double> getSortFunction() {
		return new MappingFunction<Job, Double>() {

			@Override
			public Double map(Job item) {
				double v;
				if (item.proposal.mustcomplete)
					v = item.proposal.priority + 10;
				v = item.proposal.priority;
				return -v;
			}
		};
	}
}
