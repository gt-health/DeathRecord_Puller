package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;

@ResourceDef(name = "DetailsOfInjury", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-DetailsOfInjury")
public class DetailsOfInjury extends Observation {
	private static final long serialVersionUID = 1L;

	@Child(name = "PlaceOfInjuryExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-causeOfDeath-PlaceOfInjury-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR PlaceOfInjury Extension")
	private String placeOfInjuryExtension;
	@Child(name = "PostalAddressExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/shr-core-PostalAddress", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR PostalAddress Extension")
	private PostalAddress postalAddressExtension;

	public DetailsOfInjury() {
		super();
		this.setCode(new CodeableConceptDt("http://loinc.org", "11374-6"));
	}

	public DetailsOfInjury(ResourceReferenceDt subject, DateTimeDt effectiveDateTime, String placeOfInjury,
			PostalAddress postalAddress, StringDt valueString) {
		this();
		this.setSubject(subject);
		this.setEffective(effectiveDateTime);
		this.setPlaceOfInjuryExtension(placeOfInjury);
		this.setPostalAddressExtension(postalAddress);
		this.setValue(valueString);
	}

	public String getPlaceOfInjuryExtension() {
		return placeOfInjuryExtension;
	}

	public void setPlaceOfInjuryExtension(String placeOfInjury) {
		this.placeOfInjuryExtension = placeOfInjury;
	}

	public PostalAddress getPostalAddressExtension() {
		return postalAddressExtension;
	}

	public void setPostalAddressExtension(PostalAddress postalAddress) {
		this.postalAddressExtension = postalAddress;
	}

}