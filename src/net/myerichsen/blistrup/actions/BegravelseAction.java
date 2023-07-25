package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.BegravelseLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class BegravelseAction extends Action {

	private final BlistrupLokalhistorie blistrupLokalhistorie;

	/**
	 * Constructor
	 *
	 * @param blistrupLokalhistorie
	 */
	public BegravelseAction(BlistrupLokalhistorie blistrupLokalhistorie) {
		super("Begravelser", AS_PUSH_BUTTON);
		this.blistrupLokalhistorie = blistrupLokalhistorie;
	}

	@Override
	public void run() {
		try {
			final int load = new BegravelseLoader().load();
			blistrupLokalhistorie.getStatusLineManager().setMessage(load + " begravelser er indlæst");
		} catch (final SQLException e) {
			blistrupLokalhistorie.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
