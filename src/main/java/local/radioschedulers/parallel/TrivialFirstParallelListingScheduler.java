package local.radioschedulers.parallel;


public class TrivialFirstParallelListingScheduler extends
		ParallelListingScheduler {

	public TrivialFirstParallelListingScheduler(JobSortCriterion sortFunction) {
		super(sortFunction);
		setMakeTrivialChoicesFirst(true);
	}

}
