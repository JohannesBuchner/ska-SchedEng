package local.radioschedulers.run;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class PropertiesContext {
	private static final Logger log = Logger.getLogger(PropertiesContext.class);
	protected static Properties p = new Properties();
	protected static Map<String, String> replaceMap = new HashMap<String, String>();

	static {
		try {
			p.load(new FileInputStream("run.properties"));
		} catch (IOException e) {
			log.error(e);
			p = null;
		}
	}

	public static void addReplacement(String key, String value) {
		replaceMap.put(key, value);
	}

	private static String replace(String v) {
		for (Entry<String, String> e : replaceMap.entrySet()) {
			v = v.replace("${" + e.getKey() + "}", e.getValue().toString());
		}
		return v;
	}

	public static String schedulesFilename() {
		return replace(p.getProperty("data.schedules"));
	}

	public static String spaceFilename() {
		return replace(p.getProperty("data.space"));
	}

	public static String proposalsFilename() {
		return replace(p.getProperty("data.proposals"));
	}

}
