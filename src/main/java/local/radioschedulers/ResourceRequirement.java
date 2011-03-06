package local.radioschedulers;

import java.util.HashSet;
import java.util.Set;

/**
 * Requirement on a resource set.
 * 
 * @author Johannes Buchner
 */
public class ResourceRequirement {
	/**
	 * The individual items of the resource set that are possible to be used.
	 */
	public Set<Object> possibles = new HashSet<Object>();

	/**
	 * Total number of required items
	 */
	public Integer numberrequired;
}
