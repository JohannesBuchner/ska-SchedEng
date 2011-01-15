package local.radioschedulers.ga.test;

public abstract class FunctionProblem {
	
	/**
	 * evaluates how good the approxation is (bigger is better)

	 * @param values
	 * @return a fitness value
	 */
	public abstract double evaluateApproximation(double[] values);
	
	public abstract int getSize();
	
	public abstract double getMax(int i);
	
	public abstract double getMin(int i);
}
