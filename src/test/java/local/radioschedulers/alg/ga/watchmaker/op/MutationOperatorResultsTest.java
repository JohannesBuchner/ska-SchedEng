package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import junit.framework.Assert;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.SmallTestScenario;
import local.radioschedulers.TestScenario;
import local.radioschedulers.alg.serial.IdSelector;
import local.radioschedulers.alg.serial.SerialListingScheduler;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

public class MutationOperatorResultsTest {
	private static Logger log = Logger
			.getLogger(MutationOperatorResultsTest.class);

	private ScheduleSpace space;

	private OnceTrueRandomMock rng;
	private Probability p = new Probability(0.01);
	private Schedule schedule;

	@Test
	public void testMockProbability() {
		rng = new OnceTrueRandomMock(5);
		for (int i = 0; i < 10; i++) {
			Assert.assertEquals("at " + i, i + 1 == rng.getPositive(), p.nextEvent(rng));
		}
	}

	@Before
	public void setUp() throws Exception {
		TestScenario ts = new SmallTestScenario();
		space = ts.getSpace();
		SerialListingScheduler s = new SerialListingScheduler(new IdSelector());
		schedule = s.schedule(space);
		rng = new OnceTrueRandomMock(1);
	}

	@Test
	public void testMutation() throws Exception {
		rng = new OnceTrueRandomMock(5);
		Schedule s = apply(new ScheduleMutation(space, p));

		int pos = 0;
		int i = 0;
		for (Entry<LSTTime, JobCombination> e : schedule) {
			i++;
			LSTTime t = e.getKey();
			log.debug(i + "@" + t + " " + e.getValue() + " -- " + s.get(t));
			// before mutation
			if (pos == 0) {
				if (i < rng.getPositive()) {
					Assert.assertEquals(e.getValue(), s.get(t));
				}
				if (!same(e.getValue(), s.get(t))) {
					pos = 1;
					Assert.assertTrue(i + " < 10", i < 10);
				}
			} else {
				// after mutation
				if (pos == 1) {
					Assert.assertEquals(e.getValue(), s.get(t));
				}
			}
		}
		Assert.assertEquals(1, pos);
	}

	private boolean same(JobCombination a, JobCombination b) {
		return ScheduleCrossoverTest.schedEquals(a, b);
	}

	@Test
	public void testSimilarPrevMutation() throws Exception {
		rng = new OnceTrueRandomMock(1);
		Schedule s = apply(new ScheduleSimilarPrevMutation(space, p, false));

		int i = 0;
		for (Entry<LSTTime, JobCombination> e : schedule) {
			LSTTime t = e.getKey();
			log.debug("@" + t + " " + e.getValue() + " -- " + s.get(t));
			// before mutation
			if (i < rng.getPositive()) {
				Assert.assertEquals(e.getValue(), s.get(t));
			} else if (i > rng.getPositive()) {
				Assert.assertEquals(e.getValue(), s.get(t));
			} else {
				// Assert.assertFalse(e.getValue().equals(s.get(t)));
			}
			i++;
		}
	}

	@Test
	public void testSimilarMutationMutFw() throws Exception {
		testSimilarMutation(true, true, false);
	}

	@Test
	public void testSimilarMutationMutBw() throws Exception {
		testSimilarMutation(true, false, true);
	}

	@Test
	public void testSimilarMutationMutBoth() throws Exception {
		testSimilarMutation(true, true, true);
	}

	@Test
	public void testSimilarMutationFw() throws Exception {
		testSimilarMutation(false, true, false);
	}

	@Test
	public void testSimilarMutationBw() throws Exception {
		testSimilarMutation(false, false, true);
	}

	@Test
	public void testSimilarMutationBoth() throws Exception {
		testSimilarMutation(false, true, true);
	}

	public void testSimilarMutation(boolean mut, boolean fw, boolean bw)
			throws Exception {
		rng = new OnceTrueRandomMock(4);
		LSTTime tbefore = new LSTTime(0, (rng.getPositive() - 1 - 1)
				* Schedule.LST_SLOTS_MINUTES);
		LSTTime tcenter = new LSTTime(0, (rng.getPositive() - 1)
				* Schedule.LST_SLOTS_MINUTES);
		LSTTime tafter = new LSTTime(0, (rng.getPositive() - 1 + 1)
				* Schedule.LST_SLOTS_MINUTES);

		ScheduleSimilarMutation op;
		if (mut) {
			op = new ScheduleKeepingMutation(space, p);
			op.disableNormalization();
		} else {
			JobCombination myjc = schedule.get(new LSTTime(0,
					9 * Schedule.LST_SLOTS_MINUTES));
			schedule.add(tcenter, myjc);

			op = new ScheduleSimilarMutation(space, p);
			op.disableNormalization();
		}
		op.setBackwardsKeep(bw);
		op.setForwardsKeep(fw);
		Schedule s = apply(op);

		int i = 0;
		for (Entry<LSTTime, JobCombination> e : schedule) {
			i++;
			LSTTime t = e.getKey();
			log.debug(i + "@" + t + " " + e.getValue() + " -- " + s.get(t));
		}

		JobCombination oldjc = s.get(tcenter);
		JobCombination newjc = schedule.get(tcenter);
		Assert.assertTrue("should have mutated at " + tcenter, mut
				^ oldjc.equals(newjc));
		oldjc = s.get(tbefore);
		newjc = schedule.get(tbefore);
		Assert.assertTrue(bw ^ oldjc.equals(newjc));
		oldjc = s.get(tafter);
		newjc = schedule.get(tafter);
		Assert.assertTrue(fw ^ oldjc.equals(newjc));
	}

	@Test
	public void testJobPlacementMutation() throws Exception {
		rng = new OnceTrueRandomMock(1);
		Schedule s = apply(new ScheduleJobPlacementMutation(space, p));

		int i = 0;
		for (Entry<LSTTime, JobCombination> e : schedule) {
			i++;
			LSTTime t = e.getKey();
			log.debug(i + "@" + t + " " + e.getValue() + " -- " + s.get(t));
			if (i > 6 && i < 12) {
				Assert.assertEquals(e.getValue(), s.get(t));
			} else {
				Assert.assertFalse(same(e.getValue(), (s.get(t))));
			}
		}
		/*
		 * // before mutation if (i < positive) {
		 * Assert.assertEquals(e.getValue(), s.get(t)); } else if (i > positive)
		 * { Assert.assertEquals(e.getValue(), s.get(t)); } else { //
		 * Assert.assertFalse(e.getValue().equals(s.get(t))); } i++; }
		 */
	}

	private Schedule apply(EvolutionaryOperator<Schedule> op) {
		List<Schedule> schedules = new ArrayList<Schedule>();
		schedules.add(schedule);
		schedules = op.apply(schedules, rng);
		return schedules.get(0);
	}

}
