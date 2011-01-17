package local.radioschedulers.preschedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.parallel.CompatibleJobFactory;

public class SimpleTimelineGenerator implements ITimelineGenerator {
	private RequirementGuard req;
	private static Logger log = Logger.getLogger(SimpleTimelineGenerator.class);
	private int ndays;

	public SimpleTimelineGenerator(int ndays, RequirementGuard req) {
		this.req = req;
		this.ndays = ndays;
	}

	private ScheduleSpace getPossibleSchedules(Collection<Proposal> proposals,
			RequirementGuard requirementGuard) {
		List<Job> alljobs = new ArrayList<Job>();
		for (Proposal p : proposals) {
			alljobs.addAll(p.jobs);
		}
		log.debug("found all jobs: " + alljobs.size());
		CompatibleJobFactory compatibles = new CompatibleJobFactory(alljobs,
				requirementGuard);
		log.debug("got compatibleJobFactory with "
				+ compatibles.getCombinations().size() + " combinations.");
		// just a template for the first day
		ScheduleSpace timelineConstruct = compatibles.getPossibleTimeLine(alljobs);
		log.debug("got schedule space construct.");
		// repeat the construct
		ScheduleSpace timeline = new ScheduleSpace();
		log.debug("copying to " + ndays + " days");
		for (int minute = 0; minute < ScheduleSpace.LST_SLOTS_PER_DAY; minute++) {
			LSTTime t = new LSTTime(0, minute * ScheduleSpace.LST_SLOTS_MINUTES);

			for (int day = 0; day < ndays; day++) {
				Set<JobCombination> jcs = timelineConstruct.get(t);
				t.day = Long.valueOf(day);

				for (JobCombination jc : jcs) {
					boolean gooddate = true;
					for (Job j : jc.jobs) {
						if (!requirementGuard.isDateCompatible(j, t)) {
							gooddate = false;
						}
					}
					if (gooddate)
						timeline.add(t, jc);
				}
			}
		}

		return timeline;
	}

	@Override
	public ScheduleSpace schedule(Collection<Proposal> proposals, int ndays) {
		return getPossibleSchedules(proposals, req);
	}

}
