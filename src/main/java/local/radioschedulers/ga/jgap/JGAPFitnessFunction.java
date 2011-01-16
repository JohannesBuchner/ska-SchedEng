/**
 * 
 */
package local.radioschedulers.ga.jgap;

import local.radioschedulers.ga.ScheduleFitnessFunction;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;

public final class JGAPFitnessFunction extends FitnessFunction {
	public JGAPFitnessFunction(ScheduleFitnessFunction fitnessFunction) {
		super();
		this.fitnessFunction = fitnessFunction;
	}

	private ScheduleFitnessFunction fitnessFunction;

	private static final long serialVersionUID = 1L;

	@Override
	protected double evaluate(IChromosome c) {
		return this.fitnessFunction.evaluate(JGAPScheduler.getScheduleFromChromosome(c));
	}
}