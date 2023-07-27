package net.myerichsen.blistrup.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.myerichsen.blistrup.models.FamilieModel;

/**
 * Filter for modernavnekolonnen i familie view
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class FamilieModerNavneFilter extends ViewerFilter {
	private static FamilieModerNavneFilter filter = null;

	/**
	 * @return
	 */
	public static FamilieModerNavneFilter getInstance() {
		if (filter == null) {
			filter = new FamilieModerNavneFilter();
		}

		return filter;

	}

	private String searchString;

	/**
	 * Constructor
	 *
	 */
	private FamilieModerNavneFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final FamilieModel model = (FamilieModel) element;

		if (model.getModerNavn().toLowerCase().matches(searchString)) {
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
