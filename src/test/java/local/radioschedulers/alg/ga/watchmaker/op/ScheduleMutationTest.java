package local.radioschedulers.alg.ga.watchmaker.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.alg.ga.watchmaker.ScheduleFactoryTest;
import local.radioschedulers.alg.serial.PrioritizedSelector;
import local.radioschedulers.alg.serial.SerialListingScheduler;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.uncommons.maths.random.Probability;

public class ScheduleMutationTest {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ScheduleMutationTest.class);

	protected Collection<Proposal> proposals;
	protected ScheduleSpace template;
	private int ndays = 10;
	private Schedule schedule1;
	private Schedule schedule2;
	protected OnceTrueRandomMock rng;

	protected AbstractScheduleMutation op;

	protected Probability mutationProbability = new Probability(0.1);

	@Before
	public void setup() throws Exception {
		rng = new OnceTrueRandomMock(1);
		rng.setPositive(1);

		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new SingleRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		SerialListingScheduler scheduler = new SerialListingScheduler(
				new PrioritizedSelector());
		schedule1 = scheduler.schedule(template);

		op = getOperator();
	}

	@Test
	public void testMutation() throws Exception {
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
		Assert.assertTrue(diffcount > 0);

		ScheduleFactoryTest.assertScheduleIsWithinTemplate(schedule2, template,
				ndays);

	}

	protected AbstractScheduleMutation getOperator() {
		ScheduleMutation op = new ScheduleMutation(template,
				mutationProbability);
		return op;
	}

	private boolean schedEquals(JobCombination a, JobCombination b) {
		return ScheduleCrossoverTest.schedEquals(a, b);
	}

}
