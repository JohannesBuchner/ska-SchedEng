package local.radioschedulers.alg.serial;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.serial.FairPrioritizedSelector;
import local.radioschedulers.alg.serial.FirstSelector;
import local.radioschedulers.alg.serial.PrioritizedSelector;
import local.radioschedulers.alg.serial.RandomizedSelector;
import local.radioschedulers.alg.serial.SerialListingScheduler;
import local.radioschedulers.alg.serial.ShortestFirstSelector;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.RequirementGuard;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SerialListingSchedulersTest {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(SerialListingSchedulersTest.class);

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
				getRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		for (Entry<LSTTime, Set<JobCombination>> e : template) {
			Assert.assertFalse("Combinations at " + e.getKey(), e.getValue()
					.isEmpty());
		}
	}

	protected RequirementGuard getRequirementGuard() {
		return new SingleRequirementGuard();
	}

	@Test
	public void testSingleFirst() throws Exception {
		SerialListingScheduler scheduler = new SerialListingScheduler(new FirstSelector());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSingleFair() throws Exception {
		SerialListingScheduler scheduler = new SerialListingScheduler(
				new FairPrioritizedSelector());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSinglePrio() throws Exception {
		SerialListingScheduler scheduler = new SerialListingScheduler(
				new PrioritizedSelector());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSingleShortest() throws Exception {
		SerialListingScheduler scheduler = new SerialListingScheduler(
				new ShortestFirstSelector());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	@Test
	public void testSingleRand() throws Exception {
		SerialListingScheduler scheduler = new SerialListingScheduler(
				new RandomizedSelector());
		Schedule s = scheduler.schedule(template);
		checkSchedule(s);
	}

	private void checkSchedule(Schedule s) {
		Assert.assertEquals("ScheduleSpace and Schedule have the same length",
				template.findLastEntry().day, s.findLastEntry().day);
		int i = 0;
		int emptyCount = 0;
		Set<Proposal> scheduledJobs = new HashSet<Proposal>();
		// Test correctness
		for (Entry<LSTTime, JobCombination> e : s) {
			Set<JobCombination> ref = template.get(e.getKey());
			JobCombination actual = e.getValue();
			if (actual == null)
				emptyCount++;
			else {
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
}
