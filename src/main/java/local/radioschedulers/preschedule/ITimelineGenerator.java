package local.radioschedulers.preschedule;
import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.SchedulePossibilities;

/**
 * A scheduling algorithm
 * 
 * @author Johannes Buchner
 */
public interface ITimelineGenerator {

	public abstract SchedulePossibilities schedule(Collection<Proposal> proposals, int ndays);

}