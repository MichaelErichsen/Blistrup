package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.FT1840Loader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 5. sep. 2023
 *
 */
public class FT1840Action extends Action {
	/**
	 *
	 */
	private final BlistrupLokalhistorie blh;

	/**
	 * Constructor
	 *
	 * @param blh
	 */
	public FT1840Action(BlistrupLokalhistorie blh) {
		super("FT 1840", AS_PUSH_BUTTON);
		this.blh = blh;
	}

	@Override
	public void run() {
		try {
			final int load = new FT1840Loader().load();
			blh.refresh();
			blh.getStatusLineManager().setMessage(load + " folketællinger for 1840 er indlæst");
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}