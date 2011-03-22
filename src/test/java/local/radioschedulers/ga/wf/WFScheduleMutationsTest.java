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
import local.radioschedulers.ga.watchmaker.ScheduleCrossover;
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

public class WFScheduleMutationsTest {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(WFScheduleMutationsTest.class);

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
		schedule2 = scheduler.schedule(template);
	}

	@Test
	public void testCrossover() throws Exception {
		List<Schedule> schedules = new ArrayList<Schedule>(2);
		schedules.add(schedule1);
		schedules.add(schedule2);

		ScheduleCrossover op = new ScheduleCrossover(1, new Probability(0.1));
		schedules = op.apply(schedules, rng);

		Assert.assertEquals(schedules.get(0).findLastEntry(), schedule1
				.findLastEntry());
		Assert.assertEquals(schedules.get(1).findLastEntry(), schedule2
				.findLastEntry());
		Assert.assertEquals(schedules.get(0).findLastEntry(), schedule2
				.findLastEntry());
		Assert.assertEquals(schedules.get(1).findLastEntry(), schedule1
				.findLastEntry());

		for (Entry<LSTTime, JobCombination> e : schedule1) {
			LSTTime t = e.getKey();
			JobCombination scheduleJc1 = schedule1.get(t);
			JobCombination scheduleJc2 = schedule2.get(t);
			JobCombination scheduleJc3 = schedules.get(0).get(t);
			JobCombination scheduleJc4 = schedules.get(1).get(t);
			/*
			 * log.debug("in 1:" + scheduleJc1); log.debug("in 2:" +
			 * scheduleJc2); log.debug("out1:" + scheduleJc3); log.debug("out2:"
			 * + scheduleJc4);
			 */
			if (schedEquals(scheduleJc1, scheduleJc3)) {
				Assert.assertTrue(schedEquals(scheduleJc2, scheduleJc4));
			} else {
				Assert.assertTrue(schedEquals(scheduleJc2, scheduleJc3));
				Assert.assertTrue(schedEquals(scheduleJc1, scheduleJc4));
			}
		}
	}

	private boolean schedEquals(JobCombination a, JobCombination b) {
		return WFScheduleCrossoverTest.schedEquals(a, b);
	}

}
