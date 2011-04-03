package local.radioschedulers.greedy;

import local.radioschedulers.Job;
import local.radioschedulers.ga.watchmaker.SortedCollection.MappingFunction;

public class PriorityJobSortCriterion extends JobSortCriterion {

	@Override
	public MappingFunction<Job, Double> getSortFunction() {
		return new MappingFunction<Job, Double>() {

			@Override
			public Double map(Job item) {
				if (item.proposal.mustcomplete)
					return item.proposal.priority + 10;
				return item.proposal.priority;
			}
		};
	}
}
