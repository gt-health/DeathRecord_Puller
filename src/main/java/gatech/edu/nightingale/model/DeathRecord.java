package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.resource.Bundle;

@ResourceDef(name = "DeathRecord", profile = "https://nightingaleproject.github.io/fhir-death-record/guide/StructureDefinition-sdr-deathRecord-DeathRecord.html")
public class DeathRecord extends Bundle {
	private static final long serialVersionUID = 1L;
}
