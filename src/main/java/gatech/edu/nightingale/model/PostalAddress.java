package gatech.edu.nightingale.model;

import org.hl7.fhir.dstu3.model.Address;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.primitive.BooleanDt;

@ResourceDef(name = "PostalAdress", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/shr-core-PostalAddress")
public class PostalAddress extends Address {
	private static final long serialVersionUID = 1L;

	@Child(name = "InsideCityLimitsExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/shr-core-InsideCityLimits-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR InsideCityLimits Extension")
	private BooleanDt insideCityLimits;

	public PostalAddress() {
		super();
		this.setType(AddressType.POSTAL);
	}

	public PostalAddress(ResourceReferenceDt subject, CodeableConceptDt valueCodeableConcept) {
		this();
		this.setInsideCityLimits(new BooleanDt(false));
	}

	public BooleanDt getInsideCityLimits() {
		return insideCityLimits;
	}

	public void setInsideCityLimits(BooleanDt insideCityLimits) {
		this.insideCityLimits = insideCityLimits;
	}
}