package local.radioschedulers;

import java.util.Collection;

import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;

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
		tlg = new SimpleTimelineGenerator(ndays, new SingleRequirementGuard());
	}

	@Test
	public void testSchedule() throws Exception {
		ScheduleSpace template = tlg.schedule(proposals, ndays);
		log.debug("last entry:" + template.getLastEntry());
		Assert.assertTrue(template.getLastEntry().day >= ndays - 1);
	}
}
