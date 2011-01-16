package local.radioschedulers.ga.test;

public class SimpleFunctionProblem extends FunctionProblem {
	private static final int N = 30;

	private static double truefunc[] = new double[N];

	public SimpleFunctionProblem() {
		setupProblem();
	}

	private static void setupProblem() {
		double domain = 30.;
		System.out.println("generating theoretical functions ...");
		for (int i = 0; i < N; i++) {
			double x = (i - N / 2) * domain / N;
			truefunc[i] = calcTrueFunc(x);
		}
	}

	private static double calcTrueFunc(double x) {
		double v;
		if (x == 0)
			v = 1;
		else
			v = Math.pow(Math.sin(x) / x, 2);
		if (x == 10)
			v = v + 1;
		else
			v = v + Math.pow(Math.sin(x - 10) / (x - 10), 2);

		if (x == 3)
			v = v + 0.3;
		return v;
	}

	@Override
	public double evaluateApproximation(double[] values) {
		double diff = 0.;
		for (int i = 0; i < N; i++) {
			diff += Math.pow(values[i] - truefunc[i], 2);
		}
		diff = Math.sqrt(diff / N);
		return -diff;
	}

	@Override
	public double getMax(int i) {
		return 10;
	}

	@Override
	public double getMin(int i) {
		return 0;
	}

	@Override
	public int getSize() {
		return N;
	}

}
