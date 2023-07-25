package net.myerichsen.blistrup.views;

import java.sql.SQLException;

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

import net.myerichsen.blistrup.models.IndividModel;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class IndividView extends Composite {
	private TableViewer tableViewer;
	private Table table;

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
		cLabel.setText("Her kommer der filtre");

		final ScrolledComposite scroller = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scroller = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_scroller.heightHint = 539;
		scroller.setLayoutData(gd_scroller);
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
				return Integer.toString(((IndividModel) element).getId());
			}
		});

		final TableViewerColumn tableViewerColumn1 = new TableViewerColumn(tableViewer, SWT.NONE);
		final TableColumn tblclmnKoen = tableViewerColumn1.getColumn();
		tblclmnKoen.setWidth(40);
		tblclmnKoen.setText("K�n");
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

		final String dbPath = "C:\\Users\\michael\\BlistrupDB";
		try {
			tableViewer.setInput(IndividModel.getData(dbPath));
//			blh.getStatusLineManager().setMessage("OK");
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
}
