package local.radioschedulers;

import junit.framework.Assert;
import local.radioschedulers.preschedule.date.SolarDateRangeRequirements;

import org.junit.Before;
import org.junit.Test;

public class SolarDateRangeRequirementsTest {

	private SolarDateRangeRequirements drr;
	private int starttime = 8 * 60;
	private int ndays = 30;
	private int endtime = 12 * 60;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRequires() {
		drr = new SolarDateRangeRequirements(new LSTTime(0, starttime),
				new LSTTime(ndays, endtime), null, null);
		int count = countEnabled();
		Assert.assertEquals((endtime - starttime) / Schedule.LST_SLOTS_MINUTES
				* ndays, count);
	}

	@Test
	public void testRequiresInverted() {
		drr = new SolarDateRangeRequirements(new LSTTime(0, endtime),
				new LSTTime(ndays, starttime), null, null);
		int count = countEnabled();
		Assert.assertEquals(Math.abs(24 * 60 - (endtime - starttime))
				/ Schedule.LST_SLOTS_MINUTES * ndays, count, 2);
	}

	@Test
	public void testRequiresWednesdays() {
		drr = new SolarDateRangeRequirements(new LSTTime(0, starttime),
				new LSTTime(ndays, endtime), 7, 3);
		int count = countEnabled();
		Assert.assertEquals((endtime - starttime) / Schedule.LST_SLOTS_MINUTES
				* ndays / 7, count, 5);
	}

	private int countEnabled() {
		int count = 0;
		for (LSTTimeIterator it = new LSTTimeIterator(new LSTTime(ndays, 0),
				Schedule.LST_SLOTS_MINUTES); it.hasNext();) {
			LSTTime t = it.next();
			Double v = drr.requires(t);
			if (v != 0)
				count++;
			// log.debug("@" + t + " -- " + v);
		}
		return count;
	}
}
