package local.radioschedulers;

/**
 * A scheduling algorithm
 * 
 * @author Johannes Buchner
 */
public interface IScheduler {

	public abstract Schedule schedule(ScheduleSpace timeline);

}