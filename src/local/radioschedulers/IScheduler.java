package local.radioschedulers;
import java.util.Collection;

public interface IScheduler {

	public abstract Schedule schedule(Collection<Proposal> proposals);

}