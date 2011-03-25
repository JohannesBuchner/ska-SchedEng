package local.radioschedulers.run.demo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

public class TextFileMonitoringSystem implements MonitoringSystem {
	private static Logger log = Logger
			.getLogger(TextFileMonitoringSystem.class);

	private static final File antennasFile = new File("available-antennas.txt");
	private static final File backendsFile = new File("available-backends.txt");
	private long lastReadTime = 0;

	@Override
	public boolean haveResourcesChanged() {
		if (lastReadTime < backendsFile.lastModified()
				|| lastReadTime < antennasFile.lastModified()) {
			return true;
		}
		return false;
	}

	@Override
	public Collection<String> getAvailableBackends() {
		lastReadTime = Math.max(lastReadTime, backendsFile.lastModified());
		return readLines(backendsFile);
	}

	@Override
	public Collection<String> getAvailableAntennas() {
		lastReadTime = Math.max(lastReadTime, antennasFile.lastModified());
		return readLines(antennasFile);
	}

	private static Collection<String> readLines(File f) {
		List<String> seq = new ArrayList<String>();
		try {
			LineNumberReader lnr = new LineNumberReader(new FileReader(f));
			while (true) {
				String line = lnr.readLine();
				if (line == null)
					break;
				seq.add(line);
				log.debug(f.getName() + " - " + line);
			}
		} catch (IOException e) {
			log.error("error reading file", e);
		}
		return seq;
	}

}
