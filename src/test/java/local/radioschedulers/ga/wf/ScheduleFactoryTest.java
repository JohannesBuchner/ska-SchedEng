package local.radioschedulers.ga.wf;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.ga.watchmaker.ScheduleFactory;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uncommons.maths.random.MersenneTwisterRNG;

public class ScheduleFactoryTest {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleFactoryTest.class);

	private Collection<Proposal> proposals;
	private ScheduleSpace template;
	private int ndays = 10;
	private Random rng = new MersenneTwisterRNG();

	private ScheduleFactory factory;

	@Before
	public void setup() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		this.factory = new ScheduleFactory(template);
	}

	@Test
	public void testFactory() throws Exception {
		Schedule schedule1 = factory.generateRandomCandidate(rng);
		assertScheduleIsWithinTemplate(schedule1, template, ndays);
	}

	public static void assertScheduleIsWithinTemplate(Schedule schedule,
			ScheduleSpace template, int ndays) {
		Assert.assertEquals("last scheduleentry ("
				+ schedule.findLastEntry().day
				+ ") should be roughly same day as schedulespace ("
				+ template.findLastEntry().day + ")",
				schedule.findLastEntry().day.intValue(), template
						.findLastEntry().day.intValue(), 1);
		Assert.assertTrue("last scheduleentry (" + schedule.findLastEntry()
				+ ") should be <= as schedulespace ("
				+ template.findLastEntry() + ")", schedule.findLastEntry()
				.isBeforeOrEqual(template.findLastEntry()));
		Assert.assertEquals(schedule.findLastEntry().day.intValue(), ndays, 1);

		for (Entry<LSTTime, JobCombination> e : schedule) {
			LSTTime t = e.getKey();
			JobCombination jc = schedule.get(t);
			Set<JobCombination> jcs = template.get(t);

			if (jc != null && !jcs.contains(jc)) {
				System.out.println(jc + " should be in " + jcs);
			}
			if (jc != null)
				Assert
						.assertTrue("@ " + t + ", " + jc + " should be in " + jcs, jcs
								.contains(jc));
		}
		for (Entry<LSTTime, Set<JobCombination>> e : template) {
			LSTTime t = e.getKey();
			JobCombination jc = schedule.get(t);
			Set<JobCombination> jcs = template.get(t);

			if (jc != null)
				Assert
						.assertTrue(jc + " should be in " + jcs, jcs
								.contains(jc));
		}
	}

}
