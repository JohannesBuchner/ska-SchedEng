package local.radioschedulers;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class SolarDateRangeRequirementsTest {

	private SolarDateRangeRequirements drr;
	private int starttime = 8 * Schedule.LST_SLOTS_MINUTES;
	private int ndays = 30;
	private int endtime = 12 * Schedule.LST_SLOTS_MINUTES;

	@Before
	public void setUp() throws Exception {
		drr = new SolarDateRangeRequirements(new LSTTime(0, starttime),
				new LSTTime(ndays, endtime));
	}

	@Test
	public void testRequires() {
		int count = 0;
		for (int i = 0; i < ndays; i++) {
			for (int k = 0; k < Schedule.LST_SLOTS_PER_DAY; k++) {
				LSTTime t = new LSTTime(i, k);
				Double v = drr.requires(t);
				if (v != 0)
					count++;
				System.out.println("@day:" + i + "-slot:" + k + " -- " + v);
			}
			System.out.println();
		}
		Assert.assertEquals((endtime - starttime) * ndays, count);
	}
}
