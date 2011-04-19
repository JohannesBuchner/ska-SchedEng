package local.radioschedulers.importer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import local.radioschedulers.DateRequirements;
import local.radioschedulers.GoodBadDateRangeRequirements;
import local.radioschedulers.Job;
import local.radioschedulers.JobWithResources;
import local.radioschedulers.NoDateRequirements;
import local.radioschedulers.Proposal;
import local.radioschedulers.ResourceRequirement;

import org.apache.log4j.Logger;

/**
 * reads output of the form
 * 
 * <pre>
 * ------------------------------------------------------------------------
 * Project id: J001
 * Requested time: 4x10.0; 2x8.0; 15.0
 * Band: 3mm
 * Source: source1
 * RADEC: 16:00,-50:00
 * Good dates: 
 * Bad dates: 
 * Grade: 3.9
 * Comments:
 * ------------------------------------------------------------------------
 * </pre>
 * 
 * @author Johannes Buchner
 * 
 */
public class AtcaProposalReader implements IProposalReader {
	private static final double MINIMUM_ALTITUDE_DEGREES = 30.;
	private static final double LOCATION_LAT_DEGREES = -30.312778;
	private LineNumberReader reader;
	private Date startdate;
	private int ndays;
	private LSTRangeCalculator lstcalc;
	private static final Logger log = Logger
			.getLogger(AtcaProposalReader.class);

	public AtcaProposalReader(File f, Date startdate, int ndays)
			throws FileNotFoundException {
		reader = new LineNumberReader(new FileReader(f));
		this.startdate = startdate;
		this.ndays = ndays;
		this.lstcalc = new LSTRangeCalculator();
		this.lstcalc.setLocationLatitudeDegrees(LOCATION_LAT_DEGREES);
		this.lstcalc.setMinimumAltitudeDegrees(MINIMUM_ALTITUDE_DEGREES);
	}

	@Override
	public Collection<Proposal> readall() throws Exception {
		Collection<Proposal> proposals = new ArrayList<Proposal>();

		try {
			while (true) {
				String[] l;
				l = readLine();
				if (l != null) {
					throw new IOException("expected '----', got '" + l[0] + "'");
				}
				l = readLine();
				if (l == null)
					break;
				if (!l[0].equals("Project id")) {
					throw new IOException("expected Project id, got '" + l[0]
							+ "'");
				}
				log.debug("Line " + reader.getLineNumber());
				Proposal p = new Proposal();
				p.id = l[1];

				l = readLine();
				if (!l[0].equals("Requested time")) {
					throw new IOException("expected Requested time, got '"
							+ l[0] + "'");
				}
				String[] times = l[1].split(";");

				l = readLine();
				if (!l[0].equals("Band")) {
					throw new IOException("expected Band, got '" + l[0] + "'");
				}
				String band = l[1];

				l = readLine();
				if (!l[0].equals("Source")) {
					throw new IOException("expected Source, got '" + l[0] + "'");
				}
				String[] sources = l[1].split(";");

				l = readLine();
				if (!l[0].equals("RADEC")) {
					throw new IOException("expected RADEC, got '" + l[0] + "'");
				}
				String[] radecs = l[1].split(";");

				// these refer to the whole proposal
				l = readLine();
				if (!l[0].equals("Good dates")) {
					throw new IOException("expected Good dates, got '" + l[0]
							+ "'");
				}
				String[] gooddates = l[1].split(";");
				l = readLine();
				if (!l[0].equals("Bad dates")) {
					throw new IOException("expected Bad dates, got '" + l[0]
							+ "'");
				}
				String[] baddates = l[1].split(";");

				int n = radecs.length;
				if (!(n == sources.length && (n == times.length || n == 1))) {
					throw new IOException("not the same number of sources ("
							+ sources.length + "), radecs (" + n
							+ ") and times (" + times.length + ").");
				}

				l = readLine();
				if (!l[0].equals("Grade")) {
					throw new IOException("expected Grade, got '" + l[0] + "'");
				}
				p.priority = Double.parseDouble(l[1]);

				l = readLine();
				if (!l[0].equals("Comments")) {
					throw new IOException("expected Comments, got '" + l[0]
							+ "'");
				}
				p.name = p.id;
				if (l[1].trim().length() != 0)
					p.name = p.name + " " + l[1].trim();
				// ignore comments

				p.jobs = new ArrayList<Job>();
				for (int i = 0; i < n; i++) {
					JobWithResources j = new JobWithResources();

					j.date = interpreteDates(gooddates, baddates);
					if (n == 1) {
						j.hours = parseHour(times[0]);
						for (int k = 1; k < times.length; k++)
							j.hours += parseHour(times[k]);
					} else
						j.hours = parseHour(times[i]);
					j.id = sources[i];
					Double locationlat = LOCATION_LAT_DEGREES / 180 * Math.PI;
					interpreteRADec(j, radecs[i], locationlat);
					j.proposal = p;
					ResourceRequirement rr = new ResourceRequirement();
					rr.numberrequired = 1;
					rr.possibles.add(band);
					j.resources.put("band", rr);
					rr = new ResourceRequirement();
					rr.numberrequired = 1;
					rr.possibles.add("Mopra");
					j.resources.put("antennas", rr);
					p.jobs.add(j);
				}
				proposals.add(p);
			}
		} catch (Exception e) {
			log.error("stopping at line " + reader.getLineNumber(), e);
			throw e;
		}

		return proposals;
	}

