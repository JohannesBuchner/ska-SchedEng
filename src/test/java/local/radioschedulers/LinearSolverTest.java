package local.radioschedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.alg.lp.ParallelLinearScheduler;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LinearSolverTest {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(LinearSolverTest.class);

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
	public void testSingle() throws Exception {
		ParallelLinearScheduler ls = new ParallelLinearScheduler();
		Schedule s = ls.schedule(template);

		Assert.assertEquals("ScheduleSpace and Schedule have the same length",
				template.findLastEntry().day, s.findLastEntry().day);
		int i = 0;
		Set<Proposal> scheduledJobs = new HashSet<Proposal>();
		// Test correctness
		for (Entry<LSTTime, JobCombination> e : s) {
			Set<JobCombination> ref = template.get(e.getKey());
			JobCombination actual = e.getValue();
			if (actual == null)
				continue;
			Assert.assertTrue("Schedule is outside ScheduleSpace at " + i
					+ "-- " + actual + " not in " + ref, ref.contains(actual));
			Assert.assertTrue(actual.jobs.size() > 0);
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
