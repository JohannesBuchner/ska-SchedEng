package local.radioschedulers.ga.test;

public class DeconvolutionProblem extends FunctionProblem {
	private static final int N = 100;

	private double truefunc[] = new double[N];
	private double obsfunc[] = new double[N];
	private double leakfunc[] = new double[N];

	public DeconvolutionProblem() {
		setupProblem();
	}
	
	private double max = 0.;

	private void setupProblem() {
		double domain = 30.;
		System.out.println("generating theoretical functions ...");
		for (int i = 0; i < N; i++) {
			double x = (i - N / 2) * domain / N;
			truefunc[i] = calcTrueFunc(x);
			leakfunc[i] = calcLeakFunc(x);
			obsfunc[i] = 0;
		}

		System.out.println("generating observation ... ");
		convolve(leakfunc, truefunc, obsfunc);
		max = sum(obsfunc);
		System.out.println("problem set up.");
	}

	private static double sum(double[] array) {
		double sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}

	private static void convolve(double leakfunc[], double truefunc[], double[] obsfunc) {
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				int dist = j - i + N / 2;
				if (dist > N / 2 || dist < 0)
					continue;
				obsfunc[i] += truefunc[j] * leakfunc[dist];
			}
		}
	}

	private static double calcLeakFunc(double x) {
		return Math.exp(-Math.pow(x, 2));
	}

	private static double calcTrueFunc(double x) {
		double v = 1.;
		if (x != 0)
			v = Math.pow(Math.sin(x) / x, 2);
		if (x != 10)
			v = v + Math.pow(Math.sin(x - 10) / (x - 10), 2);
		if (x == 3)
			v = v + 0.3;
		return v;
	}

	@Override
	public double evaluateApproximation(double[] values) {
		double diff = 0.;
		for (int i = 0; i < N; i++) {
			Double ours = 0.;
			for (int j = 0; j < N; j++) {
				int dist = j - i + N / 2;
				if (dist > N / 2 || dist < 0)
					continue;
				ours += values[j] * leakfunc[dist];
			}
			diff += Math.pow(ours - obsfunc[i], 2);
		}
		diff = Math.sqrt(diff / N);
		return -diff;
	}

	@Override
	public double getMax(int i) {
		return max;
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
