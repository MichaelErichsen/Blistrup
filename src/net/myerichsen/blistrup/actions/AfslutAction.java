package net.myerichsen.blistrup.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.ApplicationWindow;

/**
 * @author Michael Erichsen
 * @version 21. jul. 2023
 *
 */
public class AfslutAction extends Action {
	ApplicationWindow win;

	/**
	 * Constructor
	 *
	 * @param aWin
	 */
	public AfslutAction(ApplicationWindow aWin) {
		super("Afslut@Alt+X", AS_PUSH_BUTTON);
		this.win = aWin;
	}

	@Override
	public void run() {
		this.win.close();
	}

}
