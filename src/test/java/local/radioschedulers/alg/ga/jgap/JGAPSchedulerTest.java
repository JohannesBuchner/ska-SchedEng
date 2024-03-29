package local.radioschedulers.alg.ga.jgap;

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
import local.radioschedulers.alg.ga.GeneticAlgorithmScheduler;
import local.radioschedulers.alg.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.alg.ga.jgap.JGAPScheduler;
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

public class JGAPSchedulerTest {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(JGAPSchedulerTest.class);

	public static int ndays = 10;

	private ScheduleSpace template;
	private Collection<Proposal> proposals;
	private GeneticAlgorithmScheduler scheduler;

	@Before
	public void setUp() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		scheduler = new JGAPScheduler(new SimpleScheduleFitnessFunction());
		scheduler.setPopulationSize(10);
		scheduler.setNumberOfGenerations(10);
	}

	@Test
	public void testGA() throws Exception {
		Schedule s = scheduler.schedule(template);

		Assert.assertNotNull(s);

		Assert.assertEquals("ScheduleSpace and Schedule have the same length",
				template.findLastEntry().day, s.findLastEntry().day);
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
		SerialListingScheduler s = new SerialListingScheduler(new FirstSelector());
		schedules.add(s.schedule(template));
		s = new SerialListingScheduler(new RandomizedSelector());
		schedules.add(s.schedule(template));
		scheduler.setPopulation(schedules);
		testGA();
	}
}
