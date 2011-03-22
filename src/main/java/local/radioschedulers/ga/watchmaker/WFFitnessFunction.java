package local.radioschedulers.ga.watchmaker;

import java.util.List;

import local.radioschedulers.Schedule;
import local.radioschedulers.ga.ScheduleFitnessFunction;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

public final class WFFitnessFunction implements FitnessEvaluator<Schedule> {
	private static final long serialVersionUID = 1L;

	public WFFitnessFunction(ScheduleFitnessFunction fitnessFunction) {
		super();
		this.fitnessFunction = fitnessFunction;
	}

	private ScheduleFitnessFunction fitnessFunction;

	@Override
	public double getFitness(Schedule candidate,
			List<? extends Schedule> population) {
		return this.fitnessFunction.evaluate(candidate);
	}

	@Override
	public boolean isNatural() {
		return true;
	}
}