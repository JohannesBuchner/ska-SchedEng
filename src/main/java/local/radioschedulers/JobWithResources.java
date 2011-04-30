package local.radioschedulers;

import java.util.HashMap;
import java.util.Map;

import local.radioschedulers.preschedule.date.DateRequirements;
import local.radioschedulers.preschedule.date.NoDateRequirements;

/**
 * A job with resource and date requirement specifications.
 * 
 * @author Johannes Buchner
 */
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
	public Map<String, ResourceRequirement> resources = new HashMap<String, ResourceRequirement>();
}
