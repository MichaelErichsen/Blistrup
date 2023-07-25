 --<ScriptOptions statementTerminator=";"/>
CREATE TABLE BLISTRUP.INDIVID (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		FAMC INTEGER,
		KOEN CHAR(5),
		BLISTRUPID CHAR(32)
	);

CREATE TABLE BLISTRUP.PERSONNAVN (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		INDIVIDID INTEGER NOT NULL,
		STDNAVN CHAR(128),
		PREFIX CHAR(32),
		FORNAVN CHAR(64),
		KAELENAVN CHAR(64),
		EFTERNAVNEPREFIX CHAR(16),
		EFTERNAVN CHAR(32),
		SUFFIX CHAR(16),
		PRIMAERNAVN CHAR(5) DEFAULT 'FALSE' NOT NULL,
		FONETISKNAVN CHAR(64)
	);

CREATE TABLE BLISTRUP.FAMILIE (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY,
		HUSFADER INTEGER,
		HUSMODER INTEGER
	);

CREATE TABLE BLISTRUP.FAMILIEBEGIVENHED (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		FAMILIEID INTEGER NOT NULL,
		HUSFADERALDER INTEGER,
		HUSMODERALDER INTEGER,
		KILDEID INTEGER,
		BEGTYPE CHAR(16),
		UNDERTYPE CHAR(16),
		DATO DATE,
		NOTE VARCHAR(16000),
		DETALJER VARCHAR(16000),
		BLISTRUPID CHAR(32),
		STEDNAVN CHAR(32),
		BEM CHAR(128)
	);

	CREATE TABLE BLISTRUP.INDIVIDBEGIVENHED (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		INDIVIDID INTEGER NOT NULL,
		ALDER INTEGER,
		KILDEID INTEGER,
		BEGTYPE CHAR(16),
		UNDERTYPE CHAR(16),
		DATO DATE,
		NOTE VARCHAR(16000),
		DETALJER VARCHAR(16000),
		BLISTRUPID CHAR(32),
		ROLLE CHAR(32),
		FOEDT CHAR(16),
		STEDNAVN CHAR(32),
		BEM CHAR(128)
	);
	
CREATE TABLE BLISTRUP.JORDLOD (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		HUSNUMMER CHAR(32),
		GLMATRIKEL CHAR(32),
		MATRIKEL1844 CHAR(32),
		GADEADRESSE CHAR(64),
		EJERLAV CHAR(32),
		SOGN CHAR(32),
		HERRED CHAR(32),
		AMT CHAR(32),
		LAND CHAR(32),
		INDIVIDBEGIVENHEDID CHAR(5),
		FAMILIEBEGIVENHEDID CHAR(5)
	);

CREATE TABLE BLISTRUP.CITATION (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		SIDE CHAR(5),
		DATO CHAR(5),
		TEKST CHAR(5)
	);

CREATE TABLE BLISTRUP.VIDNE (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		INDIVIDID INTEGER NOT NULL,
		FAMILIEBEGIVENHEDID INTEGER,
		INDIVIDBEGIVENHEDID INTEGER,
		ROLLE CHAR(32)
	);

CREATE TABLE BLISTRUP.REPOSITORY (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		NAVN CHAR(64)
	);

CREATE TABLE BLISTRUP.KILDE (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		REPOSITORYID INTEGER,
		FAMILIEBEGIVENHEDID INTEGER,
		INDIVIDBEGIVENHEDID INTEGER,
		KBNR CHAR(16),
		AARINTERVAL CHAR(16),
		KBDEL CHAR(16),
		TIFNR CHAR(16),
		SIDE CHAR(16),
		OPSLAG CHAR(16),
		OPNR CHAR(16)
	);

CREATE TABLE BLISTRUP.NOTE (
		ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
		KILDEID INTEGER,
		REPOSITORYID INTEGER
	);

	

