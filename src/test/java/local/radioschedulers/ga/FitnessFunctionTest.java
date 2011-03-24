package local.radioschedulers.ga;

import java.util.Collection;

import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.cpu.CPULikeScheduler;
import local.radioschedulers.cpu.FirstSelector;
import local.radioschedulers.ga.fitness.SimpleScheduleFitnessFunction;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.parallel.ParallelRequirementGuard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FitnessFunctionTest {

	private Collection<Proposal> proposals;
	private ScheduleSpace template;
	private int ndays = 10;
	private Schedule schedule1;
	private Schedule emptyschedule;

	@Before
	public void setup() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new ParallelRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		CPULikeScheduler scheduler = new CPULikeScheduler(new FirstSelector());
		schedule1 = scheduler.schedule(template);
		emptyschedule = new Schedule();
	}

	@Test
	public void testSimpleFitnessFuntion() throws Exception {
		ScheduleFitnessFunction fitness = new SimpleScheduleFitnessFunction();
		double v = fitness.evaluate(schedule1);
		double w = fitness.evaluate(emptyschedule);
		Assert.assertTrue("Expecting schedule1: " + v + " > schedule2: " + w,
				v > w);
		Assert.assertEquals(1, w, 0.01);
	}

}
