package net.myerichsen.blistrup.views;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import net.myerichsen.blistrup.models.IndividBegivenhedModel;

/**
 * @author Michael Erichsen
 * @version 21. jul. 2023
 *
 */
public class FamilieBegivenhedView extends Composite {
	private TableViewer tableViewer;
	private Table table;

	/**
	 * Constructor
	 *
	 * @param tabFolder
	 * @param none
	 * @param blistrupLokalhistorie
	 */
	public FamilieBegivenhedView(Composite parent, int style, BlistrupLokalhistorie blh) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));

		final Composite filterComposite = new Composite(this, SWT.BORDER);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		final Label cLabel = new Label(filterComposite, SWT.NONE);
		cLabel.setText("Filtre: Fornavn");

		final ScrolledComposite scroller = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

		tableViewer = new TableViewer(scroller, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

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

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnIndividid = tableViewerColumn_1.getColumn();
		tblclmnIndividid.setWidth(70);
		tblclmnIndividid.setText("IndividId");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((IndividBegivenhedModel) element).getId());
			}
		});

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnAlder = tableViewerColumn_2.getColumn();
		tblclmnAlder.setWidth(70);
		tblclmnAlder.setText("Alder");
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((IndividBegivenhedModel) element).getAlder());
			}
		});

		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnType = tableViewerColumn_3.getColumn();
		tblclmnType.setWidth(100);
		tblclmnType.setText("Type");
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getBegType();
			}
		});

		TableViewerColumn tableViewerColumn_3a = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnRolle = tableViewerColumn_3a.getColumn();
		tblclmnRolle.setWidth(100);
		tblclmnRolle.setText("Rolle");
		tableViewerColumn_3a.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getRolle();
			}
		});

		TableViewerColumn tableViewerColumn_3b = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDato = tableViewerColumn_3b.getColumn();
		tblclmnDato.setWidth(100);
		tblclmnDato.setText("Dato");
		tableViewerColumn_3b.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				return formatter.format(((IndividBegivenhedModel) element).getDato());
			}
		});

		TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnFdt = tableViewerColumn_4.getColumn();
		tblclmnFdt.setWidth(100);
		tblclmnFdt.setText("F\u00F8dt");
		tableViewerColumn_4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getFoedt();
			}
		});

		TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnStedNavn = tableViewerColumn_5.getColumn();
		tblclmnStedNavn.setWidth(100);
		tblclmnStedNavn.setText("Strednavn");
		tableViewerColumn_5.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getStedNavn();
			}
		});

		TableViewerColumn tableViewerColumn_6 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNote = tableViewerColumn_6.getColumn();
		tblclmnNote.setWidth(100);
		tblclmnNote.setText("Note");
		tableViewerColumn_6.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getNote();
			}
		});
		TableViewerColumn tableViewerColumn_7 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnDetaljer = tableViewerColumn_7.getColumn();
		tblclmnDetaljer.setWidth(100);
		tblclmnDetaljer.setText("Detaljer");
		tableViewerColumn_7.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IndividBegivenhedModel) element).getDetaljer();
			}
		});
		TableViewerColumn tableViewerColumn_8 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnBem = tableViewerColumn_8.getColumn();
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
			tableViewer.setInput(IndividBegivenhedModel.getData(dbPath));
//		blistrupLokalhistorie.getStatusLineManager().setMessage("OK");
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
