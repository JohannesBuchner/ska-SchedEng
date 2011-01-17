package local.radioschedulers.preschedule;
import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.ScheduleSpace;

/**
 * A scheduling algorithm
 * 
 * @author Johannes Buchner
 */
public interface ITimelineGenerator {

	public abstract ScheduleSpace schedule(Collection<Proposal> proposals, int ndays);

}