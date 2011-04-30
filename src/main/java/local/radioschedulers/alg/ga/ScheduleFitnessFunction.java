package local.radioschedulers.alg.ga;

import local.radioschedulers.Schedule;

public interface ScheduleFitnessFunction {
	public double evaluate(Schedule s);
}
