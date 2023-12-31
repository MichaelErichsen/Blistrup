package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.KonfirmationLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class KonfirmationAction extends Action {

	private final BlistrupLokalhistorie blistrupLokalhistorie;

	/**
	 * Constructor
	 *
	 * @param blistrupLokalhistorie
	 */
	public KonfirmationAction(BlistrupLokalhistorie blistrupLokalhistorie) {
		super("Konfirmationer", AS_PUSH_BUTTON);
		this.blistrupLokalhistorie = blistrupLokalhistorie;
	}

	@Override
	public void run() {
		try {
			final int load = new KonfirmationLoader().load();
			blistrupLokalhistorie.refresh();
			blistrupLokalhistorie.getStatusLineManager().setMessage(load + " konfirmationsregistreringer er indl�st");
		} catch (final SQLException e) {
			blistrupLokalhistorie.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

}
