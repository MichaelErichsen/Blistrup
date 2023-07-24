package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.DaabLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 24. jul. 2023
 *
 */
class DaabAction extends Action {
	BlistrupLokalhistorie win;

	/**
	 * Constructor
	 *
	 * @param aWin
	 */
	public DaabAction(BlistrupLokalhistorie aWin) {
		super("Daab", AS_PUSH_BUTTON);
		this.win = aWin;
	}

	@Override
	public void run() {
		try {
			final int load = new DaabLoader().load();
			win.getStatusLineManager().setMessage(load + " dåbsregistreringer er indlæst");
		} catch (final SQLException e) {
			win.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
