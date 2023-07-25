package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.DaabLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class DaabAction extends Action {
	/**
	 * 
	 */
	private final BlistrupLokalhistorie blh;

	/**
	 * Constructor
	 *
	 * @param blh
	 */
	public DaabAction(BlistrupLokalhistorie blh) {
		super("Dåb", AS_PUSH_BUTTON);
		this.blh = blh;
	}

	@Override
	public void run() {
		try {
			final int load = new DaabLoader().load();
			blh.getStatusLineManager().setMessage(load + " dåbsregistreringer er indlæst");
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}