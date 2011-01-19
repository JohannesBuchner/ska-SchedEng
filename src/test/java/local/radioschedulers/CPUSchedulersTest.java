package local.radioschedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FairPrioritizedSelector;
import local.radioschedulers.cpu.FirstSelector;
import local.radioschedulers.cpu.PrioritizedSelector;
import local.radioschedulers.cpu.RandomizedSelector;
import local.radioschedulers.cpu.ShortestFirstSelector;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CPUSchedulersTest {

	private static Logger log = Logger.getLogger(CPUSchedulersTest.class);

	public static int ndays = 10;

	private ScheduleSpace template;
	Collection<Proposal> proposals;

	@Before
	public void setUp() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new SingleRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		for (Entry<LSTTime, Set<JobCombination>> e : template) {
			Assert.assertFalse("Combinations at " + e.getKey(), e.getValue()
					.isEmpty());
		}
	}

	@Test
	public void testSingleFirst() throws Exception {
		CPULikeScheduler scheduler = new CPULikeScheduler(new FirstSelector(),
				new SingleRequirementGuard());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSingleFair() throws Exception {
		CPULikeScheduler scheduler = new CPULikeScheduler(
				new FairPrioritizedSelector(), new SingleRequirementGuard());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSinglePrio() throws Exception {
		CPULikeScheduler scheduler = new CPULikeScheduler(
				new PrioritizedSelector(), new SingleRequirementGuard());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSingleShortest() throws Exception {
		CPULikeScheduler scheduler = new CPULikeScheduler(
				new ShortestFirstSelector(), new SingleRequirementGuard());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSingleRand() throws Exception {
		CPULikeScheduler scheduler = new CPULikeScheduler(
				new RandomizedSelector(), new SingleRequirementGuard());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testAllParallel() throws Exception {
		RequirementGuard req = new ParallelRequirementGuard();
		List<IScheduler> schedulers = new ArrayList<IScheduler>();

		schedulers.add(new CPULikeScheduler(new FairPrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new PrioritizedSelector(),
				new ParallelRequirementGuard()));
		schedulers.add(new CPULikeScheduler(new ShortestFirstSelector(),
				new ParallelRequirementGuard()));

		CPULikeScheduler rand = new CPULikeScheduler(new RandomizedSelector(),
				req);
		schedulers.add(rand);

		for (IScheduler s : schedulers) {
			log.debug("scheduling using " + s);

			Schedule schedule = s.schedule(template);
			checkSchedule(schedule);
		}
	}

	private void checkSchedule(Schedule s) {
		Assert.assertEquals("ScheduleSpace and Schedule have the same length",
				template.findLastEntry().day, s.findLastEntry().day);
		int i = 0;
		Set<Proposal> scheduledJobs = new HashSet<Proposal>();
		// Test correctness
		for (Entry<LSTTime, JobCombination> e : s) {
			Set<JobCombination> ref = template.get(e.getKey());
			JobCombination actual = e.getValue();
			Assert.assertTrue("Schedule is outside ScheduleSpace at " + i, ref
					.contains(actual));
			if (actual.jobs.size() > 0)
				i++;
			for (Job j : actual.jobs) {
				scheduledJobs.add(j.proposal);
			}
		}

		// Test if it is any good
		Assert.assertTrue("Job slots filled: " + i, i > ndays);
		Assert.assertTrue("Jobs scheduled: " + scheduledJobs.size() + " of "
				+ proposals.size(), scheduledJobs.size() > 1);
	}
}
