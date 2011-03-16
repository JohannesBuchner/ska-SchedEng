package local.radioschedulers.importer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;
import local.radioschedulers.Schedule;
import local.radioschedulers.ScheduleSpace;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ObjectMapper.DefaultTyping;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

public class JsonScheduleReader {
	private static Logger log = Logger.getLogger(JsonScheduleReader.class);
	private ObjectMapper mapper;
	private File spaceFile;
	private File schedulesFile;
	private Map<String, Job> jobmap = new HashMap<String, Job>();

	public JsonScheduleReader(File scheduleFile, File spaceFile,
			Collection<Proposal> proposals) {
		this.schedulesFile = scheduleFile;
		this.spaceFile = spaceFile;
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				jobmap.put(j.id, j);
			}
		}

		mapper = new ObjectMapper();
		SimpleModule m = new SimpleModule("Job", new Version(1, 1, 1, ""));
		m.addDeserializer(Job.class, new JsonDeserializer<Job>() {

			@Override
			public Job deserialize(JsonParser jp, DeserializationContext ctxt)
					throws IOException, JsonProcessingException {
				String s = jp.getCurrentToken().asString();
				if (s.startsWith("Job:")) {
					String jobid = s.substring("Job:".length());
					return jobmap.get(jobid);
				}
				return null;
			}
		});
		m.addSerializer(new JsonSerializer<Job>() {
			@Override
			public Class<Job> handledType() {
				return Job.class;
			}

			@Override
			public void serialize(Job j, JsonGenerator jgen,
					SerializerProvider provider) throws IOException,
					JsonProcessingException {
				jgen.writeString("Job:" + j.id);
			}
		});
		// mapper.registerModule(m);
		// mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
		// mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
		// mapper.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
		// mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
	}

	public void write(ScheduleSpace space) throws Exception {
		ObjectWriter w = mapper
				.prettyPrintingWriter(new DefaultPrettyPrinter());

		w.writeValue(spaceFile, space);
		log.debug("wrote space to " + spaceFile);
	}

	public void write(Map<String, Schedule> schedules) throws Exception {
		ObjectWriter w = mapper
				.prettyPrintingWriter(new DefaultPrettyPrinter());
		ScheduleCollection sc = new ScheduleCollection();
		sc.content = schedules;
		w.writeValue(schedulesFile, sc);
		log.debug("wrote " + schedules.size() + " to " + schedulesFile);
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
	public static class ScheduleCollection {
		public Map<String, Schedule> content;
	}

	public void writeone(Schedule s) throws Exception {
		ObjectWriter w = mapper
				.prettyPrintingWriter(new DefaultPrettyPrinter());
		w.writeValue(schedulesFile, s);
		log.debug("wrote schedule to " + schedulesFile);
	}

	public Map<String, Schedule> readall() throws IOException {
		return mapper.readValue(schedulesFile, ScheduleCollection.class).content;
	}

	public Schedule readone() throws IOException {
		return (Schedule) mapper.readValue(schedulesFile, Schedule.class);
	}

	public ScheduleSpace readspace() throws IOException {
		return mapper.readValue(spaceFile, ScheduleSpace.class);
	}
}
