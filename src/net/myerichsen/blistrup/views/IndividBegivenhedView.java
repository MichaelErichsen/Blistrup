package net.myerichsen.blistrup.views;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import net.myerichsen.blistrup.filters.IndividBegivenhedNavneFilter;
import net.myerichsen.blistrup.models.IndividBegivenhedModel;

/**
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class IndividBegivenhedView extends Composite {
	private TableViewer tableViewer;
	private Table table;
	private Text navneFiltertext;

	/**
	 * Constructor
	 *
	 * @param parent
	 * @param style
	 * @param blh
	 */
	public IndividBegivenhedView(Composite parent, int style, BlistrupLokalhistorie blh) {
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
				IndividBegivenhedNavneFilter.getInstance().setSearchText(navneFiltertext.getText());
				tableViewer.refresh();
			}
		});

		final ScrolledComposite scroller = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

		tableViewer = new TableViewer(scroller, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		final ViewerFilter[] filters = new ViewerFilter[1];
		filters[0] = IndividBegivenhedNavneFilter.getInstance();
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
				return Integer.toString(((IndividBegivenhedModel) element).getId());
			}
		});

		final TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnIndividid = tableViewerColumn_1.getColumn();
		tblclmnIndividid.setWidth(70);
		tblclmnIndividid.setText("IndividId");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((IndividBegivenhedModel) element).getIndividId());
			}
		});

		final TableViewerColumn tableViewerColumn_1a = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnIndividNavn = tableViewerColumn_1a.getColumn();
		tblclmnIndividNavn.setWidth(200);
		tblclmnIndividNavn.setText("Navn");
		tableViewerColumn_1a.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getStdNavn();
			}
		});

		final TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnAlder = tableViewerColumn_2.getColumn();
		tblclmnAlder.setWidth(70);
		tblclmnAlder.setText("Alder");
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((IndividBegivenhedModel) element).getAlder());
			}
		});

		final TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnType = tableViewerColumn_3.getColumn();
		tblclmnType.setWidth(100);
		tblclmnType.setText("Type");
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getBegType();
			}
		});

		final TableViewerColumn tableViewerColumn_3a = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnRolle = tableViewerColumn_3a.getColumn();
		tblclmnRolle.setWidth(100);
		tblclmnRolle.setText("Rolle");
		tableViewerColumn_3a.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getRolle();
			}
		});

		final TableViewerColumn tableViewerColumn_3b = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnDato = tableViewerColumn_3b.getColumn();
		tblclmnDato.setWidth(100);
		tblclmnDato.setText("Dato");
		tableViewerColumn_3b.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				return formatter.format(((IndividBegivenhedModel) element).getDato());
			}
		});

		final TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnFdt = tableViewerColumn_4.getColumn();
		tblclmnFdt.setWidth(100);
		tblclmnFdt.setText("F\u00F8dt");
		tableViewerColumn_4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getFoedt();
			}
		});

		final TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnStedNavn = tableViewerColumn_5.getColumn();
		tblclmnStedNavn.setWidth(100);
		tblclmnStedNavn.setText("Stednavn");
		tableViewerColumn_5.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getStedNavn();
			}
		});

		final TableViewerColumn tableViewerColumn_6 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnNote = tableViewerColumn_6.getColumn();
		tblclmnNote.setWidth(100);
		tblclmnNote.setText("Note");
		tableViewerColumn_6.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getNote();
			}
		});
		final TableViewerColumn tableViewerColumn_7 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnDetaljer = tableViewerColumn_7.getColumn();
		tblclmnDetaljer.setWidth(100);
		tblclmnDetaljer.setText("Detaljer");
		tableViewerColumn_7.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getDetaljer();
			}
		});
		final TableViewerColumn tableViewerColumn_8 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnBem = tableViewerColumn_8.getColumn();
		tblclmnBem.setWidth(100);
		tblclmnBem.setText("Bemærkninger");
		tableViewerColumn_8.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getBem();
			}
		});
		scroller.setContent(table);
		scroller.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		final String dbPath = "C:\\Users\\michael\\BlistrupDB";
		try {
			refresh(dbPath);
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @throws SQLException
	 *
	 */
	public void refresh(String dbPath) throws SQLException {
		tableViewer.setInput(IndividBegivenhedModel.getData(dbPath));

	}
}
