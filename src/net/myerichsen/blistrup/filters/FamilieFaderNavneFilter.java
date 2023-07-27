package net.myerichsen.blistrup.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.myerichsen.blistrup.models.FamilieModel;

/**
 * Filter for Fadernavnekolonnen i familie view
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class FamilieFaderNavneFilter extends ViewerFilter {
	private static FamilieFaderNavneFilter filter = null;

	/**
	 * @return
	 */
	public static FamilieFaderNavneFilter getInstance() {
		if (filter == null) {
			filter = new FamilieFaderNavneFilter();
		}

		return filter;

	}

	private String searchString;

	/**
	 * Constructor
	 *
	 */
	private FamilieFaderNavneFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final FamilieModel model = (FamilieModel) element;

		if (model.getFaderNavn().toLowerCase().matches(searchString)) {
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
