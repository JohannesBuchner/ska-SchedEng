package local.radioschedulers.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

import org.apache.log4j.Logger;
import org.codehaus.jackson.impl.Indenter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.ObjectMapper.DefaultTyping;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.codehaus.jackson.util.DefaultPrettyPrinter.Lf2SpacesIndenter;

public class JsonProposalReader implements IProposalReader {
	private static Logger log = Logger.getLogger(JsonProposalReader.class);
	private ObjectMapper mapper;
	private File file;

	public JsonProposalReader(File f) {
		this.file = f;
		mapper = new ObjectMapper();
		mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
	}

	public void write(Collection<Proposal> proposals) throws Exception {
		ObjectWriter w = mapper.prettyPrintingWriter(new DefaultPrettyPrinter());
		w.writeValue(file, proposals);
		log.debug("wrote " + proposals.size() + " to " + file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() throws IOException {
		@SuppressWarnings("unchecked")
		List<Proposal> proposals = (List<Proposal>) mapper.readValue(file, ArrayList.class);
		for (Proposal p : proposals) {
			for (Job j : p.jobs) {
				j.proposal = p;
			}
		}
		log.debug("wrote " + proposals.size() + " to " + file);
		return proposals;
	}
}
