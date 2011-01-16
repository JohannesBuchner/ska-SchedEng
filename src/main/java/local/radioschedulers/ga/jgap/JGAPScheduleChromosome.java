package local.radioschedulers.ga.jgap;

import local.radioschedulers.Schedule;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.IGeneConstraintChecker;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.SetGene;

public class JGAPScheduleChromosome extends SetGene {

	public JGAPScheduleChromosome() throws InvalidConfigurationException {
		super();
	}

	public JGAPScheduleChromosome(Schedule s)
			throws InvalidConfigurationException {
		this.s = s;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Schedule s;
}
