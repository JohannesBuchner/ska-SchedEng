package local.radioschedulers.preschedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.preschedule.parallel.FastRecursiveCompatibleJobFactory;
import local.radioschedulers.preschedule.parallel.CompatibleJobFactory;

import org.apache.log4j.Logger;

public class SimpleTimelineGenerator implements ITimelineGenerator {

	private RequirementGuard req;

	private static Logger log = Logger.getLogger(SimpleTimelineGenerator.class);

	public SimpleTimelineGenerator(RequirementGuard req) {
		this.req = req;
	}

	private ScheduleSpace getPossibleSchedules(Collection<Proposal> proposals,
			RequirementGuard requirementGuard, int ndays) {
		List<Job> alljobs = new ArrayList<Job>();
		for (Proposal p : proposals) {
			alljobs.addAll(p.jobs);
		}
		log.debug("found all jobs: " + alljobs.size());
		// CompatibleJobFactory compatibles1 = new CompatibleJobFactory(
		// alljobs, requirementGuard);
		// log.debug("got compatibleJobFactory with "
		// + compatibles1.getCombinations().size() + " combinations.");
		CompatibleJobFactory compatibles = new FastRecursiveCompatibleJobFactory(
				alljobs, requirementGuard);
		log.debug("got FRcompatibleJobFactory with "
				+ compatibles.getCombinations().size() + " combinations.");
		// just a template for the first day
		ScheduleSpace timelineConstruct = compatibles
				.getPossibleTimeLine(alljobs);
		log.debug("got schedule space construct.");
		// repeat the construct
		ScheduleSpace timeline = new ScheduleSpace();
		log.debug("copying to " + ndays + " days");
		for (int minute = 0; minute < ScheduleSpace.LST_SLOTS_PER_DAY; minute++) {
			LSTTime t = new LSTTime(0, minute * ScheduleSpace.LST_SLOTS_MINUTES);

			Set<JobCombination> jcs = timelineConstruct.get(t);
			// log.debug("at " + t.minute + ": " + jcs.size() +
			// " combinations");
			for (int day = 0; day < ndays; day++) {
				LSTTime t2 = new LSTTime(Long.valueOf(day), t.minute);

				// this will put a empty set there, instead of a null
				timeline.get(t2);

				for (JobCombination jc : jcs) {
					boolean gooddate = true;
					for (Job j : jc.jobs) {
						if (!requirementGuard.isDateCompatible(j, t2)) {
							gooddate = false;
						}
					}
					if (gooddate)
						timeline.add(t2, jc);
				}
				// log.debug("   " + t.day + ": " + jcs.size());
			}
		}
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			if (e.getValue() == null)
				log.debug("created ScheduleSpace is null at " + e.getKey() + "");
		}

		return timeline;
	}

	@Override
	public ScheduleSpace schedule(Collection<Proposal> proposals, int ndays) {
		return getPossibleSchedules(proposals, req, ndays);
	}

}
