package net.myerichsen.blistrup.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.myerichsen.blistrup.models.IndividModel;

/**
 * Filter for ægtefællekolonnen i individ view
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class IndividAegtefaelleFilter extends ViewerFilter {
	private static IndividAegtefaelleFilter filter = null;

	/**
	 * @return
	 */
	public static IndividAegtefaelleFilter getInstance() {
		if (filter == null) {
			filter = new IndividAegtefaelleFilter();
		}

		return filter;

	}

	private String searchString;

	/**
	 * Constructor
	 *
	 */
	private IndividAegtefaelleFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final IndividModel model = (IndividModel) element;

		if (model.getSpouseNames().size() > 0 && model.getSpouseNames().get(0).toLowerCase().matches(searchString)) {
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
