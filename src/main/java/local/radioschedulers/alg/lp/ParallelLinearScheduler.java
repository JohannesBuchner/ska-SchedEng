package local.radioschedulers.alg.lp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import local.radioschedulers.IScheduler;
import local.radioschedulers.Job;
import local.radioschedulers.JobCombination;
import local.radioschedulers.LSTTime;
import local.radioschedulers.LSTTimeIterator;
import local.radioschedulers.ScheduleSpace;
import local.radioschedulers.SimpleEntry;
import local.radioschedulers.Schedule;

public class ParallelLinearScheduler implements IScheduler {
	private static Logger log = Logger.getLogger(ParallelLinearScheduler.class);

	protected List<JobCombination> jobComboIdSet = new ArrayList<JobCombination>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see IScheduler#schedule(java.util.Collection)
	 */
	public Schedule schedule(ScheduleSpace scheduleTemplate) {
		/**
		 * set the variables to be binary
		 */
		PrintStream varDefinition;
		PrintStream constraints;
		PrintStream costFunction;
		File vardefFile;
		File constraintsFile;
		File costsFile;
		File lp;
		try {
			vardefFile = File.createTempFile("vardef", ".lp");
			constraintsFile = File.createTempFile("constraints", ".lp");
			costsFile = File.createTempFile("costs", ".lp");
			lp = File.createTempFile("schedule", ".lp");
		} catch (IOException e1) {
			throw new IllegalStateException(e1);
		}
		try {
			varDefinition = new PrintStream(vardefFile);
			constraints = new PrintStream(constraintsFile);
			costFunction = new PrintStream(costsFile);
		} catch (FileNotFoundException e1) {
			throw new IllegalStateException(e1);
		}

		Map<Job, StringBuilder> jobsumSet = new HashMap<Job, StringBuilder>();
		/**
		 * cost function: simply maximizing time * priority
		 */
		costFunction.append("max: ");

		int varCount = 0;
		int constraintCount = 0;
		/**
		 * 1) No incompatible jobs are at the same time k, i.e. not requesting
		 * more resources than we have.
		 * 
		 * 2) A job is on when it can do work, i.e. the source object is up:
		 * 
		 * This is already ensured by the timeline, which only allows compatible
		 * jobs in the first place
		 */
		for (Iterator<LSTTime> it = getIterator(scheduleTemplate); it.hasNext();) {
			LSTTime t = it.next();
			Set<JobCombination> jcs = scheduleTemplate.get(t);
			if (!jcs.isEmpty()) {
				for (JobCombination jc : jcs) {
					Integer id = jobComboIdSet.lastIndexOf(jc);
					if (id == -1) {
						id = jobComboIdSet.size();
						jobComboIdSet.add(jc);
					}
					String varname = getVar(t, id);
					varCount++;
					log.debug(varname + " -- jobCombination " + id
							+ " at time " + t + " : " + " (prio "
							+ jc.calculatePriority() + ")");
					for (Job j : jc.jobs) {
						log.debug("     job [" + j.lstmin + ".." + j.lstmax
								+ "]: Proposal " + j.proposal.id);
					}

					varDefinition.append("bin " + varname + ";\n");

					for (Job j : jc.jobs) {
						/**
						 * 4) A job gets its hours:
						 */
						StringBuilder jobsum = jobsumSet.get(j);
						if (jobsum == null) {
							jobsum = new StringBuilder();
							jobsumSet.put(j, jobsum);
						}
						jobsum.append(varname);
						jobsum.append(" +");

					}
					// cost function
					costFunction.append(jc.calculatePriority() + " " + varname
							+ " + ");

					/**
					 * only allow one JobCombination at a time
					 */
					constraints.append(varname);
					constraints.append(" +");
				}
				constraints.append("0 <= 1;\n");
			}
		}
		/**
		 * overall time for the job
		 */
		for (Entry<Job, StringBuilder> e : jobsumSet.entrySet()) {
			constraints.append(e.getValue());
			long totalslots = (long) Math.ceil(e.getKey().hours
					* Schedule.LST_SLOTS_PER_HOUR);
			constraints.append("0 <= " + totalslots + ";\n");
			constraintCount++;
		}
		costFunction.append("0;\n");

		costFunction.close();
		constraints.close();
		varDefinition.close();
		log.debug("concat  ... ");
		catFilesF(lp, costsFile, constraintsFile, vardefFile);
		costsFile.delete();
		constraintsFile.delete();
		vardefFile.delete();
		log.debug("concat  done");

		Schedule s;
		try {
			log.info("Linear problem stated with " + varCount
					+ " variables and " + constraintCount + " constraints.");
			log.debug("solving ...");
			s = lpsolve(lp);
			log.info("solving done");
		} catch (IOException e) {
			throw new IllegalStateException("Install lp_solve", e);
		} finally {
			lp.delete();
		}
		return s;
	}

