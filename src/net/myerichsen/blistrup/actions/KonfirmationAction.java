package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.KonfirmationLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
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
		super("Konfirmation", AS_PUSH_BUTTON);
		this.blistrupLokalhistorie = blistrupLokalhistorie;
	}

	@Override
	public void run() {
		try {
			final int load = new KonfirmationLoader().load();
			blistrupLokalhistorie.getStatusLineManager().setMessage(load + " konfirmationsregistreringer er indl�st");
		} catch (final SQLException e) {
			blistrupLokalhistorie.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

}
