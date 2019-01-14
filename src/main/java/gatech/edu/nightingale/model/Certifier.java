package gatech.edu.nightingale.model;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.CodeDt;

@ResourceDef(name = "Certifier", profile = "https://nightingaleproject.github.io/fhir-death-record/guide/StructureDefinition-sdr-deathRecord-Certifier.html")
public class Certifier extends Practitioner {
	private static final long serialVersionUID = 1L;

	@Child(name = "certifierTypeExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-deathRecord-CertifierType-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "Certifier Type Extension")
	private CodeDt certifierTypeExtension;
	
	public Certifier() {
		super();
	}

	public CodeDt getCertifierTypeExtension() {
		return certifierTypeExtension;
	}

	public void setCertifierTypeExtension(CodeDt certifierTypeExtension) {
		this.certifierTypeExtension = certifierTypeExtension;
	}

}