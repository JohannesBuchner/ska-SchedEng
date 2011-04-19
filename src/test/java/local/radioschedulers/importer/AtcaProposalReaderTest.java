package local.radioschedulers.importer;

import java.io.File;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;
import local.radioschedulers.GoodBadDateRangeRequirements;
import local.radioschedulers.Job;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.Proposal;

import org.junit.Before;
import org.junit.Test;

public class AtcaProposalReaderTest {

	private File f = new File(
			"/home/user/Downloads/ata-proposals/mp-johannes.txt");
	private AtcaProposalReader reader;
	private int ndays = 182;

	private Date str2date(String s) throws ParseException {
		return SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).parse(
				s + "/" + new Date().getYear());
	}

	@Before
	public void setUp() throws Exception {
		reader = new AtcaProposalReader(f, new Date(2011 - 1900, 1, 4), ndays );
	}

	@Test
	public void readTest() throws Exception {
		Collection<Proposal> proposals = reader.readall();
		Assert.assertEquals(41, proposals.size());
		PrintStream out = new PrintStream(f.getAbsolutePath() + ".out");
		for (Proposal p : proposals) {
			out
					.println("------------------------------------------------------------------------");
			out.println("Project id: " + p.name);
			String h = "";
			String s = "";
			String lst = "";
			String g = "";
			String b = "";
			for (Job j1 : p.jobs) {
				h += j1.hours + "; ";
				s += j1.id + "; ";
				lst += "lst(" + formatLSTHour(j1.lstmin) + "," + formatLSTHour(j1.lstmax) + ")"
						+ "; ";
				JobWithResources jwr = (JobWithResources) j1;
				GoodBadDateRangeRequirements d = (GoodBadDateRangeRequirements) jwr.date;
				b += d.getBad() + "; ";
				g += d.getGood() + "; ";
			}
			out.println("Requested time: " + h.substring(0, h.length() - 2));
			out.println("Band: 3mm");
			out.println("Source: " + s.substring(0, s.length() - 2));
			out.println("RADEC: " + lst.substring(0, lst.length() - 2));
			out.println("Good Dates: " + g.substring(0, g.length() - 2));
			out.println("Bad Dates: " + b.substring(0, b.length() - 2));
			out.println("Grade: " + p.priority);
			out.println("Comments:");
		}
		out
				.println("------------------------------------------------------------------------");
	}

	private String formatLSTHour(Double lstmin) {
		long h = (long) Math.floor(lstmin);
		long m = Math.round((lstmin - h) * 60);
		return h + ":" + m;
	}

}
