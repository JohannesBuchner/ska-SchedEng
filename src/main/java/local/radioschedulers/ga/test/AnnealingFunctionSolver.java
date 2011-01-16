package local.radioschedulers.ga.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class AnnealingFunctionSolver {

	private FunctionProblem problem;

	public AnnealingFunctionSolver(FunctionProblem problem) {
		this.problem = problem;
	}

	public void solve() {
		int n = problem.getSize();
		double values[] = new double[n];
		double parameters[] = new double[n];
		double parameters2[] = new double[n];

		int nSegments = 1;
		double v;
		double w;

		fillvalues(values, parameters, nSegments);
		v = problem.evaluateApproximation(values);

		Random r = new Random();
		File dumpFile = new File("/tmp/annealing.txt");

		double noise = 1;
		while (true) {
			System.out.println("nSegments: " + nSegments);
			int ntakes = 0;
			int notakes = 0;
			noise = 0.1;
			System.out.println(nSegments);
			while (ntakes + notakes < 10000) {
				System.out.println(nSegments + " notakes:" + notakes
						+ " ntakes:" + ntakes + " noise: "
						+ Math.round(Math.log10(noise)));
				for (int j = 0; j < nSegments + 1; j++) {
					parameters2[j] = r.nextGaussian() * noise + parameters[j];
				}

				fillvalues(values, parameters2, nSegments);
				w = problem.evaluateApproximation(values);
				if (w > v /* || r.nextDouble() * w / v > 0.1 */) {
					// better. take it
					System.out.println("taking ... " + ntakes + " " + w);
					copyArray(parameters, parameters2);
					writeArray(values, dumpFile);
					if (w > v) {
						// refine
						//noise /= 1.1;
						ntakes++;
					}
					notakes = 0;
					v = w;
				} else {
					// increase freedom
					//noise *= 1.01;
					notakes++;
				}
				/*if (noise < 1e-6 || noise > 1e6) {
					noise = 1;
					ntakes++;
				}*/
			}
			writeArray(values, dumpFile);
			if (nSegments > n / 2)
				break;
			if (nSegments == n - 1)
				break;
			nSegments = nSegments * 2;
			if (nSegments >= n)
				nSegments = n - 1;
		}

	}

	private void writeArray(double[] a, File dumpFile) {
		try {
			FileWriter fw = new FileWriter(dumpFile);
			for (int i = 0; i < a.length; i++)
				fw.append(a[i] + "\n");
			fw.close();
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * copies array b onto array a
	 * 
	 * @param a
	 * @param b
	 */
	private void copyArray(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++)
			a[i] = b[i];
	}

	/**
	 * equal spaced linear interpolation
	 * 
	 * @param values
	 *            array to fill
	 * @param parameters
	 *            values to use
	 * @param nSegments
	 *            how many segments we have
	 */
	private void fillvalues(double[] values, double[] parameters, int nSegments) {

		for (int i = 0; i < nSegments; i++) {

			int leftIndex = values.length * i / nSegments;
			double leftValue = parameters[i];
			int rightIndex = values.length * (i + 1) / nSegments;
			double rightValue = parameters[i + 1];

			for (int j = leftIndex; j < rightIndex; j++) {
				values[j] = (rightValue - leftValue) * (j - leftIndex)
						/ (rightIndex - leftIndex) + leftValue;
				parameters[j] = values[j];
				// System.out.print(values[j] + " ");
			}
			// System.out.println(leftIndex + "," + rightIndex);
		}
	}

	public static void main(String[] args) {
		AnnealingFunctionSolver afs = new AnnealingFunctionSolver(
				new SimpleFunctionProblem());
		afs.solve();
	}
}
