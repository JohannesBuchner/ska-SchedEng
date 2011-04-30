package local.radioschedulers;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;

import local.radioschedulers.alg.serial.FirstSelector;
import local.radioschedulers.alg.serial.SerialListingScheduler;
import local.radioschedulers.exporter.ExportFactory;
import local.radioschedulers.exporter.IExport;
import local.radioschedulers.importer.GeneratingProposalReader;
import local.radioschedulers.preschedule.ITimelineGenerator;
import local.radioschedulers.preschedule.SimpleTimelineGenerator;
import local.radioschedulers.preschedule.SingleRequirementGuard;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonScheduleSpaceConverterTest {

	private static Logger log = Logger
			.getLogger(JsonScheduleSpaceConverterTest.class);

	private ScheduleSpace template;
	public static int ndays = 10;
	private File f = new File("testexport.html");

	@Before
	public void setUp() throws Exception {
		GeneratingProposalReader gpr = new GeneratingProposalReader();
		gpr.fill();
		Collection<Proposal> proposals = gpr.readall();
		Assert.assertTrue(proposals.size() > 0);
		ITimelineGenerator tlg = new SimpleTimelineGenerator(
				new SingleRequirementGuard());
		template = tlg.schedule(proposals, ndays);
		Assert.assertTrue(template.findLastEntry().day > 2);
	}

	@Test
	public void testExportToJson() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		StringWriter sw = new StringWriter();
		mapper.writeValue(sw, template);
		StringBuffer s = sw.getBuffer();
		log.debug(s);
		// mapper.registerModule(getLSTModule());
		ScheduleSpace space = mapper.readValue(s.toString(),
				ScheduleSpace.class);
		Assert.assertEquals(template.getPossibles().size(), space.getPossibles()
				.size());
	}

	@Test
	public void testExportToHtml() throws Exception {
		IScheduler scheduler = new SerialListingScheduler(new FirstSelector());
		IExport export = ExportFactory.getHtmlExport(f, "test schedule");

		Schedule s = scheduler.schedule(template);
		export.export(s);
		Assert.assertTrue(f.exists());
	}

	@After
	public void tearDown() throws Exception {
		// f.delete();
	}

}
