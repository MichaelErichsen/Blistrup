package net.myerichsen.blistrup.actions;

import org.eclipse.jface.action.Action;

import net.myerichsen.blistrup.loaders.VielseLoader;
import net.myerichsen.blistrup.views.BlistrupLokalhistorie;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class VielseAction extends Action {

	private final BlistrupLokalhistorie blistrupLokalhistorie;

	/**
	 * Constructor
	 *
	 * @param blistrupLokalhistorie
	 */
	public VielseAction(BlistrupLokalhistorie blistrupLokalhistorie) {
		super("Vielse", AS_PUSH_BUTTON);
		this.blistrupLokalhistorie = blistrupLokalhistorie;
	}

	@Override
	public void run() {
		try {
			final int load = new VielseLoader().load();
			blistrupLokalhistorie.getStatusLineManager().setMessage(load + " vielsesregistreringer er indlæst");
		} catch (final Exception e) {
			blistrupLokalhistorie.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

}
