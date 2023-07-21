package net.myerichsen.blistrup.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import net.myerichsen.blistrup.actions.AfslutAction;

/**
 * Hovedvindue for Blistrup Lokalhistorie programmet
 * 
 * @author Michael Erichsen
 * @version 21. jul. 2023
 *
 */
public class BlistrupLokalhistorie extends ApplicationWindow {
	/**
	 * Launch the application.
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			final BlistrupLokalhistorie window = new BlistrupLokalhistorie();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private Action loadChristenings;
	private Action afslut;
	private TabFolder tabFolder;
	private IndividView individView;

	/**
	 * Create the application window.
	 */
	public BlistrupLokalhistorie() {
		super(null);
		createActions();
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Configure the shell.
	 *
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Blistrup Lokalhistorie");
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
		{
			afslut = new AfslutAction(this);
			loadChristenings = new Action("D\u00E5b") {

			};
		}
	}

	/**
	 * Create contents of the application window.
	 *
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Composite searchComposite = new Composite(container, SWT.NONE);
		searchComposite.setLayout(new GridLayout(1, false));

		Label lblNewLabel = new Label(searchComposite, SWT.NONE);
		lblNewLabel.setText("S\u00F8g");

		Composite tabComposite = new Composite(container, SWT.NONE);
		tabComposite.setLayout(new GridLayout(1, false));

		tabFolder = new TabFolder(tabComposite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final TabItem tbtmPerson = new TabItem(tabFolder, SWT.NONE);
		tbtmPerson.setText("&Person");
		individView = new IndividView(tabFolder, SWT.NONE, this);
		tbtmPerson.setControl(individView);

		return container;
	}

	/**
	 * Create the menu manager.
	 *
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		final MenuManager menuManager = new MenuManager();
		final MenuManager fileMenu = new MenuManager("&Filer");
		menuManager.add(fileMenu);
		fileMenu.add(afslut);

		final MenuManager loadMenu = new MenuManager("&Indl\u00E6s");
		menuManager.add(loadMenu);
		loadMenu.add(loadChristenings);

		return menuManager;
	}

	/**
	 * Create the status line manager.
	 *
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		final StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1145, 633);
	}

	@Override
	public StatusLineManager getStatusLineManager() {
		return super.getStatusLineManager();
	}
}
