package local.radioschedulers.ga.wf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleExchangeMutation;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleKeepingMutation;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleMutation;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleSimilarMutation;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleSimilarPrevMutation;
import local.radioschedulers.alg.serial.RandomizedSelector;
import local.radioschedulers.alg.serial.SerialListingScheduler;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;

public class MutationPercentTest {
	private static final Probability TEST_PROBABILITY = new Probability(0.01);
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(MutationPercentTest.class);

	private Collection<Proposal> proposals;
	private ScheduleSpace template;
	private int ndays = 10;
	private Schedule schedule1;
	private Schedule schedule2;
	private Random rng = new MersenneTwisterRNG();

	@Before
	public void setup() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		SerialListingScheduler scheduler = new SerialListingScheduler(
				new RandomizedSelector());
		schedule1 = scheduler.schedule(template);
	}

	@Test
	public void testExchangeMutation() throws Exception {
		apply(new ScheduleExchangeMutation(template, TEST_PROBABILITY));
	}

	@Test
	public void testMutation() throws Exception {
		apply(new ScheduleMutation(template, TEST_PROBABILITY));
	}

	@Test
	public void testSimilarMutationBoth() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template,
				TEST_PROBABILITY);
		op.setBackwardsKeep(true);
		op.setForwardsKeep(true);
		apply(op);
	}

	@Test
	public void testSimilarMutationFw() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template,
				TEST_PROBABILITY);
		op.setBackwardsKeep(false);
		op.setForwardsKeep(true);
		apply(op);
	}

	@Test
	public void testSimilarMutationBw() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template,
				TEST_PROBABILITY);
		op.setBackwardsKeep(true);
		op.setForwardsKeep(false);
		apply(op);
	}

	@Test
	public void testSimilarPrevMutation() throws Exception {
		apply(new ScheduleSimilarPrevMutation(template, TEST_PROBABILITY));
	}

	@Test
	public void testKeepingMutation() throws Exception {
		apply(new ScheduleKeepingMutation(template, TEST_PROBABILITY));
	}

	public void apply(EvolutionaryOperator<Schedule> op) {

		int diffcount = 0;
		int eqcount = 0;

		for (int i = 0; i < 100; i++) {
			List<Schedule> origSchedules = new ArrayList<Schedule>(1);
			origSchedules.add(getCopy(schedule1));

			Assert.assertEquals(origSchedules.get(0), schedule1);
			List<Schedule> schedules = op.apply(origSchedules, rng);
			schedule2 = schedules.get(0);
			Assert.assertEquals(origSchedules.get(0), schedule1);

			Assert.assertEquals(schedule2.findLastEntry(), schedule1
					.findLastEntry());

			for (Entry<LSTTime, JobCombination> e : schedule1) {
				LSTTime t = e.getKey();
				JobCombination scheduleJc1 = schedule1.get(t);
				JobCombination scheduleJc2 = schedule2.get(t);

				if (!template.get(t).isEmpty()) {
					if (schedEquals(scheduleJc1, scheduleJc2)) {
						eqcount++;
					} else {
						diffcount++;
					}
				}
			}
		}
		Assert.assertTrue(diffcount > 0);
		Assert.assertEquals(TEST_PROBABILITY.doubleValue(), diffcount * 1.
				/ (eqcount + diffcount), 0.003);
	}

	private static Schedule getCopy(Schedule orig) {
		Schedule s = new Schedule();
		Schedule s2 = new Schedule();
		for (Entry<LSTTime, JobCombination> e : orig) {
			if (e.getValue() == null)
				continue;
			s.add(e.getKey(), e.getValue());
			s2.add(e.getKey(), e.getValue());
		}
		Assert.assertEquals(s2, s);
		for (Entry<LSTTime, JobCombination> e : s) {
			if (e.getValue() == null)
				continue;
			Assert.assertEquals(orig.get(e.getKey()), s.get(e.getKey()));
		}
		Assert.assertEquals(orig, s);
		return s;
	}

	private boolean schedEquals(JobCombination a, JobCombination b) {
		return ScheduleCrossoverTest.schedEquals(a, b);
	}

}
