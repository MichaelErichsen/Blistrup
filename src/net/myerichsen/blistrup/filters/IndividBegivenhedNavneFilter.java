package net.myerichsen.blistrup.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.myerichsen.blistrup.models.IndividBegivenhedModel;

/**
 * Filter for navnekolonnen i individbegivenhed view
 *
 * @author Michael Erichsen
 * @version 28. aug. 2023
 *
 */
public class IndividBegivenhedNavneFilter extends ViewerFilter {
	private static IndividBegivenhedNavneFilter filter = null;

	/**
	 * @return
	 */
	public static IndividBegivenhedNavneFilter getInstance() {
		if (filter == null) {
			filter = new IndividBegivenhedNavneFilter();
		}

		return filter;

	}

	private String searchString;

	/**
	 * Constructor
	 *
	 */
	private IndividBegivenhedNavneFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final IndividBegivenhedModel model = (IndividBegivenhedModel) element;

		if (model.getStedNavn().toLowerCase().matches(searchString)) {
			return true;
		}

		return false;
	}

	public void setSearchText(String s) {
		if (s.isBlank()) {
			this.searchString = "";
		} else {
			this.searchString = ".*" + s.toLowerCase() + ".*";
		}
	}

}
