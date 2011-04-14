package local.radioschedulers.importer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import local.radioschedulers.Job;
import local.radioschedulers.Proposal;

public class SqliteProposalReader implements IProposalReader {
	private static Logger log = Logger.getLogger(SqliteProposalReader.class);

	private static final String DB = "jdbc:sqlite:/home/user/test.db";

	public void fill() throws Exception {

		Class.forName("org.sqlite.JDBC");
		// Class.forName("SQLite.JDBCDriver").newInstance();

		Connection conn = DriverManager.getConnection(DB);
		conn.setAutoCommit(false);
		conn.prepareStatement("DELETE FROM proposals;").execute();

		PreparedStatement prep = conn
				.prepareStatement("INSERT INTO proposals (id, name, start) VALUES (?,?,?);");

		prep.setString(1, "1");
		prep.setString(2, "foo");
		prep.setDate(3, new Date(System.currentTimeMillis() + 0 * 24 * 60 * 60
				* 1000));
		prep.addBatch();
		prep.setString(1, "2");
		prep.setString(2, "bar");
		prep.setDate(3, new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60
				* 1000));
		prep.addBatch();
		prep.setString(1, "3");
		prep.setString(2, "baz");
		prep.setDate(3, new Date(System.currentTimeMillis() + 0 * 24 * 60 * 60
				* 1000));
		prep.addBatch();

		prep.executeBatch();
		conn.commit();

		conn.prepareStatement("DELETE FROM jobs;").execute();

		prep = conn
				.prepareStatement("INSERT INTO jobs (proposalid, hours, dec, ra) VALUES (?,?,?,?);");

		prep.setString(1, "1");
		prep.setLong(2, 100);
		prep.setDouble(3, -(5 + 35. / 60 + 30. / 60. / 60.));
		prep.setDouble(4, +(5 + 35. / 60 + 30. / 60. / 60.));
		prep.addBatch();
		prep.setString(1, "1");
		prep.setLong(2, 40);
		prep.setDouble(3, -(5 + 25. / 60 + 30. / 60. / 60.));
		prep.setDouble(4, +(5 + 45. / 60 + 30. / 60. / 60.));
		prep.addBatch();
		prep.setString(1, "1");
		prep.setLong(2, 10);
		prep.setDouble(3, -(5 + 25. / 60 + 30. / 60. / 60.));
		prep.setDouble(4, +(5 + 35. / 60 + 30. / 60. / 60.));
		prep.addBatch();
		prep.setString(1, "2");
		prep.setLong(2, 100);
		prep.setDouble(3, -(12 + 35. / 60 + 30. / 60. / 60.));
		prep.setDouble(4, +(15 + 35. / 60 + 30. / 60. / 60.));
		prep.addBatch();
		prep.setString(1, "3");
		prep.setLong(2, 40);
		prep.setDouble(3, -(20 + 35. / 60 + 30. / 60. / 60.));
		prep.setDouble(4, +(12 + 35. / 60 + 30. / 60. / 60.));
		prep.addBatch();

		prep.executeBatch();

		conn.commit();
		conn.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IProposalReader#readall()
	 */
	public Collection<Proposal> readall() throws SQLException {

		Connection conn = DriverManager.getConnection(DB);

		PreparedStatement stmt = conn
				.prepareStatement("SELECT name, start, id from proposals ORDER BY start ASC");
		ResultSet res = stmt.executeQuery();
		List<Proposal> proposals = new ArrayList<Proposal>();
		while (res.next()) {
			Proposal p = new Proposal();
			p.name = res.getString(1);
			p.start = res.getDate(2);
			p.id = res.getString(3);
			p.jobs = new ArrayList<Job>();
			PreparedStatement stmt2 = conn
					.prepareStatement("SELECT id, hours, lstmin, lstmax from jobs where proposalid = ?");
			stmt2.setString(1, p.id);

			ResultSet res2 = stmt2.executeQuery();
			while (res2.next()) {
				Job j = new Job();
				j.id = res2.getString(1);
				j.proposal = p;
				j.hours = 1. * res2.getLong(2);
				j.lstmin = res2.getDouble(3);
				j.lstmax = res2.getDouble(4);

				p.jobs.add(j);
			}

			proposals.add(p);
		}

		return proposals;
	}

	public static void main(String[] args) throws Exception {
		SqliteProposalReader pr = new SqliteProposalReader();
		pr.fill();
		for (Proposal p : pr.readall())
			log.debug(p.toString());
	}
}
