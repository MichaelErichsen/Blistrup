package net.myerichsen.blistrup.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.myerichsen.blistrup.models.IndividModel;

/**
 * Filter for navnekolonnen i individ view
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class IndividNavneFilter extends ViewerFilter {
	private static IndividNavneFilter filter = null;

	/**
	 * @return
	 */
	public static IndividNavneFilter getInstance() {
		if (filter == null) {
			filter = new IndividNavneFilter();
		}

		return filter;

	}

	private String searchString;

	/**
	 * Constructor
	 *
	 */
	private IndividNavneFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final IndividModel model = (IndividModel) element;

		if (model.getStdNavn().toLowerCase().matches(searchString)) {
			return true;
		}

		return false;
	}

	public void setSearchText(String s) {
		this.searchString = ".*" + s.toLowerCase() + ".*";
	}

}
