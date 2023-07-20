package net.myerichsen.blistrup.views;

import java.sql.SQLException;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.myerichsen.blistrup.models.IndividModel;

/**
 * @author michael
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

	private Table table;
	private final String dbPath = "C:\\Users\\michael\\BlistrupDB";

	/**
	 * Create the application window.
	 */
	public BlistrupLokalhistorie() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
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

		final ScrolledComposite scroller = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

		// TODO Træk ud i separate view klasser
//		tabFolder = new TabFolder(this, SWT.NONE);
//		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//
//		final TabItem tbtmPerson = new TabItem(tabFolder, SWT.NONE);
//		tbtmPerson.setText("&Person");
//		individualView = new IndividualView(tabFolder, SWT.NONE);
//		tbtmPerson.setControl(individualView);

		final TableViewer tableViewer = new TableViewer(scroller, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnId = tableViewerColumn.getColumn();
		tblclmnId.setWidth(70);
		tblclmnId.setText("Id");
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((IndividModel) element).getId());
			}
		});

		final TableViewerColumn tableViewerColumn1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnKoen = tableViewerColumn1.getColumn();
		tblclmnKoen.setWidth(40);
		tblclmnKoen.setText("Køn");
		tableViewerColumn1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividModel) element).getKoen();
			}
		});

		final TableViewerColumn tableViewerColumn2 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnFornavn = tableViewerColumn2.getColumn();
		tblclmnFornavn.setWidth(200);
		tblclmnFornavn.setText("Fornavn");
		tableViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividModel) element).getFornavn();
			}
		});

		final TableViewerColumn tableViewerColumn3 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnEfternavn = tableViewerColumn3.getColumn();
		tblclmnEfternavn.setWidth(200);
		tblclmnEfternavn.setText("Efternavn");
		tableViewerColumn3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividModel) element).getEfternavn();
			}
		});

		final TableViewerColumn tableViewerColumn4 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnBlistrupId = tableViewerColumn4.getColumn();
		tblclmnBlistrupId.setWidth(100);
		tblclmnBlistrupId.setText("Blistrup ID");
		tableViewerColumn4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividModel) element).getBlistrupId();
			}
		});

		final TableViewerColumn tableViewerColumn5 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnFon = tableViewerColumn5.getColumn();
		tblclmnFon.setWidth(200);
		tblclmnFon.setText("Fonetisk navn");
		tableViewerColumn5.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividModel) element).getFonetiskNavn();
			}
		});

		final TableViewerColumn tableViewerColumn6 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnFamc = tableViewerColumn6.getColumn();
		tblclmnFamc.setWidth(70);
		tblclmnFamc.setText("Familie ID");
		tableViewerColumn6.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((IndividModel) element).getFamc());
			}
		});

		scroller.setContent(table);
		scroller.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		try {
			tableViewer.setInput(IndividModel.load(dbPath));
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		getStatusLineManager().setMessage("OK");

		return container;
	}

	/**
	 * Create the menu manager.
	 *
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		final MenuManager menuManager = new MenuManager("menu");
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
	 * Create the toolbar manager.
	 *
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		final ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(991, 492);
	}
}
