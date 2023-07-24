package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.KonfirmationLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 24. jul. 2023
 *
 */
class KonfirmationAction extends Action {
	BlistrupLokalhistorie win;

	/**
	 * Constructor
	 *
	 * @param aWin
	 */
	public KonfirmationAction(BlistrupLokalhistorie aWin) {
		super("Konfirmation", AS_PUSH_BUTTON);
		this.win = aWin;
	}

	@Override
	public void run() {
		try {
			final int load = new KonfirmationLoader().load();
			win.getStatusLineManager().setMessage(load + " konfirmationsregistreringer er indlæst");
		} catch (final SQLException e) {
			win.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
