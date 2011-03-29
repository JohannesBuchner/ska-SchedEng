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
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.ga.watchmaker.ScheduleSimilarMutation;
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

public class ScheduleSimilarMutationTest {
	private static final Probability MUTATION_PROBABILITY = new Probability(0.1);

	private static Logger log = Logger
			.getLogger(ScheduleSimilarMutationTest.class);

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
		CPULikeScheduler scheduler = new CPULikeScheduler(
				new PrioritizedSelector());
		schedule1 = scheduler.schedule(template);
	}

	@Test
	public void testBoth() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template,
				MUTATION_PROBABILITY);
		op.setBackwardsKeep(true);
		op.setForwardsKeep(true);
		testMutation(op);
	}

	@Test
	public void testForward() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template,
				MUTATION_PROBABILITY);
		op.setBackwardsKeep(false);
		op.setForwardsKeep(true);
		testMutation(op);
	}

	@Test
	public void testBackward() throws Exception {
		ScheduleSimilarMutation op = new ScheduleSimilarMutation(template,
				MUTATION_PROBABILITY);
		op.setBackwardsKeep(true);
		op.setForwardsKeep(false);
		testMutation(op);
	}

	public void testMutation(ScheduleSimilarMutation op) throws Exception {
		List<Schedule> schedules = new ArrayList<Schedule>(2);
		schedules.add(schedule1);

		schedules = op.apply(schedules, rng);
		schedule2 = schedules.get(0);

		Assert.assertEquals(schedule2.findLastEntry(), schedule1
				.findLastEntry());

		int diffcount = 0;
		int eqcount = 0;
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
		ScheduleFactoryTest.assertScheduleIsWithinTemplate(schedule2, template,
				ndays);
		Assert.assertTrue("something should have been changed", diffcount > 0);
		log.debug("mutated parts " + eqcount + " vs " + diffcount + " --> "
				+ diffcount * 1. / (eqcount + diffcount));
		Assert.assertEquals(MUTATION_PROBABILITY.doubleValue(), diffcount * 1.
				/ (eqcount + diffcount), 0.1);

	}

	private boolean schedEquals(JobCombination a, JobCombination b) {
		return ScheduleCrossoverTest.schedEquals(a, b);
	}

}
