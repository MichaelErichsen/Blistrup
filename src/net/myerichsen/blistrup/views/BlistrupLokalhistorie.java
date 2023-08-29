package net.myerichsen.blistrup.views;

import java.sql.SQLException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import net.myerichsen.blistrup.actions.BegravelseAction;
import net.myerichsen.blistrup.actions.DaabAction;
import net.myerichsen.blistrup.actions.FolketaellingAction;
import net.myerichsen.blistrup.actions.GedcomSaveAction;
import net.myerichsen.blistrup.actions.KonfirmationAction;
import net.myerichsen.blistrup.actions.TabelRydningAction;
import net.myerichsen.blistrup.actions.VielseAction;

/**
 * Hovedvindue for Blistrup Lokalhistorie programmet
 *
 * @author Michael Erichsen
 * @version 29. aug. 2023
 *
 */
// TODO Add load buttons to each view and remove initial load
public class BlistrupLokalhistorie extends ApplicationWindow {
	private static final String dbPath = "C:\\Users\\michael\\BlistrupDB";

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

	private Action afslut;
	private TabFolder tabFolder;
	private IndividView individView;
	private FamilieView familieView;
	private IndividBegivenhedView individBegivenhedView;
	private FamilieBegivenhedView familieBegivenhedView;

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
		afslut = new AfslutAction(this);
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

		final Composite searchComposite = new Composite(container, SWT.NONE);
		searchComposite.setLayout(new GridLayout(1, false));

		final Label lblNewLabel = new Label(searchComposite, SWT.NONE);
		lblNewLabel.setText("S\u00F8g");

		tabFolder = new TabFolder(container, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final TabItem tbtmPerson = new TabItem(tabFolder, SWT.NONE);
		tbtmPerson.setText("&Person");
		individView = new IndividView(tabFolder, SWT.NONE, this);
		tbtmPerson.setControl(individView);

		final TabItem tbtmFamilie = new TabItem(tabFolder, SWT.NONE);
		tbtmFamilie.setText("&Familie");
		familieView = new FamilieView(tabFolder, SWT.NONE, this);
		tbtmFamilie.setControl(familieView);

		final TabItem tbtmIndividualEvent = new TabItem(tabFolder, SWT.NONE);
		tbtmIndividualEvent.setText("&Individbegivenhed");
		individBegivenhedView = new IndividBegivenhedView(tabFolder, SWT.NONE, this);
		tbtmIndividualEvent.setControl(individBegivenhedView);

		final TabItem tbtmFamilieEvent = new TabItem(tabFolder, SWT.NONE);
		tbtmFamilieEvent.setText("F&amiliebegivenhed");
		familieBegivenhedView = new FamilieBegivenhedView(tabFolder, SWT.NONE, this);
		tbtmFamilieEvent.setControl(familieBegivenhedView);

		return container;
	}

	/**
	 * Create the menu manager.
	 *
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		final MenuManager mainMenu = new MenuManager();
		final MenuManager fileMenu = new MenuManager("&Filer");
		final MenuManager loadMenu = new MenuManager("&Indl\u00E6s");
		final MenuManager saveMenu = new MenuManager("&Gem");

		fileMenu.add(afslut);

		loadMenu.add(new DaabAction(this));
		loadMenu.add(new KonfirmationAction(this));
		loadMenu.add(new VielseAction(this));
		loadMenu.add(new BegravelseAction(this));
		loadMenu.add(new FolketaellingAction(this));
		loadMenu.add(new Separator());
		loadMenu.add(new TabelRydningAction(this));

		saveMenu.add(new GedcomSaveAction(this));

		mainMenu.add(fileMenu);
		mainMenu.add(loadMenu);
		mainMenu.add(saveMenu);

		return mainMenu;
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

	/**
	 *
	 */
	public void refresh() {
		try {
			individView.refresh(dbPath);
			familieView.refresh(dbPath);
			individBegivenhedView.refresh(dbPath);
			familieBegivenhedView.refresh(dbPath);
		} catch (final SQLException e) {
		}

	}
}
