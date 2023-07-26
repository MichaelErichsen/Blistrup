package net.myerichsen.blistrup.views;

import java.sql.SQLException;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.TableItem;

import net.myerichsen.blistrup.models.FamilieModel;

/**
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class FamilieView extends Composite {
	private TableViewer tableViewer;

	private Table table;

	/**
	 * Constructor
	 *
	 * @param parent
	 * @param style
	 */
	public FamilieView(Composite parent, int style, BlistrupLokalhistorie blh) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		final Composite filterComposite = new Composite(this, SWT.BORDER);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		final Label cLabel = new Label(filterComposite, SWT.NONE);
		cLabel.setText("Her kommer der filtre");

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
		tblclmnHusfadernavn.setText("Husfadernavn");
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieModel) element).getFaderNavn();
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
		tblclmnHusmodernavn.setText("Husmodernavn");
		tableViewerColumn_4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((FamilieModel) element).getModerNavn();
			}
		});

		scroller.setContent(table);
		scroller.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		final String dbPath = "C:\\Users\\michael\\BlistrupDB";
		try {
			tableViewer.setInput(FamilieModel.getData(dbPath));
			blh.getStatusLineManager().setMessage("OK");
		} catch (final SQLException e) {
			blh.getStatusLineManager().setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Display the popup
	 */
	private void displayPopup() {
		try {
			popup();
		} catch (final SQLException e1) {
			// TODO ((BlistrupLokalhistorie) ((TabFolder)
			// getParent()).getParent()).setMessage(e1.getMessage());
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

		String[] buttonArray = new String[] { "OK" };

		final MessageDialog dialog = new MessageDialog(getShell(), "Familier", null, sb.toString(),
				MessageDialog.INFORMATION, buttonArray, 0);
		dialog.open();

	}

}
