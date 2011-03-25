package local.radioschedulers.run.demo;

import java.util.Collection;

public interface MonitoringSystem {

	public abstract boolean haveResourcesChanged();

	public abstract Collection<String> getAvailableBackends();

	public abstract Collection<String> getAvailableAntennas();

}
