/**
 * 
 */
package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.Random;

public class OnceTrueRandomMock extends Random {
	private static final long serialVersionUID = 1L;
	private int i = 0;
	private int positive = 1;

	public OnceTrueRandomMock(int positive) {
		setPositive(positive);
	}

	@Override
	public double nextDouble() {
		i++;
		return (i == positive ? 0. : 1.);
	}

	public void setPositive(int positive) {
		this.positive = positive;
	}
	
	public int getPositive() {
		return positive;
	}
}