	private Double parseHour(String string) {
		if (string.contains("x")) {
			String[] parts = string.split("x");
			return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
		} else {
			return Double.parseDouble(string);
		}
	}

	private void interpreteRADec(JobWithResources j, String radec,
			Double locationlat) throws Exception {

		if (radec.startsWith("lst(")) {
			String[] parts = radec.substring("lst(".length()).split("[),]");
			j.lstmin = parseRAHours(parts[0]);
			j.lstmax = parseRAHours(parts[1]);
			return;
		}

		this.lstcalc.setRaHours(parseRAHours(radec.split(",")[0]));
		this.lstcalc.setDecDegrees(parseDecDegrees(radec.split(",")[1]));
		this.lstcalc.calculate();
		j.lstmin = this.lstcalc.getLstminHours();
		j.lstmax = this.lstcalc.getLstmaxHours();
	}

	private Double parseDecDegrees(String string) {
		// degrees, minutes
		String[] parts = string.split(":", 2);
		return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]) / 60;
	}

	private Double parseRAHours(String string) {
		// hours, minutes
		String[] parts = string.split(":", 2);
		return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]) / 60;
	}

	private DateRequirements interpreteDates(String[] gooddates,
			String[] baddates) throws ParseException {

		if (gooddates.length == 0 && baddates.length == 0) {
			return new NoDateRequirements();
		}
		GoodBadDateRangeRequirements dr = null;
		for (int i = 0; i < gooddates.length; i++) {
			if (gooddates[i].length() == 0) {
				dr = new GoodBadDateRangeRequirements(ndays);
				continue;
			}
			String[] parts = gooddates[i].split("-");
			Date start = str2date(parts[0]);
			Date end = str2date(parts[1]);
			if (dr == null) {
				dr = new GoodBadDateRangeRequirements(
						getDelta(start, startdate), getDelta(end, startdate));
			} else {
				dr.addGoodRange(getDelta(start, startdate), getDelta(end,
						startdate));
			}
		}
		for (int i = 0; i < baddates.length; i++) {
			if (baddates[i].length() == 0)
				continue;
			String[] parts = baddates[i].split("-");
			Date start = str2date(parts[0]);
			Date end = str2date(parts[1]);
			dr
					.addBadRange(getDelta(start, startdate), getDelta(end,
							startdate));
		}
		return dr;
	}

	private int getDelta(Date d, Date start) {
		// there is a bug here that we don't respect time etc.
		// to do this properly, we would need the Periods class from JodaTime
		return (int) ((d.getTime() - start.getTime()) / 1000 / 60 / 60 / 24);
	}

	private String[] readLine() throws IOException {
		String l = reader.readLine();
		if (l == null)
			return null;
		if (l.startsWith("----")) {
			return null;
		}
		while (l.endsWith("\\")) {
			String l2 = reader.readLine();
			if (l2 == null) {
				l2 = null;
			}
			l = l.substring(0, l.length() - 1).concat(l2);
		}
		return l.split(": *", 2);
	}

	private Date str2date(String s) throws ParseException {
		// DateFormat d =
		// SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
		// return d.parse(s + "/" + (new Date().getYear() + 1900));
		Date r = new Date();
		String[] parts = s.trim().split("/");
		r.setMonth(Integer.parseInt(parts[1]) - 1);
		r.setDate(Integer.parseInt(parts[0]));
		return r;
	}

}
