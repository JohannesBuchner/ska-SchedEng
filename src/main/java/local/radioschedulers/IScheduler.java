package local.radioschedulers;

/**
 * A scheduling algorithm
 * 
 * @author Johannes Buchner
 */
public interface IScheduler {

	public abstract SpecificSchedule schedule(SchedulePossibilities timeline);

}