package local.radioschedulers.alg.ga.watchmaker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.alg.ga.watchmaker.WFScheduler;
import local.radioschedulers.alg.serial.FirstSelector;
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

public class WFSchedulerTest {

	private static Logger log = Logger.getLogger(WFSchedulerTest.class);

	public static int ndays = 10;

	private ScheduleSpace template;
	private Collection<Proposal> proposals;
	private WFScheduler scheduler;

	@Before
	public void setUp() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		scheduler = new WFScheduler(new SimpleScheduleFitnessFunction());
		scheduler.setCrossoverDays(2);
		scheduler.setCrossoverProbability(0.1);
		scheduler.setDoubleCrossoverProbability(0.1);
		scheduler.setMutationExchangeProbability(0.1);
		scheduler.setMutationJobPlacementProbability(0.1);
		scheduler.setMutationKeepingProbability(0.1);
		scheduler.setMutationProbability(0.1);
		scheduler.setMutationSimilarBackwardsProbability(0.1);
		scheduler.setMutationSimilarForwardsProbability(0.1);
		scheduler.setMutationSimilarPrevProbability(0.1);
		scheduler.setPopulationSize(10);
		scheduler.setNumberOfGenerations(10);
	}

	@Test
	public void testops() throws Exception {
		for (int j = 0; j < 8; j++) {
			int i = 1 << j;
			log.debug("testing individual op: " + i);
			scheduler.setCrossoverProbability(((i & 1) != 0) ? 0.1 : 0);
			scheduler.setDoubleCrossoverProbability((i & 2) != 0 ? 0.1 : 0);
			scheduler.setMutationExchangeProbability((i & 4) != 0 ? 0.1 : 0);
			scheduler
					.setMutationJobPlacementProbability((i & 8) != 0 ? 0.1 : 0);
			scheduler.setMutationKeepingProbability((i & 16) != 0 ? 0.1 : 0);
			scheduler.setMutationProbability((i & 32) != 0 ? 0.1 : 0);
			scheduler
					.setMutationSimilarBackwardsProbability((i & 64) != 0 ? 0.1
							: 0);
			scheduler
					.setMutationSimilarForwardsProbability((i & 128) != 0 ? 0.1
							: 0);
			scheduler.setMutationSimilarPrevProbability((i & 256) != 0 ? 0.1
					: 0);
			testGA();
		}
	}

	@Test
	public void testGA() throws Exception {
		Schedule s = scheduler.schedule(template);

		Assert.assertNotNull(s);

		ScheduleFactoryTest.assertScheduleIsWithinTemplate(s, template, ndays);
		int i = 0;
		Set<Proposal> scheduledJobs = new HashSet<Proposal>();
		// Test correctness
		for (Entry<LSTTime, JobCombination> e : s) {
			Set<JobCombination> ref = template.get(e.getKey());
			JobCombination actual = e.getValue();
			if (actual != null) {
				Assert.assertTrue("Schedule is outside ScheduleSpace at " + i,
						ref.contains(actual));
				i++;
				for (Job j : actual.jobs) {
					scheduledJobs.add(j.proposal);
				}
			}
		}

		// Test if it is any good
		Assert.assertTrue("Job slots filled: " + i, i > ndays);
		Assert.assertTrue("Jobs scheduled: " + scheduledJobs.size() + " of "
				+ proposals.size(), scheduledJobs.size() > 1);
	}

	@Test
	public void testWithInitialPopulation() throws Exception {
		List<Schedule> schedules = new ArrayList<Schedule>();
		SerialListingScheduler s = new SerialListingScheduler(
				new FirstSelector());
		schedules.add(s.schedule(template));
		s = new SerialListingScheduler(new RandomizedSelector());
		schedules.add(s.schedule(template));
		scheduler.setPopulation(schedules);
		testGA();
	}
}
