package net.myerichsen.blistrup.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Michael Erichsen
 * @version 6. okt. 2023
 *
 */
public class TableBuilder {
	private final static String[] statements = { "ALTER TABLE BLISTRUP.FAMILIE DROP CONSTRAINT FAMILIE_PK",
			"ALTER TABLE BLISTRUP.FAMILIEBEGIVENHED DROP CONSTRAINT FAMILIEBEG_PK",
			"ALTER TABLE BLISTRUP.INDIVIDBEGIVENHED DROP CONSTRAINT INDIVIDBEG_PK",
			"ALTER TABLE BLISTRUP.VIDNE DROP CONSTRAINT VIDNE_PK",
			"ALTER TABLE BLISTRUP.INDIVID DROP CONSTRAINT INDIVID_PK",
			"ALTER TABLE BLISTRUP.KILDE DROP CONSTRAINT KILDE_PK",
			"ALTER TABLE BLISTRUP.PERSONNAVN DROP CONSTRAINT PERSONNAVN_PK", "DROP INDEX BLISTRUP.INDIVIDBEG_ID_IDX",
			"DROP INDEX BLISTRUP.SQL230717172251240", "DROP INDEX BLISTRUP.SQL230908210035770",
			"DROP INDEX BLISTRUP.SQL230717172251540", "DROP INDEX BLISTRUP.KILDE_ID_IDX",
			"DROP INDEX BLISTRUP.SQL230717172250900", "DROP INDEX BLISTRUP.SQL230717172251440",
			"DROP INDEX BLISTRUP.SQL230728111151940", "DROP INDEX BLISTRUP.FAMILIEBEG_ID_IDX",
			"DROP INDEX BLISTRUP.SQL230728111151961", "DROP INDEX BLISTRUP.SQL230717172251010",
			"DROP INDEX BLISTRUP.SQL230717172250700", "DROP INDEX BLISTRUP.SQL230728111151910",
			"DROP INDEX BLISTRUP.PERSONNAVN_ID_IDX", "DROP INDEX BLISTRUP.SQL230908210035750",
			"DROP INDEX BLISTRUP.SQL230728111151930", "DROP INDEX BLISTRUP.SQL230728111151880",
			"DROP INDEX BLISTRUP.SQL230717172251140", "DROP TABLE BLISTRUP.FAMILIE", "DROP TABLE BLISTRUP.KILDE",
			"DROP TABLE BLISTRUP.INDIVID", "DROP TABLE BLISTRUP.VIDNE", "DROP TABLE BLISTRUP.PERSONNAVN",
			"DROP TABLE BLISTRUP.FAMILIEBEGIVENHED", "DROP TABLE BLISTRUP.INDIVIDBEGIVENHED",
			"CREATE TABLE BLISTRUP.FAMILIE ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), HUSFADER INTEGER, HUSMODER INTEGER )",
			"CREATE TABLE BLISTRUP.KILDE ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), REPOSITORYID INTEGER, FAMILIEBEGIVENHEDID INTEGER, INDIVIDBEGIVENHEDID INTEGER, KBNR CHAR(16), AARINTERVAL CHAR(16), KBDEL CHAR(16), TIFNR CHAR(16), SIDE CHAR(16), OPSLAG CHAR(16), OPNR CHAR(16) )",
			"CREATE TABLE BLISTRUP.INDIVID ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), FAMC INTEGER, KOEN CHAR(5), BLISTRUPID CHAR(32), FOEDT CHAR(16), FAM CHAR(16), SLGT CHAR(16) )",
			"CREATE TABLE BLISTRUP.VIDNE ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), INDIVIDID INTEGER NOT NULL, FAMILIEBEGIVENHEDID INTEGER, INDIVIDBEGIVENHEDID INTEGER, ROLLE VARCHAR(64) )",
			"CREATE TABLE BLISTRUP.PERSONNAVN ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), INDIVIDID INTEGER NOT NULL, STDNAVN CHAR(128), PREFIX CHAR(32), FORNAVN CHAR(64), KAELENAVN CHAR(64), EFTERNAVNEPREFIX CHAR(16), EFTERNAVN CHAR(32), SUFFIX CHAR(16), PRIMAERNAVN CHAR(5) DEFAULT 'FALSE' NOT NULL, FONETISKNAVN CHAR(64) )",
			"CREATE TABLE BLISTRUP.FAMILIEBEGIVENHED ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), FAMILIEID INTEGER NOT NULL, HUSFADERALDER INTEGER, HUSMODERALDER INTEGER, KILDEID INTEGER, BEGTYPE CHAR(16), UNDERTYPE CHAR(16), DATO DATE, NOTE VARCHAR(16000), DETALJER VARCHAR(16000), BLISTRUPID CHAR(32), STEDNAVN VARCHAR(128), BEM CHAR(128) )",
			"CREATE TABLE BLISTRUP.INDIVIDBEGIVENHED ( ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 ,INCREMENT BY 1), INDIVIDID INTEGER NOT NULL, ALDER INTEGER, KILDEID INTEGER, BEGTYPE CHAR(16), UNDERTYPE CHAR(16), DATO DATE, NOTE VARCHAR(16000), DETALJER VARCHAR(16000), BLISTRUPID CHAR(32), ROLLE VARCHAR(64), FOEDT CHAR(16), STEDNAVN VARCHAR(128), BEM CHAR(128) )",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230728111151830 ON BLISTRUP.PERSONNAVN (INDIVIDID ASC, ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.INDIVIDBEG_ID_IDX ON BLISTRUP.INDIVIDBEGIVENHED (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230908210035770 ON BLISTRUP.INDIVIDBEGIVENHED (INDIVIDID ASC, ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230717172251540 ON BLISTRUP.VIDNE (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.KILDE_ID_IDX ON BLISTRUP.KILDE (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230717172250900 ON BLISTRUP.PERSONNAVN (INDIVIDID ASC, ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230717172251440 ON BLISTRUP.FAMILIE (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230728111151940 ON BLISTRUP.INDIVID (ID ASC)",
			"CREATE INDEX BLISTRUP.FAMILIEBEG_ID_IDX ON BLISTRUP.FAMILIEBEGIVENHED (FAMILIEID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230728111151961 ON BLISTRUP.FAMILIE (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230717172251010 ON BLISTRUP.FAMILIEBEGIVENHED (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230717172250700 ON BLISTRUP.INDIVID (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230728111151910 ON BLISTRUP.VIDNE (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.PERSONNAVN_ID_IDX ON BLISTRUP.PERSONNAVN (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230908210035750 ON BLISTRUP.FAMILIEBEGIVENHED (ID ASC)",
			"CREATE UNIQUE INDEX BLISTRUP.SQL230728111151880 ON BLISTRUP.KILDE (ID ASC)",
			"CREATE INDEX BLISTRUP.SQL230717172251140 ON BLISTRUP.INDIVIDBEGIVENHED (INDIVIDID ASC)",
			"ALTER TABLE BLISTRUP.FAMILIE ADD CONSTRAINT FAMILIE_PK PRIMARY KEY (ID)",
			"ALTER TABLE BLISTRUP.FAMILIEBEGIVENHED ADD CONSTRAINT FAMILIEBEG_PK PRIMARY KEY (ID)",
			"ALTER TABLE BLISTRUP.INDIVIDBEGIVENHED ADD CONSTRAINT INDIVIDBEG_PK PRIMARY KEY (INDIVIDID, ID)",
			"ALTER TABLE BLISTRUP.VIDNE ADD CONSTRAINT VIDNE_PK PRIMARY KEY (ID)",
			"ALTER TABLE BLISTRUP.INDIVID ADD CONSTRAINT INDIVID_PK PRIMARY KEY (ID)",
			"ALTER TABLE BLISTRUP.KILDE ADD CONSTRAINT KILDE_PK PRIMARY KEY (ID)",
			"ALTER TABLE BLISTRUP.PERSONNAVN ADD CONSTRAINT PERSONNAVN_PK PRIMARY KEY (INDIVIDID, ID)" };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final TableBuilder tb = new TableBuilder();
		try {
			tb.execute();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @throws SQLException
	 *
	 */
	private void execute() throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:C:\\Users\\michael\\BlistrupDB");
		PreparedStatement statement;

		for (final String statement2 : statements) {
			System.out.println(statement2);
			try {
				statement = conn.prepareStatement(statement2);
				statement.execute();
			} catch (final SQLException e) {
				System.out.println(e.getMessage());
			}
		}

		conn.commit();
		conn.close();
	}

}
