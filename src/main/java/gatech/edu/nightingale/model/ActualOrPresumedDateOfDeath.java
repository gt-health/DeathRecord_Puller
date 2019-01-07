package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;

@ResourceDef(name = "ActualOrPresumedDateOfDeath", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-ActualOrPresumedDateOfDeath")
public class ActualOrPresumedDateOfDeath extends Observation {
	private static final long serialVersionUID = 1L;

	public ActualOrPresumedDateOfDeath() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "81956-5"));
	}

	public ActualOrPresumedDateOfDeath(DateTimeDt valueDateTime) {
		this();
		this.setValue(valueDateTime);
	}
}