package net.myerichsen.blistrup.actions;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.VielseLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 24. jul. 2023
 *
 */
class VielseAction extends Action {
	BlistrupLokalhistorie win;

	/**
	 * Constructor
	 *
	 * @param aWin
	 */
	public VielseAction(BlistrupLokalhistorie aWin) {
		super("Vielse", AS_PUSH_BUTTON);
		this.win = aWin;
	}

	@Override
	public void run() {
		try {
			final int load = new VielseLoader().load();
			win.getStatusLineManager().setMessage(load + " vielsesregistreringer er indlæst");
		} catch (final Exception e) {
			win.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
