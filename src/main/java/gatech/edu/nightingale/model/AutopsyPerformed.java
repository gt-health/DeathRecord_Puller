package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.BooleanDt;

@ResourceDef(name = "AutopsyPerformed", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-AutopsyPerformed")
public class AutopsyPerformed extends Observation {
	private static final long serialVersionUID = 1L;

	public AutopsyPerformed() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "85699-7"));
	}

	public AutopsyPerformed(BooleanDt valueBoolean) {
		this();
		this.setValue(valueBoolean);
	}
}