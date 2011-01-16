package local.radioschedulers.ga;

import local.radioschedulers.SpecificSchedule;

public interface ScheduleFitnessFunction {
	public double evaluate(SpecificSchedule s);
}