CREATE UNIQUE INDEX BLISTRUP.SQL230717172250800 ON BLISTRUP.REPOSITORY (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.KILDE_ID_IDX ON BLISTRUP.KILDE (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251240 ON BLISTRUP.JORDLOD (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251650 ON BLISTRUP.KILDE (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172250700 ON BLISTRUP.INDIVID (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251010 ON BLISTRUP.FAMILIEBEGIVENHED (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251340 ON BLISTRUP.CITATION (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.FAMILIEBEG_ID_IDX ON BLISTRUP.FAMILIEBEGIVENHED (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.NOTE_ID_IDX ON BLISTRUP.NOTE (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251540 ON BLISTRUP.VIDNE (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.INDIVIDBEG_ID_IDX ON BLISTRUP.INDIVIDBEGIVENHED (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251140 ON BLISTRUP.INDIVIDBEGIVENHED (INDIVIDID ASC, ID ASC);

CREATE UNIQUE INDEX BLISTRUP.INDIVID_ID_IDX ON BLISTRUP.INDIVID (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.PERSONNAVN_ID_IDX ON BLISTRUP.PERSONNAVN (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.REPOSITORY_ID_IDX ON BLISTRUP.REPOSITORY (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.CITATION_ID_IDX ON BLISTRUP.CITATION (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.JORDLOD_ID_IDX ON BLISTRUP.JORDLOD (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172250600 ON BLISTRUP.NOTE (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172250900 ON BLISTRUP.PERSONNAVN (INDIVIDID ASC, ID ASC);

CREATE UNIQUE INDEX BLISTRUP.VIDNE_ID_IDX ON BLISTRUP.VIDNE (ID ASC);

CREATE UNIQUE INDEX BLISTRUP.SQL230717172251440 ON BLISTRUP.FAMILIE (ID ASC);

ALTER TABLE BLISTRUP.CITATION ADD CONSTRAINT CITATION_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.PERSONNAVN ADD CONSTRAINT PERSONNAVN_PK PRIMARY KEY (INDIVIDID, ID);

ALTER TABLE BLISTRUP.REPOSITORY ADD CONSTRAINT REPOSITORY_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.FAMILIEBEGIVENHED ADD CONSTRAINT FAMILIEBEG_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.KILDE ADD CONSTRAINT KILDE_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.VIDNE ADD CONSTRAINT VIDNE_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.JORDLOD ADD CONSTRAINT JORDLOD_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.INDIVID ADD CONSTRAINT INDIVID_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.NOTE ADD CONSTRAINT NOTE_PK PRIMARY KEY (ID);

ALTER TABLE BLISTRUP.INDIVIDBEGIVENHED ADD CONSTRAINT INDIVIDBEG_PK PRIMARY KEY (INDIVIDID, ID);

ALTER TABLE BLISTRUP.FAMILIE ADD CONSTRAINT FAMILIE_PK PRIMARY KEY (ID);

--CREATE TABLE BLISTRUP.F9PERSONFAMILIEQ (
--		COL0 VARCHAR(200),
--		BEGIV VARCHAR(200),
--		PID VARCHAR(200),
--		TYPE VARCHAR(200),
--		AAR VARCHAR(200),
--		LBNR VARCHAR(200),
--		ROLLE VARCHAR(200),
--		RX VARCHAR(200),
--		HP VARCHAR(200),
--		HPNR VARCHAR(200),
--		STD_NAVN VARCHAR(200),
--		PNYT VARCHAR(200),
--		PERSFIL VARCHAR(200),
--		BEGIVFIL VARCHAR(200),
--		FADER VARCHAR(200),
--		FPNR VARCHAR(200),
--		MODER VARCHAR(200),
--		MPNR VARCHAR(200),
--		FQELLE VARCHAR(200),
--		FLPNR VARCHAR(200),
--		NAVN VARCHAR(200),
--		KBNR VARCHAR(200),
--		KILDE VARCHAR(200),
--		KBDEL VARCHAR(200),
--		TIFNR VARCHAR(200),
--		SIDE VARCHAR(200),
--		OPSLAG VARCHAR(200),
--		OPNR VARCHAR(200),
--		DATO VARCHAR(200),
--		REF VARCHAR(200),
--		FORNVN VARCHAR(200),
--		EFTERNVN VARCHAR(200),
--		SEX VARCHAR(200),
--		CIVILSTAND VARCHAR(200),
--		ERHVERV VARCHAR(200),
--		ALDER VARCHAR(200),
--		FQODT VARCHAR(200),
--		KVAL VARCHAR(200),
--		FQODESTED VARCHAR(200),
--		HM_ VARCHAR(200),
--		FQODTDATO VARCHAR(200),
--		HJDQOBT VARCHAR(200),
--		DQOBTSTED VARCHAR(200),
--		DQOBT VARCHAR(200),
--		KONF VARCHAR(200),
--		VIET VARCHAR(200),
--		DQOD VARCHAR(200),
--		BEGR VARCHAR(200),
--		FFNVN VARCHAR(200),
--		FENVN VARCHAR(200),
--		FERHV VARCHAR(200),
--		MFNVN VARCHAR(200),
--		MENVN VARCHAR(200),
--		MALDER VARCHAR(200),
--		FLFNVN VARCHAR(200),
--		FLENVN VARCHAR(200),
--		FLERHV VARCHAR(200),
--		FLKQON VARCHAR(200),
--		FLALDER VARCHAR(200),
--		FLSTED VARCHAR(200),
--		TEDKOD VARCHAR(200),
--		STEDNAVN VARCHAR(200),
--		SKOLEDISTR VARCHAR(200),
--		GAARD VARCHAR(200),
--		FAM0 VARCHAR(200),
--		SLGT0 VARCHAR(200),
--		FAM VARCHAR(200),
--		SLGT VARCHAR(200),
--		SLGT00 VARCHAR(200),
--		SLGT1 VARCHAR(200),
--		SLGT2 VARCHAR(200),
--		FAM1 VARCHAR(200),
--		FAM2 VARCHAR(200),
--		HUSSTAND VARCHAR(200),
--		MATR_ VARCHAR(200),
--		MATR1 VARCHAR(200),
--		RULLE VARCHAR(200),
--		TITEL0 VARCHAR(200),
--		ROLLE0 VARCHAR(200),
--		INIT0 VARCHAR(200),
--		TILG_REF VARCHAR(200),
--		AFG_REF VARCHAR(200),
--		FRA VARCHAR(200),
--		TIL VARCHAR(200),
--		BEM VARCHAR(200),
--		STILLING VARCHAR(200),
--		ANTFAM VARCHAR(200),
--		TRO VARCHAR(200),
--		HANDICAP VARCHAR(200),
--		FLYTTET VARCHAR(200),
--		GIFT VARCHAR(200),
--		LEVB VARCHAR(200),
--		DQODEB VARCHAR(200),
--		ERHVERVSSTED VARCHAR(200),
--		SIDSTEOPHOLD VARCHAR(200),
--		KILDEKOMMENTAR VARCHAR(200),
--		SLUT VARCHAR(200)
--	);