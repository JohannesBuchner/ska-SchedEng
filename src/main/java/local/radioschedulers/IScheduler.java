package local.radioschedulers;
import java.util.Collection;

public interface IScheduler {

	public abstract SpecificSchedule schedule(Collection<Proposal> proposals, int ndays);

}