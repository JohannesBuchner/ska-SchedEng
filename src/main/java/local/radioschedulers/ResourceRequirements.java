package local.radioschedulers;

import java.util.HashMap;
import java.util.Map;

public class ResourceRequirements {
	/**
	 * key: resourceid of wanted resource
	 * 
	 * value \in [0..1]. The sum of all resource requirements has to be 1. A
	 * preference can be expressed this way.
	 */
	public Map<Integer, Double> requirements = new HashMap<Integer, Double>();

	/**
	 * @return total number of required resources
	 */
	public Integer totalRequired() {
		return requirements.size();
	}
}
