package gatech.edu.nightingale.model.valuesets;

import java.util.Map;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;

public class ValueSetUtil {
	public static void addMapping(Map<String, CodeableConceptDt> map, String basicName, String code, String system) {
		CodeableConceptDt codeableConcept = new CodeableConceptDt(system, code);
		map.put(basicName, codeableConcept);
	}
}
