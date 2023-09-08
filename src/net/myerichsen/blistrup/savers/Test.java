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
		// TODO Auto-generated method stub
		Test test = new Test();
		test.execute();
	}

	/**
	 * 
	 */
	private void execute() {
		Gedcom g = new Gedcom();
		Header h = new Header();
		SourceSystem s = new SourceSystem();
		s.setProductName("Blistrup Lokalhistorie");
		s.setVersionNum("1.0.0");
		h.setSourceSystem(s);
		Submitter submitter = new Submitter();
		submitter.setName("Alex /Hvidberg/");
		Map<String, Submitter> ms = new HashMap<String, Submitter>();
		ms.put("SUBM", submitter);
		g.setHeader(h);
		Trailer t = new Trailer();
		g.setTrailer(t);

	}

}
