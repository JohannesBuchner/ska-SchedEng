package local.radioschedulers.preschedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.parallel.CompatibleJobFactory;

public class SimpleTimelineGenerator implements ITimelineGenerator {
	private RequirementGuard req;

	public SimpleTimelineGenerator(RequirementGuard req) {
		this.req = req;
	}

	public ScheduleSpace getPossibleSchedules(
			Collection<Proposal> proposals, RequirementGuard requirementGuard) {
		List<Job> alljobs = new ArrayList<Job>();
		for (Proposal p : proposals) {
			alljobs.addAll(p.jobs);
		}
		CompatibleJobFactory compatibles = new CompatibleJobFactory(alljobs,
				requirementGuard);
		return compatibles.getPossibleTimeLine(alljobs);
	}

	@Override
	public ScheduleSpace schedule(Collection<Proposal> proposals,
			int ndays) {
		return getPossibleSchedules(proposals, req);
	}

}
