package net.myerichsen.blistrup.views;

import java.sql.SQLException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import net.myerichsen.blistrup.filters.FamilieFaderNavneFilter;
import net.myerichsen.blistrup.filters.FamilieModerNavneFilter;
import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividModel;

/**
 * @author Michael Erichsen
 * @version 27. jul. 2023
 *
 */
public class FamilieView extends Composite {
	private static final String dbPath = "C:\\Users\\michael\\BlistrupDB";
	private TableViewer tableViewer;
	private Table table;
	private BlistrupLokalhistorie blh;
	private Text faderNavneFiltertext;
	private Text moderNavneFiltertext;

	/**
	 * Constructor
	 *
	 * @param parent
	 * @param style
	 */
	public FamilieView(Composite parent, int style, BlistrupLokalhistorie blh) {
		super(parent, style);
		this.blh = blh;
		setLayout(new GridLayout(1, false));

		final Composite filterComposite = new Composite(this, SWT.BORDER);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		final Label cLabel = new Label(filterComposite, SWT.NONE);
		cLabel.setText("Filtre: Fadernavn");

		faderNavneFiltertext = new Text(filterComposite, SWT.BORDER);
		faderNavneFiltertext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (faderNavneFiltertext.getText().length() > 0) {
					faderNavneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				} else {
					faderNavneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				}
				FamilieFaderNavneFilter.getInstance().setSearchText(faderNavneFiltertext.getText());
				tableViewer.refresh();
			}
		});

		final Label dLabel = new Label(filterComposite, SWT.NONE);
		dLabel.setText("Modernavn");

		moderNavneFiltertext = new Text(filterComposite, SWT.BORDER);
		moderNavneFiltertext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (moderNavneFiltertext.getText().length() > 0) {
					moderNavneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				} else {
					moderNavneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				}
				FamilieModerNavneFilter.getInstance().setSearchText(moderNavneFiltertext.getText());
				tableViewer.refresh();
			}
		});

		final Button btnRydFelterne = new Button(filterComposite, SWT.NONE);
		btnRydFelterne.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearFilters();
			}
		});
		btnRydFelterne.setText("Ryd felterne");

		final ScrolledComposite scroller = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		final GridData gd_scroller = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scroller.heightHint = 539;
		scroller.setLayoutData(gd_scroller);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

		tableViewer = new TableViewer(scroller, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.addDoubleClickListener(event -> displayPopup());
		final ViewerFilter[] filters = new ViewerFilter[2];
		filters[0] = FamilieFaderNavneFilter.getInstance();
		filters[1] = FamilieModerNavneFilter.getInstance();
		tableViewer.setFilters(filters);

		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnId = tableViewerColumn.getColumn();
		tblclmnId.setWidth(50);
		tblclmnId.setText("ID");
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((FamilieModel) element).getId());
			}
		});

		final TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnHusfader = tableViewerColumn_1.getColumn();
		tblclmnHusfader.setWidth(70);
		tblclmnHusfader.setText("Husfader");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((FamilieModel) element).getFader());
			}
		});

		final TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnHusfadernavn = tableViewerColumn_2.getColumn();
		tblclmnHusfadernavn.setWidth(200);
		tblclmnHusfadernavn.setText("Navn");
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieModel) element).getFaderNavn();
			}
		});

		final TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnHusfaderFoedt = tableViewerColumn_5.getColumn();
		tblclmnHusfaderFoedt.setWidth(100);
		tblclmnHusfaderFoedt.setText("F\u00F8dt");
		tableViewerColumn_5.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieModel) element).getFaderFoedt();
			}
		});

		final TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnHusmoder = tableViewerColumn_3.getColumn();
		tblclmnHusmoder.setWidth(70);
		tblclmnHusmoder.setText("Husmoder");
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((FamilieModel) element).getModer());
			}
		});

		final TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnHusmodernavn = tableViewerColumn_4.getColumn();
		tblclmnHusmodernavn.setWidth(200);
		tblclmnHusmodernavn.setText("Navn");
		tableViewerColumn_4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieModel) element).getModerNavn();
			}
		});

		final TableViewerColumn tableViewerColumn_6 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnHusmoderFoedt = tableViewerColumn_6.getColumn();
		tblclmnHusmoderFoedt.setWidth(100);
		tblclmnHusmoderFoedt.setText("F\u00F8dt");
		tableViewerColumn_6.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieModel) element).getModerFoedt();
			}
		});

		final TableViewerColumn tableViewerColumn_7 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnBrn = tableViewerColumn_7.getColumn();
		tblclmnBrn.setWidth(300);
		tblclmnBrn.setText("B\u00F8rn");
		tableViewerColumn_7.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final FamilieModel model = (FamilieModel) element;
				final StringBuilder sb = new StringBuilder();

				for (final IndividModel iModel : model.getBoern()) {
					sb.append(iModel.getStdNavn() + ", ");
				}
				return sb.toString();
			}
		});

		scroller.setContent(table);
		scroller.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		try {
			refresh(dbPath);
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
		}
	}

	/**
	 * Clear filters
	 *
	 * @throws SQLException
	 */
	protected void clearFilters() {
		FamilieFaderNavneFilter.getInstance().setSearchText("");
		FamilieModerNavneFilter.getInstance().setSearchText("");
		faderNavneFiltertext.setText("");
		moderNavneFiltertext.setText("");
		faderNavneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		moderNavneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		tableViewer.refresh();
	}

	/**
	 * Display the popup
	 */
	private void displayPopup() {
		try {
			popup();
		} catch (final SQLException e1) {
			blh.getStatusLineManager().setErrorMessage(e1.getMessage());
		}
	}

	/**
	 * Create the popup
	 *
	 * @throws SQLException
	 */
	private void popup() throws SQLException {
		final TableItem[] tia = table.getSelection();
		final FamilieModel model = (FamilieModel) tia[0].getData();
		final StringBuilder sb = new StringBuilder(model.toString());
		sb.append("\n\n");

		final String[] buttonArray = new String[] { "OK" };

		final MessageDialog dialog = new MessageDialog(getShell(), "Familier", null, sb.toString(),
				MessageDialog.INFORMATION, buttonArray, 0);
		dialog.open();

	}

	/**
	 * @throws SQLException
	 *
	 */
	public void refresh(String dbPath) throws SQLException {
		tableViewer.setInput(FamilieModel.getData(dbPath));

	}

}
