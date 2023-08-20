package net.myerichsen.blistrup.actions;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.savers.GedcomSaver;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 20. aug. 2023
 *
 */
public class GedcomSaveAction extends Action {
	/**
	 *
	 */
	private final BlistrupLokalhistorie blh;

	/**
	 * Constructor
	 *
	 * @param blh
	 */
	public GedcomSaveAction(BlistrupLokalhistorie blh) {
		super("Gem", AS_PUSH_BUTTON);
		this.blh = blh;
	}

	@Override
	public void run() {
		new GedcomSaver().save();
		blh.refresh();
		blh.getStatusLineManager().setMessage("GEDCOM er gemt");
	}
}
