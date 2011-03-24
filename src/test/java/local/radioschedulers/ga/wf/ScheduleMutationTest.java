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
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.ga.watchmaker.ScheduleMutation;
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

public class ScheduleMutationTest {
	private static final Probability MUTATION_PROBABILITY = new Probability(0.1);

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleMutationTest.class);

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
				new RandomizedSelector());
		schedule1 = scheduler.schedule(template);
	}

	@Test
	public void testMutation() throws Exception {
		List<Schedule> schedules = new ArrayList<Schedule>(2);
		schedules.add(schedule1);

		ScheduleMutation op = new ScheduleMutation(template,
				MUTATION_PROBABILITY);
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
		Assert.assertTrue(diffcount > 0);
		Assert.assertEquals(diffcount * 1. / (eqcount + diffcount),
				MUTATION_PROBABILITY.doubleValue(), 0.05);

		ScheduleFactoryTest.assertScheduleIsWithinTemplate(schedule2, template,
				ndays);

	}

	private boolean schedEquals(JobCombination a, JobCombination b) {
		return ScheduleCrossoverTest.schedEquals(a, b);
	}

}
