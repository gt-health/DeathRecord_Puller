package gatech.edu.nightingale.model;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Patient;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.model.primitive.UnsignedIntDt;

@ResourceDef(name = "Decedent", profile = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-Decedent")
public class Decedent extends Patient {
	private static final long serialVersionUID = 1L;

	@Child(name = "birthSex")
	@Extension(url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "US Core BirthSex Extension")
	// TODO: Add valueset
	// http://hl7.org/fhir/us/core/1.0.1/ValueSet-us-core-birthsex.html
	private CodeDt birthSex;
	@Child(name = "enthnicity")
	@Extension(url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "US Core Ethnicity Extension")
	// TODO: MAP TO US-CORE PROPERLY INSTEAD OF USING A CODING
	private Coding ethnicity;
	@Child(name = "Race")
	@Extension(url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "US Core Race Extension")
	// TODO: MAP TO US-CORE PROPERLY INSTEAD OF USING A CODING
	private Coding race;
	@Child(name = "AgeExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-Age-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR Age Extension")
	private UnsignedIntDt ageExtension;
	@Child(name = "BirthplaceExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-Birthplace-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR Birthplace Extension")
	private PostalAddress birthplaceExtension;
	@Child(name = "ServedInArmedForcesExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-ServedInArmedForces-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR ServedInArmedForces Extension")
	private BooleanType servedInArmedForcesExtension;
	@Child(name = "MaritalStatusAtDeathExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-ServedInArmedForces-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR MaritalStatusAtDeath Extension")
	private CodeableConcept maritalStatusAtDeathExtension;
	@Child(name = "PlaceOfDeathExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-PlaceOfDeath-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR PlaceOfDeath Extension")
	private PlaceOfDeath placeOfDeathExtension;
	@Child(name = "DispositionExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-Disposition-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR Disposition Extension")
	private Disposition dispositionExtension;
	@Child(name = "EducationExtension")
	@Extension(url = "http://nightingaleproject.github.io/fhirDeathRecord/StructureDefinition/sdr-decedent-Education-extension", definedLocally = true, isModifier = false)
	@Description(shortDefinition = "SDR Education Extension")
	private CodeableConcept educationExtension;
	
	public Decedent() {
		super();
	}

	public CodeDt getBirthSex() {
		return birthSex;
	}

	public void setBirthSex(CodeDt birthSex) {
		this.birthSex = birthSex;
	}

	public Coding getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(Coding ethnicity) {
		this.ethnicity = ethnicity;
	}

	public Coding getRace() {
		return race;
	}

	public void setRace(Coding race) {
		this.race = race;
	}

	public UnsignedIntDt getAgeExtension() {
		return ageExtension;
	}

	public void setAgeExtension(UnsignedIntDt ageExtension) {
		this.ageExtension = ageExtension;
	}

	public PostalAddress getBirthplaceExtension() {
		return birthplaceExtension;
	}

	public void setBirthplaceExtension(PostalAddress birthplaceExtension) {
		this.birthplaceExtension = birthplaceExtension;
	}

	public BooleanType getServedInArmedForcesExtension() {
		return servedInArmedForcesExtension;
	}

	public void setServedInArmedForcesExtension(BooleanType servedInArmedForcesExtension) {
		this.servedInArmedForcesExtension = servedInArmedForcesExtension;
	}

	public CodeableConcept getMaritalStatusAtDeathExtension() {
		return maritalStatusAtDeathExtension;
	}

	public void setMaritalStatusAtDeathExtension(CodeableConcept maritalStatusAtDeathExtension) {
		this.maritalStatusAtDeathExtension = maritalStatusAtDeathExtension;
	}

	public PlaceOfDeath getPlaceOfDeathExtension() {
		return placeOfDeathExtension;
	}

	public void setPlaceOfDeathExtension(PlaceOfDeath placeOfDeathExtension) {
		this.placeOfDeathExtension = placeOfDeathExtension;
	}

	public Disposition getDispositionExtension() {
		return dispositionExtension;
	}

	public void setDispositionExtension(Disposition dispositionExtension) {
		this.dispositionExtension = dispositionExtension;
	}

	public CodeableConcept getEducationExtension() {
		return educationExtension;
	}

	public void setEducationExtension(CodeableConcept educationExtension) {
		this.educationExtension = educationExtension;
	}
}