	protected Iterator<LSTTime> getIterator(ScheduleSpace scheduleTemplate) {
		return new LSTTimeIterator(scheduleTemplate.findLastEntry(),
				ScheduleSpace.LST_SLOTS_MINUTES);
	}

	private void catFilesF(File out, File... in) {

		try {
			out.delete();
			FileChannel dstChannel = new FileOutputStream(out, true)
					.getChannel();
			for (File f : in) {
				FileChannel srcChannel = new FileInputStream(f).getChannel();
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
				srcChannel.close();
			}
			dstChannel.close();
		} catch (IOException e) {
			throw new IllegalStateException("bad copying", e);
		}

	}

	/**
	 * parses a lp statement in LP format (see lp_solve) and returns a Schedule.
	 * 
	 * Calls and uses lp_solve internally to do this, so lp_solve has to be
	 * installed.
	 * 
	 * @param lpText
	 * @return schedule
	 * @throws IOException
	 */
	protected Schedule lpsolve(File lp) throws IOException {
		List<String> cmd = new ArrayList<String>();
		cmd.add("lp_solve");
		cmd.add(lp.getAbsolutePath());

		ProcessBuilder pb = new ProcessBuilder(cmd);
		log.debug("launching lp_solve");
		Process p = pb.start();
		LineNumberReader is = new LineNumberReader(new InputStreamReader(p
				.getInputStream()));
		return parseLpsolve(is);
	}

	private Schedule parseLpsolve(LineNumberReader is) throws IOException {
		String line;
		line = is.readLine();
		if (line.length() != 0)
			throw new IOException("unexpected response: " + line);

		line = is.readLine();
		if (!line.startsWith("Value of objective function: "))
			throw new IOException("unexpected response: " + line);

		line = is.readLine();
		if (line.length() != 0)
			throw new IOException("unexpected response: " + line);

		line = is.readLine();
		if (!line.startsWith("Actual values of the variables:"))
			throw new IOException("unexpected response: " + line);

		log.debug("parsing output ...");
		Schedule s = new Schedule();

		while (true) {
			line = is.readLine();
			if (line == null)
				break;

			// log(line);
			String parts[] = line.split(" [ ]*", 2);
			String varname = parts[0];
			Double value = Double.parseDouble(parts[1]);
			Entry<LSTTime, JobCombination> entry = decodeVar(varname);
			// log("parsed as j " + j + ", k " + k + ", day " + day +
			// ", minute "
			// + minute + ", value " + value);
			// log(t + " : " + j);
			if (value != 0) {
				s.add(entry.getKey(), entry.getValue());
			}
		}
		log.debug("parsing output done");
		return s;
	}

	private Entry<LSTTime, JobCombination> decodeVar(String var) {
		String varnameparts[] = var.split("_", 3);
		int j = Integer.parseInt(varnameparts[1]);
		int k = Integer.parseInt(varnameparts[2]);
		long day = k / ScheduleSpace.LST_SLOTS_PER_DAY;
		long minute = (k % ScheduleSpace.LST_SLOTS_PER_DAY)
				* ScheduleSpace.LST_SLOTS_MINUTES;
		return new SimpleEntry<LSTTime, JobCombination>(
				new LSTTime(day, minute), jobComboIdSet.get(j));
	}

	private String getVar(LSTTime t, int jobCombinationId) {
		long timeslotId = t.day * ScheduleSpace.LST_SLOTS_PER_DAY + t.minute
				/ ScheduleSpace.LST_SLOTS_MINUTES;
		return "x_" + jobCombinationId + "_" + timeslotId;
	}
}
