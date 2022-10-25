epackage com.nuix.nx.controls.filters;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DynamicTableRegexFilter extends DynamicTableFilterProvider {
	private Pattern filterPattern;
	
	/***
	 * {@inheritDoc}<br>
	 * This is meant to be a fallback filter so it will happily accept any filter expression except when
	 * given value does not correctly compile to a regular expression.
	 */
	@Override
	public boolean handlesExpression(String filterExpression) {
		try {
			Pattern.compile(filterExpression, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean keepRecord(int sourceIndex, boolean isChecked, String filterExpression,
			Object record, Map<String,Object> rowValues) {
		boolean result = false;
		for(Map.Entry<String, Object> entry : rowValues.entrySet()) {
			// Easy way to get string value of most common types
			String stringValue = String.format("%s", entry.getValue());
			// Does string value match against our regex?
			if(filterPattern.matcher(stringValue).find()) {
				// Any match will be considered a success and keeps the record so we
				// do not need to keep looking
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public void beforeFiltering(String filterExpression, List<Object> allRecords) {
		this.filterPattern = Pattern.compile(Pattern.quote(filterExpression), Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void afterFiltering() {
		filterPattern = null;
	}

	
}
