package local.radioschedulers.alg.ga.watchmaker.op;

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
import local.radioschedulers.alg.ga.watchmaker.ScheduleFactoryTest;
import local.radioschedulers.alg.ga.watchmaker.op.ScheduleCrossover;
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

public class OperatorNoopTest {
	private static final Probability NOOP_PROBABILITY = new Probability(0.);
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(OperatorNoopTest.class);

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
		apply(new ScheduleExchangeMutation(template, NOOP_PROBABILITY));
	}

	@Test
	public void testMutation() throws Exception {
		apply(new ScheduleMutation(template, NOOP_PROBABILITY));
	}

	@Test
	public void testCrossover() throws Exception {
		apply(new ScheduleCrossover(1, NOOP_PROBABILITY));
	}

	@Test
	public void testSimilarMutation() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template, NOOP_PROBABILITY);
		op.setBackwardsKeep(true);
		op.setForwardsKeep(true);
		apply(op);
	}


	@Test
	public void testSimilarPrevMutation() throws Exception {
		apply(new ScheduleSimilarPrevMutation(template, NOOP_PROBABILITY, false));
	}


	@Test
	public void testKeepingMutation() throws Exception {
		apply(new ScheduleKeepingMutation(template, NOOP_PROBABILITY));
	}


	public void apply(EvolutionaryOperator<Schedule> op) {

		List<Schedule> schedules = new ArrayList<Schedule>(1);
		schedules.add(schedule1);

		schedules = op.apply(schedules, rng);
		schedule2 = schedules.get(0);

		Assert.assertEquals(schedule2.findLastEntry(), schedule1
				.findLastEntry());

		for (Entry<LSTTime, JobCombination> e : schedule1) {
			LSTTime t = e.getKey();
			JobCombination scheduleJc1 = schedule1.get(t);
			JobCombination scheduleJc2 = schedule2.get(t);

			Assert.assertTrue(schedEquals(scheduleJc1, scheduleJc2));
		}

		ScheduleFactoryTest.assertScheduleIsWithinTemplate(schedule2, template,
				ndays);
	}

	private boolean schedEquals(JobCombination a, JobCombination b) {
		return ScheduleCrossoverTest.schedEquals(a, b);
	}

}
