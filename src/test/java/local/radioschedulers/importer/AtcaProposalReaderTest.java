package local.radioschedulers.importer;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import junit.framework.Assert;
import local.radioschedulers.Proposal;

import org.junit.Before;
import org.junit.Test;

public class AtcaProposalReaderTest {

	private File f = new File(
			"/home/user/Downloads/ata-proposals/mp-johannes.txt");
	private AtcaProposalReader reader;

	private Date str2date(String s) throws ParseException {
		return SimpleDateFormat.getDateInstance(
				SimpleDateFormat.SHORT).parse( s + "/" + new Date().getYear());
	}
	
	@Before
	public void setUp() throws Exception {
		reader = new AtcaProposalReader(f, str2date("01/07"), 30);
	}

	@Test
	public void readTest() throws Exception {
		Collection<Proposal> proposals = reader.readall();
		Assert.assertEquals(41, proposals.size());
	}

}
