package local.radioschedulers;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleTimelineGeneratorTest {

	private static Logger log = Logger
			.getLogger(SimpleTimelineGeneratorTest.class);

	private ITimelineGenerator tlg;
	Collection<Proposal> proposals;
	public static int ndays = 10;

	@Before
	public void setUp() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);

		for (Proposal p : proposals) {
			Assert.assertTrue(p.priority > 0);
			Assert.assertFalse(p.jobs.isEmpty());
		}
	}

	private void checkSchedule(ScheduleSpace template, boolean can_have_parallel) {
		log.debug("last entry:" + template.findLastEntry());
		Assert.assertTrue(template.findLastEntry().day >= ndays - 1);
		for (Entry<LSTTime, Set<JobCombination>> e : template) {
			Assert.assertFalse("No Combinations at " + e.getKey(), e.getValue()
					.isEmpty());
			int emptyCount = 0;

			for (JobCombination jc : e.getValue()) {
				if (jc.jobs.isEmpty()) {
					emptyCount++;
					Assert.assertTrue(jc.calculatePriority() == 0);
				} else {
					Assert.assertTrue(jc.calculatePriority() > 0);
					if (!can_have_parallel) {
						Assert.assertEquals(jc.jobs.size(), 1);
					}
				}
			}
			Assert.assertTrue(emptyCount == 1);
		}
	}

	@Test
	public void testSingle() throws Exception {
		tlg = new SimpleTimelineGenerator(new SingleRequirementGuard());
		ScheduleSpace template = tlg.schedule(proposals, ndays);
		checkSchedule(template, false);
	}

	@Test
	public void testParallel() throws Exception {
		tlg = new SimpleTimelineGenerator(new ParallelRequirementGuard());
		ScheduleSpace template = tlg.schedule(proposals, ndays);
		checkSchedule(template, true);
	}
}
