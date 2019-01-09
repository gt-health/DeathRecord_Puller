package gatech.edu.nightingale.model.valuesets;

import java.util.Map;
import java.util.TreeMap;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;

public class BaseValueSet {
	public static Map<String, CodeableConceptDt> map;

	public BaseValueSet() {
		map = new TreeMap<String, CodeableConceptDt>(String.CASE_INSENSITIVE_ORDER);
	}
}
