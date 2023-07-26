package net.myerichsen.blistrup.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import net.myerichsen.blistrup.models.IndividModel;

/**
 * Filter for født kolonnen i individ view
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class IndividFoedtFilter extends ViewerFilter {
	private static IndividFoedtFilter filter = null;

	/**
	 * @return
	 */
	public static IndividFoedtFilter getInstance() {
		if (filter == null) {
			filter = new IndividFoedtFilter();
		}

		return filter;

	}

	private String searchString;

	/**
	 * Constructor
	 *
	 */
	private IndividFoedtFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final IndividModel model = (IndividModel) element;

		if (model.getFoedt() != null && model.getFoedt().toString().matches(searchString)) {
			return true;
		}

		return false;
	}

	public void setSearchText(String s) {
		this.searchString = ".*" + s.toLowerCase() + ".*";
	}

}
