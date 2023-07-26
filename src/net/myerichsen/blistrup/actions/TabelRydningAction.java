package net.myerichsen.blistrup.actions;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.ClearTables;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class TabelRydningAction extends Action {

	private final BlistrupLokalhistorie blistrupLokalhistorie;

	/**
	 * Constructor
	 *
	 * @param blistrupLokalhistorie
	 */
	public TabelRydningAction(BlistrupLokalhistorie blistrupLokalhistorie) {
		super("Ryd tabellerne", AS_PUSH_BUTTON);
		this.blistrupLokalhistorie = blistrupLokalhistorie;
	}

	@Override
	public void run() {
		try {
			new ClearTables().clear();
			blistrupLokalhistorie.refresh();
			blistrupLokalhistorie.getStatusLineManager().setMessage("Tabellerne er ryddet");
		} catch (final SQLException e) {
			blistrupLokalhistorie.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
