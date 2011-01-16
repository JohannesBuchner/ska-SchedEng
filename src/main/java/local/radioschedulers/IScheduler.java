package local.radioschedulers;
import java.util.Collection;

/**
 * A scheduling algorithm
 * 
 * @author Johannes Buchner
 */
public interface IScheduler {

	public abstract SpecificSchedule schedule(Collection<Proposal> proposals, int ndays);

}