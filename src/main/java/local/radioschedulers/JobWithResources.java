package local.radioschedulers;

import java.util.HashMap;
import java.util.Map;

public class JobWithResources extends Job {
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
