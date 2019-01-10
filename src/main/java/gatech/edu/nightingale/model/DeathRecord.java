package gatech.edu.nightingale.model;

import org.hl7.fhir.dstu3.model.Bundle;

import ca.uhn.fhir.model.api.annotation.ResourceDef;

@ResourceDef(name = "DeathRecord", profile = "https://nightingaleproject.github.io/fhir-death-record/guide/StructureDefinition-sdr-deathRecord-DeathRecord.html")
public class DeathRecord extends Bundle {
	private static final long serialVersionUID = 1L;
}
