package net.myerichsen.blistrup.views;

import java.sql.SQLException;
import java.util.List;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import net.myerichsen.blistrup.filters.IndividAegtefaelleFilter;
import net.myerichsen.blistrup.filters.IndividFoedtFilter;
import net.myerichsen.blistrup.filters.IndividNavneFilter;
import net.myerichsen.blistrup.models.IndividModel;

/**
 * @author Michael Erichsen
 * @version 22. aug. 2023
 *
 */
public class IndividView extends Composite {
	final String dbPath = "C:\\Users\\michael\\BlistrupDB";
	private TableViewer tableViewer;
	private Table table;
//	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private Text navneFiltertext;
	private Text aegtefaelleFilterText;
	private Text foedtFilterText;

	/**
	 * Constructor
	 *
	 * @param parent
	 * @param style
	 */
	public IndividView(Composite parent, int style, BlistrupLokalhistorie blh) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		final Composite filterComposite = new Composite(this, SWT.BORDER);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		final Label cLabel = new Label(filterComposite, SWT.NONE);
		cLabel.setText("Filtre: Navn");

		navneFiltertext = new Text(filterComposite, SWT.BORDER);
		navneFiltertext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (navneFiltertext.getText().length() > 0) {
					navneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				} else {
					navneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				}
				IndividNavneFilter.getInstance().setSearchText(navneFiltertext.getText());
				tableViewer.refresh();
			}
		});

		final Label lblgteflle = new Label(filterComposite, SWT.NONE);
		lblgteflle.setText("\u00C6gtef\u00E6lle");

		aegtefaelleFilterText = new Text(filterComposite, SWT.BORDER);
		aegtefaelleFilterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (aegtefaelleFilterText.getText().length() > 0) {
					aegtefaelleFilterText.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				} else {
					aegtefaelleFilterText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				}
				IndividAegtefaelleFilter.getInstance().setSearchText(aegtefaelleFilterText.getText());
				tableViewer.refresh();
			}
		});

		final Label lblFdt = new Label(filterComposite, SWT.NONE);
		lblFdt.setText("F\u00F8dt");

		foedtFilterText = new Text(filterComposite, SWT.BORDER);
		foedtFilterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (foedtFilterText.getText().length() > 0) {
					foedtFilterText.setBackground(SWTResourceManager.getColor(SWT.COLOR_YELLOW));
				} else {
					foedtFilterText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
				}
				IndividFoedtFilter.getInstance().setSearchText(foedtFilterText.getText());
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
		final ViewerFilter[] filters = new ViewerFilter[3];
		filters[0] = IndividNavneFilter.getInstance();
		filters[1] = IndividAegtefaelleFilter.getInstance();
		filters[2] = IndividFoedtFilter.getInstance();
		tableViewer.setFilters(filters);

		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

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
		tblclmnFornavn.setWidth(300);
		tblclmnFornavn.setText("Navn");
		tableViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividModel) element).getStdNavn();
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
		tblclmnFon.setWidth(150);
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
				IndividModel model = ((IndividModel) element);
				int famc = model.getFamc();

				if (famc > 0) {
					return Integer.toString(model.getFamc());
				} else if (model.getFams().size() > 0) {
					return Integer.toString(model.getFams().get(0));
				} else
					return "0";
			}
		});

		final TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmngteflle = tableViewerColumn_1.getColumn();
		tblclmngteflle.setWidth(166);
		tblclmngteflle.setText("\u00C6gtef\u00E6lle");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				final List<String> liste = ((IndividModel) element).getSpouseNames();

				if (liste.size() > 0) {
					return liste.get(0);
				}
				return "";
			}
		});

		final TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnFdt = tableViewerColumn_2.getColumn();
		tblclmnFdt.setWidth(62);
		tblclmnFdt.setText("F\u00F8dt");
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final String foedt = ((IndividModel) element).getFoedt();

				if (foedt != null) {
					return foedt.trim();
//					return formatter.format(foedt);
				}
				return "";

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
		IndividAegtefaelleFilter.getInstance().setSearchText("");
		IndividFoedtFilter.getInstance().setSearchText("");
		IndividNavneFilter.getInstance().setSearchText("");
		navneFiltertext.setText("");
		aegtefaelleFilterText.setText("");
		foedtFilterText.setText("");
		navneFiltertext.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		aegtefaelleFilterText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		foedtFilterText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		tableViewer.refresh();
	}

	/**
	 * @throws SQLException
	 *
	 */
	public void refresh(String dbPath) throws SQLException {
		tableViewer.setInput(IndividModel.getData(dbPath));

	}
}
