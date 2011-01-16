package local.radioschedulers;

import java.util.HashMap;
import java.util.Map;

public class JobWithResources extends Job {
	public JobWithResources() {
	}

	public JobWithResources(JobWithResources j) {
		super(j);
		this.date = j.date;
		this.resources = j.resources;
	}

	/**
	 * date requirements
	 */
	public DateRequirements date = new NoDateRequirements();
	/**
	 * resource requirements
	 * 
	 * key: resource type (e.g. "antennas"
	 */
	public Map<String, ResourceRequirements> resources = new HashMap<String, ResourceRequirements>();
}