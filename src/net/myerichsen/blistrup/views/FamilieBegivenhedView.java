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

import net.myerichsen.blistrup.models.FamilieBegivenhedModel;

/**
 * @author Michael Erichsen
 * @version 27. jul. 2023
 *
 */
public class FamilieBegivenhedView extends Composite {
	private TableViewer tableViewer;
	private Table table;

	/**
	 * Constructor
	 *
	 * @param parent
	 * @param style
	 * @param blh
	 */
	public FamilieBegivenhedView(Composite parent, int style, BlistrupLokalhistorie blh) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		final Composite filterComposite = new Composite(this, SWT.BORDER);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		final Label cLabel = new Label(filterComposite, SWT.NONE);
		cLabel.setText("Her kommer der filtre");

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
				return Integer.toString(((FamilieBegivenhedModel) element).getId());
			}
		});

		final TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnBegType = tableViewerColumn_1.getColumn();
		tblclmnBegType.setWidth(70);
		tblclmnBegType.setText("Type");
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getBegType();
			}
		});

		final TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnSubType = tableViewerColumn_2.getColumn();
		tblclmnSubType.setWidth(70);
		tblclmnSubType.setText("Undertype");
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getUnderType();
			}
		});

		final TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnType = tableViewerColumn_3.getColumn();
		tblclmnType.setWidth(100);
		tblclmnType.setText("Dato");
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				return formatter.format(((FamilieBegivenhedModel) element).getDato());
			}
		});

		final TableViewerColumn tableViewerColumn_3a = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnRolle = tableViewerColumn_3a.getColumn();
		tblclmnRolle.setWidth(100);
		tblclmnRolle.setText("Rolle");
		tableViewerColumn_3a.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getRolle();
			}
		});

		final TableViewerColumn tableViewerColumn_3b = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnKildeId = tableViewerColumn_3b.getColumn();
		tblclmnKildeId.setWidth(100);
		tblclmnKildeId.setText("Kilde id");
		tableViewerColumn_3b.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return Integer.toString(((FamilieBegivenhedModel) element).getKildeId());
			}
		});

		final TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnNote = tableViewerColumn_4.getColumn();
		tblclmnNote.setWidth(100);
		tblclmnNote.setText("Note");
		tableViewerColumn_4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getNote();
			}
		});

		final TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnStedNavn = tableViewerColumn_5.getColumn();
		tblclmnStedNavn.setWidth(100);
		tblclmnStedNavn.setText("Detaljer");
		tableViewerColumn_5.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getDetaljer();
			}
		});

		final TableViewerColumn tableViewerColumn_6 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnBlistrupId = tableViewerColumn_6.getColumn();
		tblclmnBlistrupId.setWidth(100);
		tblclmnBlistrupId.setText("Blistrup ID");
		tableViewerColumn_6.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getBlistrupId();
			}
		});
		final TableViewerColumn tableViewerColumn_7 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnDetaljer = tableViewerColumn_7.getColumn();
		tblclmnDetaljer.setWidth(100);
		tblclmnDetaljer.setText("Stednavn");
		tableViewerColumn_7.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getStedNavn();
			}
		});
		final TableViewerColumn tableViewerColumn_8 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnBem = tableViewerColumn_8.getColumn();
		tblclmnBem.setWidth(100);
		tblclmnBem.setText("Bemærkninger");
		tableViewerColumn_8.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieBegivenhedModel) element).getBem();
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
		tableViewer.setInput(FamilieBegivenhedModel.getData(dbPath));

	}
}
