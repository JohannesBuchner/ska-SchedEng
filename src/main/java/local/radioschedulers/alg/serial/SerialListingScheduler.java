package local.radioschedulers.alg.serial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;

public class SerialListingScheduler extends ListingScheduler {
	private static Logger log = Logger.getLogger(SerialListingScheduler.class);

	protected List<LSTTime> unassignedTimeslots = new ArrayList<LSTTime>();

	protected JobSelector jobselector;

	private boolean trackPossiblesReduction;

	public SerialListingScheduler(JobSelector jobselector) {
		this.jobselector = jobselector;
		this.jobselector.setTimeleft(timeleft);
		setTrackPossiblesReduction(false);
		this.jobselector.setPossibles(possibleSlots);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " with jobselector " + jobselector
				+ " instance " + hashCode();
	}

	protected void choose(LSTTime t, JobCombination jc, Schedule s) {
		s.add(t, jc);
		reduceTimeleft(jc, s);
	}

	private void reduceTimeleft(JobCombination jc, Schedule s) {
		for (Job j : jc.jobs) {
			timeleft.put(j, timeleft.get(j) - 1. / Schedule.LST_SLOTS_PER_HOUR);
			if (timeleft.get(j) <= 0) {
				log.debug("done scheduling " + j);
				timeleft.remove(j);
				removeChoice(j, s);
			}
		}
	}

	protected List<LSTTime> removeChoice(Job j, Schedule s) {
		return possibleSlots.remove(j);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	protected Schedule doSchedule(ScheduleSpace timeline, Schedule s) {
		while (!possibleSlots.isEmpty()) {
			LSTTime t = getNextUnassignedTimeslot();
			if (log.isDebugEnabled())
				log.debug("@" + t);
			if (t == null)
				break;
			Collection<JobCombination> list = getChoices(timeline, t);
			if (log.isDebugEnabled())
				log.debug("@" + t + " " + list.size() + "choices");
			JobCombination jc = select(list);
			if (log.isDebugEnabled())
				log.debug("@" + t + " -- selected " + jc);
			if (jc != null)
				choose(t, jc, s);
			if (log.isDebugEnabled())
				log.debug("@" + t + " -- chose " + jc);

			if (trackPossiblesReduction) {
				for (JobCombination jc1 : list) {
					for (Job j : jc1.jobs) {
						if (possibleSlots.containsKey(j))
							possibleSlots.get(j).remove(t);
					}
				}
			}
		}
		return s;
	}

	@Override
	protected void beforeSchedule(ScheduleSpace timeline) {
		super.beforeSchedule(timeline);
		for (Entry<LSTTime, Set<JobCombination>> e : timeline) {
			this.unassignedTimeslots.add(e.getKey());
		}
	}

	private Collection<JobCombination> getChoices(ScheduleSpace timeline,
			LSTTime t) {
		return timeline.get(t);
	}

	protected LSTTime getNextUnassignedTimeslot() {
		if (unassignedTimeslots.isEmpty())
			return null;
		else
			return unassignedTimeslots.remove(0);
	}

	protected JobCombination select(Collection<JobCombination> jcs) {
		return this.jobselector.select(jcs);
	}

	protected void setTrackPossiblesReduction(boolean trackPossiblesReduction) {
		this.trackPossiblesReduction = trackPossiblesReduction;
	}

	protected boolean isTrackPossiblesReduction() {
		return trackPossiblesReduction;
	}

}
