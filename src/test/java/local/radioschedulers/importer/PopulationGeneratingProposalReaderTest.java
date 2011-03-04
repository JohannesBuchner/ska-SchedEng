package local.radioschedulers.importer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

import org.junit.Before;
import org.junit.Test;

public class PopulationGeneratingProposalReaderTest {

	private PopulationGeneratingProposalReader reader;

	@Before
	public void setUp() throws Exception {
		reader = new PopulationGeneratingProposalReader();
		reader.fill(10000);
	}

	@Test
	public void testConvergence() throws Exception {
		Collection<Proposal> proposals = reader.readall();
		Collection<Job> jobs = getallJobs(proposals);
		Histogram<Integer> hourhist = new Histogram<Integer>();
		Histogram<Double> durationhist = new Histogram<Double>();

		calculateDurationBins(durationhist);
		calculateHourBins(hourhist);
		// hourhist.addBin(lastborder, 10000);
		for (Job j : jobs) {
			Assert.assertNotNull(j.lstmax);
			Assert.assertNotNull(j.lstmin);
			Assert.assertTrue(j.lstmin >= 0);
			Assert.assertTrue(j.lstmax >= 0);
			Assert.assertTrue(j.lstmin < 24);
			Assert.assertTrue(j.lstmax < 24);
			Assert.assertNotNull(j.hours);
			Assert.assertNotNull(j.proposal);
			Assert.assertNotNull(j.proposal.jobs);
			Assert.assertFalse(j.proposal.jobs.isEmpty());
			Assert.assertNotNull(j.proposal.priority);
			Assert.assertTrue(j.proposal.priority > 0);

			hourhist.addItem(j.hours.intValue());
			if (j.lstmax == null || j.lstmin == null)
				System.out.println(j);
			double diff = j.lstmax - j.lstmin;
			if (diff < 0)
				diff = j.lstmax - j.lstmin + 24;
			durationhist.addItem(diff);
		}

		System.out.println("hours:");
		for (Bin<Integer> bin : hourhist) {
			int c = round(bin.getCount());
			System.out.print(bin.getLow() + " .. " + bin.getHigh() + "  \t");
			while (c >= 100) {
				System.out.print("C");
				c -= 100;
			}
			System.out.println(" | " + bin.getCount());
		}
		System.out.println("duration:");
		for (Bin<Double> bin : durationhist) {
			int c = round(bin.getCount());
			System.out.print(bin.getLow() + " .. " + bin.getHigh() + "  \t");
			while (c >= 100) {
				System.out.print("C");
				c -= 100;
			}
			System.out.print(" ");
			while (c >= 10) {
				System.out.print("X");
				c -= 10;
			}
			System.out.print(" ");
			while (c >= 1) {
				System.out.print("i");
				c -= 1;
			}

			System.out.println(" | " + bin.getCount());
		}
		durationhist.normalize();
		hourhist.normalize();
		FileWriter fw;
		fw = new FileWriter("/tmp/histdurationsgen.txt");
		for (Bin<Double> bin : durationhist) {
			fw.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
					+ bin.getCount() + "\t" + bin.getLow() + "\t"
					+ bin.getHigh() + "\n");
		}
		fw.close();
		fw = new FileWriter("/tmp/histhoursgen.txt");
		for (Bin<Integer> bin : hourhist) {
			fw.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
					+ bin.getCount() + "\t" + bin.getLow() + "\t"
					+ bin.getHigh() + "\n");
		}
		fw.close();
	}

	private void calculateDurationBins(Histogram<Double> durationhist) {
		for (int i = 0; i <= 24; i += 4) {
			durationhist.addBin(i * 1., i + 4.);
		}
	}

	private void calculateHourBins(Histogram<Integer> hourhist) {
		Integer lastborder = 0;
		for (int i = 9; i >= 0; i--) {
			Integer border = round(10000 * Math.pow(3, -i));
			hourhist.addBin(lastborder, border);
			System.out.println(lastborder + " .. " + border);
			lastborder = border;
		}
	}

	@Test
	public void fetchDataHistogram() throws IOException {
		File f = new File("/home/user/Downloads/ata-proposals.csv");
		// List<Double> start = new ArrayList<Double>();
		// List<Double> end = new ArrayList<Double>();
		List<Double> duration = new ArrayList<Double>();
		List<Double> visibility = new ArrayList<Double>();
		List<Double> antennas = new ArrayList<Double>();
		List<Double> hours = new ArrayList<Double>();
		LineNumberReader r = new LineNumberReader(new FileReader(f));
		// skipping first line, header
		String line = r.readLine();

		while (true) {
			line = r.readLine();
			if (line == null)
				break;
			String[] parts = line.split(", ");
			Double[] partsd = new Double[parts.length];
			for (int i = 0; i < parts.length; i++) {
				try {
					partsd[i] = Double.parseDouble(parts[i]);
				} catch (NumberFormatException e) {
					partsd[i] = Double.NaN;
				}
			}

			// start, end, duration, antennas, hours
			duration.add(Math.abs(partsd[2]));
			visibility.add(Math.abs(partsd[0] - partsd[1]));
			antennas.add(partsd[3]);
			hours.add(partsd[4]);
		}
		Histogram<Double> histduration = new Histogram<Double>();
		Histogram<Double> histdurationw = new Histogram<Double>();
		Histogram<Integer> histhours = new Histogram<Integer>();
		calculateDurationBins(histduration);
		calculateDurationBins(histdurationw);
		calculateHourBins(histhours);

		for (int i = 0; i < duration.size(); i++) {
			if (Double.isNaN(duration.get(i))) {
				Assert.assertTrue(Double.isNaN(visibility.get(i)));
				continue;
			}
			Assert.assertEquals(duration.get(i), visibility.get(i), 0.001);
			histduration.addItem(duration.get(i));
			histdurationw.addItem(duration.get(i), hours.get(i));
			histhours.addItem(round(hours.get(i)));
		}
		histduration.normalize();
		histdurationw.normalize();
		histhours.normalize();
		FileWriter fw;
		fw = new FileWriter("/tmp/histdurationsw.txt");
		for (Bin<Double> bin : histdurationw) {
			fw.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
					+ bin.getCount() + "\t" + bin.getLow() + "\t"
					+ bin.getHigh() + "\n");
		}
		fw.close();
		fw = new FileWriter("/tmp/histdurations.txt");
		for (Bin<Double> bin : histduration) {
			fw.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
					+ bin.getCount() + "\t" + bin.getLow() + "\t"
					+ bin.getHigh() + "\n");
		}
		fw.close();
		fw = new FileWriter("/tmp/histhours.txt");
		for (Bin<Integer> bin : histhours) {
			fw.write((bin.getHigh() + bin.getLow()) / 2. + "\t"
					+ bin.getCount() + "\t" + bin.getLow() + "\t"
					+ bin.getHigh() + "\n");
		}
		fw.close();
	}

	private Integer round(double d) {
		return Integer.valueOf((int) Math.round(d));
	}

	private Collection<Job> getallJobs(Collection<Proposal> proposals) {
		Collection<Job> jobs = new ArrayList<Job>(proposals.size());
		for (Proposal p : proposals) {
			jobs.addAll(p.jobs);
		}
		return jobs;
	}

}
