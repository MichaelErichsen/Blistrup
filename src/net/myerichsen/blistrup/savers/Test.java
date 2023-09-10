package net.myerichsen.blistrup.savers;

import java.util.HashMap;
import java.util.Map;

import org.gedcom4j.model.Gedcom;
import org.gedcom4j.model.Header;
import org.gedcom4j.model.SourceSystem;
import org.gedcom4j.model.Submitter;
import org.gedcom4j.model.Trailer;

/**
 * @author Michael Erichsen
 * @version 6. sep. 2023
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Test test = new Test();
		test.execute();
	}

	/**
	 *
	 */
	private void execute() {
		final Gedcom g = new Gedcom();
		final Header h = new Header();
		final SourceSystem s = new SourceSystem();
		s.setProductName("Blistrup Lokalhistorie");
		s.setVersionNum("1.0.0");
		h.setSourceSystem(s);
		final Submitter submitter = new Submitter();
		submitter.setName("Alex /Hvidberg/");
		final Map<String, Submitter> ms = new HashMap<>();
		ms.put("SUBM", submitter);
		g.setHeader(h);
		final Trailer t = new Trailer();
		g.setTrailer(t);

	}

}
