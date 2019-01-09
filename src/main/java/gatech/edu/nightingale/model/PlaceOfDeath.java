package gatech.edu.nightingale.model;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.primitive.StringDt;

@ResourceDef(name = "PlaceOfDeath", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-PlaceOfDeath-extension")
public class PlaceOfDeath {
	private static final long serialVersionUID = 1L;

	@Child(name = "PlaceOfDeathTypeExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-PlaceOfDeathType-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR PlaceOfDeathType Extension")
	private CodeableConceptDt placeOfDeathTypeExtension;
	@Child(name = "FacilityNameExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-PlaceOfDeathType-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR FacilityName Extension")
	private StringDt facilityNameExtension;
	@Child(name = "PostalAddressExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/shr-core-PostalAddress-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR PostalAddress Extension")
	private PostalAddress postalAddressExtension;

	public CodeableConceptDt getPlaceOfDeathTypeExtension() {
		return placeOfDeathTypeExtension;
	}

	public void setPlaceOfDeathTypeExtension(CodeableConceptDt placeOfDeathTypeExtension) {
		this.placeOfDeathTypeExtension = placeOfDeathTypeExtension;
	}

	public StringDt getFacilityNameExtension() {
		return facilityNameExtension;
	}

	public void setFacilityNameExtension(StringDt facilityNameExtension) {
		this.facilityNameExtension = facilityNameExtension;
	}

	public PostalAddress getPostalAddressExtension() {
		return postalAddressExtension;
	}

	public void setPostalAddressExtension(PostalAddress postalAddressExtension) {
		this.postalAddressExtension = postalAddressExtension;
	}
}