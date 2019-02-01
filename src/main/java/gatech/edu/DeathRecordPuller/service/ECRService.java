

package gatech.edu.DeathRecordPuller.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.model.api.IResource;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Range;
import org.hl7.fhir.dstu3.model.Ratio;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SampledData;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationAdministration;
import org.hl7.fhir.dstu3.model.MedicationDispense;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.RelatedPerson;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;

import gatech.edu.DeathRecordPuller.util.HAPIFHIRUtil;
import gatech.edu.STIECR.JSON.Diagnosis;
import gatech.edu.STIECR.JSON.ECR;
import gatech.edu.STIECR.JSON.ImmunizationHistory;
import gatech.edu.STIECR.JSON.LabOrderCode;
import gatech.edu.STIECR.JSON.LabResult;
import gatech.edu.STIECR.JSON.Name;
import gatech.edu.STIECR.JSON.ParentGuardian;
import gatech.edu.STIECR.JSON.Provider;
import gatech.edu.STIECR.JSON.TypeableID;
import gatech.edu.STIECR.JSON.utils.DateUtil;
import gatech.edu.STIECR.JSON.utils.ECRJsonConverter;
import gatech.edu.STIECR.controller.ControllerUtils;

@RestController
public class ECRService {

	private static final Logger log = LoggerFactory.getLogger(ECRService.class);

	ECRJsonConverter ecrConverter;

	@Autowired
	public ECRService() {
		this.ecrConverter = new ECRJsonConverter();
	}

	
	public ECR ecrFromFHIRRecords(Bundle fhirRecords) {
		ECR output = new ECR();
		
		for(BundleEntryComponent entry : fhirRecords.getEntry()) {
			Resource resource = entry.getResource();
			switch(resource.getResourceType()) {
				case Claim:
					handleSingularClaim(output,(Claim)resource);
				case Condition:
					handleSingularCondition(output,(Condition)resource);
					break;
				case Encounter:
					handleSingularEncounter(output, (Encounter)resource);
					break;
				case Immunization:
					handleSingularImmunization(output, (Immunization) resource);
					break;
				case MedicationAdministration:
					handleSingularMedicationAdministration(output, (MedicationAdministration) resource);
					break;
				case MedicationDispense:
					handleSingularMedicationDispense(output, (MedicationDispense) resource);
					break;
				case MedicationRequest:
					handleSingularMedicationRequest(output, (MedicationRequest) resource);
					break;
				case MedicationStatement:
					handleSingularMedicationStatement(output, (MedicationStatement) resource);
					break;
				case Observation:
					handleSingularObservation(output, (Observation) resource);
					break;
				case Patient:
					handlePatient(output, (org.hl7.fhir.dstu3.model.Patient) resource);
					break;
				case Practitioner:
					handleSingularPractitioner(output, (Practitioner) resource);
					break;
				case Procedure:
					handleSingularProcedure(output, (Procedure) resource);
					break;
				case RelatedPerson:
					handleSingularRelatedPerson(output, (RelatedPerson) resource);
					break;
			}
		}
		updateDateOfOnset(output);
		return output;
	}

	protected void updateDateOfOnset(ECR ecr) {
		if (StringUtils.isBlank(ecr.getPatient().getdateOfOnset())) {
			for (LabOrderCode labcode : ecr.getPatient().getlabOrderCode()) {
				for (LabResult labresult : labcode.getLaboratory_Results()) {
					if (labresult.getValue().toLowerCase().contains("positive")) {
						if (!StringUtils.isBlank(labresult.getDate())) {
							log.info("LabResult --- Found onset date of: " + labresult.getDate());
							ecr.getPatient().setdateOfOnset(labresult.getDate());
							break;
						}
					}
				}
			}
		}
	}

	void handlePatient(ECR ecr, org.hl7.fhir.dstu3.model.Patient patient) {
		ecr.getPatient().setbirthDate(patient.getBirthDate().toString());
		TypeableID id = new TypeableID();
		id.settype(patient.getIdentifierFirstRep().getSystem());
		id.setvalue(patient.getIdentifierFirstRep().getValue());
		ecr.getPatient().getid().add(id);
		Name name = new Name();
		name.setfamily(patient.getNameFirstRep().getFamily());
		name.setgiven(patient.getNameFirstRep().getGiven().get(0).toString());
		ecr.getPatient().setname(name);
		ecr.getPatient().setstreetAddress(patient.getAddressFirstRep().getText());
		Type deceasedValue = patient.getDeceased();
		try {
			if (patient.getDeceased() != null && patient.getDeceasedDateTimeType() != null) {
				ecr.getPatient().setdeathDate(HAPIFHIRUtil.getDate(patient.getDeceasedDateTimeType()).toString());
			}
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ecr.getPatient().setsex(patient.getGender().getDisplay());
	}
	
	void handleSingularRelatedPerson(ECR ecr, RelatedPerson relatedPerson) {
		Name nameToSearch = new Name(relatedPerson.getName().get(0).getFamily(),
				relatedPerson.getName().get(0).getGiven().get(0).getValue());
		ParentGuardian ecrParentGuardian = ecr.findParentGuardianWithName(nameToSearch);
		if (ecrParentGuardian == null) {
			ecrParentGuardian = new ParentGuardian();
			ecrParentGuardian.setname(nameToSearch);
			updateParentGuardian(ecrParentGuardian, relatedPerson);
			ecr.getPatient().getparentsGuardians().add(ecrParentGuardian);
		} else {
			updateParentGuardian(ecrParentGuardian, relatedPerson);
		}
	}

	void handleSingularPractitioner(ECR ecr, Practitioner provider) {
		Provider ecrProvider = new Provider();
		ecrProvider.setaddress(provider.getAddress().get(0).getText());
		ecrProvider.setcountry(provider.getAddress().get(0).getCountry());
		for (ContactPoint contact : provider.getTelecom()) {
			if (contact.getSystem().equals("Phone") && ecrProvider.getphone().isEmpty()) {
				ecrProvider.setphone(contact.getValue());
			} else if (contact.getSystem().equals("Email") && ecrProvider.getemail().isEmpty()) {
				ecrProvider.setemail(contact.getValue());
			}
		}
		// Update or add to the current provider list
		if (ecr.getProvider().contains(ecrProvider)) {
			for (Provider originalProvider : ecr.getProvider()) {
				if (originalProvider.equals(ecrProvider))
					originalProvider.update(ecrProvider);
			}
		} else {
			ecr.getProvider().add(ecrProvider);
		}
	}
	
	void handleSingularMedicationAdministration(ECR ecr,MedicationAdministration medicationAdministration) {
		gatech.edu.STIECR.JSON.CodeableConcept ecrCode = new gatech.edu.STIECR.JSON.CodeableConcept();
		gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
		log.info("MEDICATIONADMINISTRATION --- Trying medicationAdministration: "
				+ medicationAdministration.getId());
		Type medicationCodeUntyped = medicationAdministration.getMedication();
		log.info("MEDICATIONADMINISTRATION --- medication code element class: "
				+ medicationCodeUntyped.getClass());
		if (medicationCodeUntyped instanceof CodeableConcept) {
			CodeableConcept code = (CodeableConcept) medicationCodeUntyped;
			log.info("MEDICATIONADMINISTRATION --- Trying code with this many codings: "
					+ code.getCoding().size());
			for (Coding coding : code.getCoding()) {
				log.info("MEDICATIONADMINISTRATION --- Trying coding: " + coding.getDisplay());
				gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
				log.info("MEDICATIONADMINISTRATION --- Translated to ECRconcept:" + concept.toString());
				ecrMedication.setCode(coding.getCode());
				ecrMedication.setSystem(coding.getSystem());
				ecrMedication.setDisplay(coding.getDisplay());
				ecrCode.setcode(coding.getCode());
				ecrCode.setsystem(coding.getSystem());
				ecrCode.setdisplay(coding.getDisplay());
			}
		}
		if (!medicationAdministration.getDosage().isEmpty()) {
			gatech.edu.STIECR.JSON.Dosage ecrDosage = new gatech.edu.STIECR.JSON.Dosage();
			ecrDosage.setValue(medicationAdministration.getDosage().getDose().getValue().toString());
			ecrDosage.setUnit(medicationAdministration.getDosage().getDose().getUnit());
			ecrMedication.setDosage(ecrDosage);
		}
		try {
			ecrMedication.setDate(HAPIFHIRUtil.getDate(medicationAdministration.getEffectiveDateTimeType()).toString());
		} catch (FHIRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("MEDICATIONADMINISTRATION --- ECRCode: " + ecrCode);
		if (!ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
			log.info("MEDICATIONADMINISTRATION --- Found New Entry: " + ecrCode);
			ecr.getPatient().getMedicationProvided().add(ecrMedication);
		} else {
			log.info("MEDICATIONADMINISTRATION --- Didn't Match or found duplicate! " + ecrCode);
		}
	}
	
	void handleSingularMedicationDispense(ECR ecr, MedicationDispense medicationDispense) {
		gatech.edu.STIECR.JSON.CodeableConcept ecrCode = new gatech.edu.STIECR.JSON.CodeableConcept();
		gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
		log.info("MEDICATIONDISPENSE --- Trying medicationDispense: " + medicationDispense.getId());

		Type medicationCodeUntyped = medicationDispense.getMedication();
		if (medicationCodeUntyped == null && medicationDispense.getAuthorizingPrescription() != null) {
			medicationCodeUntyped = ((MedicationRequest) medicationDispense.getAuthorizingPrescription().get(0)
					.getResource()).getMedication();
		}
		if (medicationCodeUntyped == null) {
			log.info("MEDICATIONDISPENSE --- FAILED TO FIND MEDICATION - SKIPPING!!");
			return;
		}
		log.info("MEDICATIONDISPENSE --- medication code element class: " + medicationCodeUntyped.getClass());

		CodeableConcept code = null;

		if (medicationCodeUntyped instanceof CodeableConcept) {
			code = (CodeableConcept) medicationCodeUntyped;
		}
		if (code != null) {
			log.info("MEDICATIONDISPENSE --- Trying code with this many codings: " + code.getCoding().size());
			for (Coding coding : code.getCoding()) {
				log.info("MEDICATIONDISPENSE --- Trying coding: " + coding.getDisplay());
				gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);

				log.info("\n----------> MEDICATIONDISPENSE --- Translated to ECRconcept:" + concept.toString());
				ecrMedication.setCode(coding.getCode());
				ecrMedication.setSystem(coding.getSystem());
				ecrMedication.setDisplay(coding.getDisplay());
				ecrCode.setcode(coding.getCode());
				ecrCode.setsystem(coding.getSystem());
				ecrCode.setdisplay(coding.getDisplay());

			}
			if (ControllerUtils.isSTIMed(ecrCode)) {
				for (Dosage dosageInstruction : medicationDispense.getDosageInstruction()) {
					gatech.edu.STIECR.JSON.Dosage ecrDosage = new gatech.edu.STIECR.JSON.Dosage();
					Type doseUntyped = dosageInstruction.getDose();
					if (doseUntyped != null) {
						log.info("MEDICATIONDISPENSE --- Found Dosage: " + doseUntyped.toString());
						if (doseUntyped instanceof SimpleQuantity) {
							SimpleQuantity doseTyped = (SimpleQuantity) doseUntyped;
							log.info("MEDICATIONDISPENSE --- Dosage is of SimpleQuentity Type");
							ecrDosage.setValue(doseTyped.getValue().toString());
							ecrDosage.setUnit(doseTyped.getUnit());
							ecrMedication.setDosage(ecrDosage);
						}
						String periodUnit = dosageInstruction.getTiming().getRepeat().getDurationUnit().getDisplay();
						BigDecimal period = dosageInstruction.getTiming().getRepeat().getPeriod();
						Integer frequency = dosageInstruction.getTiming().getRepeat().getFrequency();
						String commonFrequency = "" + frequency + " times per " + period + " " + periodUnit;
						log.info("MEDICATIONDISPENSE --- Found Frequency: " + commonFrequency);
						ecrMedication.setFrequency(commonFrequency);
					} else {
						log.info("MEDICATIONDISPENSE --- Not Found");
					}
				}
				Date timeDispensed = medicationDispense.getWhenHandedOver();
				log.info("MEDICATIONDISPENSE --- Found Handed Over Date: " + timeDispensed);
				if (timeDispensed != null) {
					ecrMedication.setDate(DateUtil.dateTimeToStdString(timeDispensed));
				}
				log.info("MEDICATIONDISPENSE --- ECRCode: " + ecrCode);
				if (!ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
					log.info("=======>MEDICATIONDISPENSE --- Found New Entry and added to ECR: " + ecrCode);
					ecr.getPatient().getMedicationProvided().add(ecrMedication);
				} else {
					log.info("MEDICATIONDISPENSE --- Didn't Match or found duplicate! " + ecrCode);
				}
			} else {
				log.info("MEDICATIONDISPENSE --- Didn't Match or found duplicate! " + ecrCode);
			}
		} else {
			log.info("FAILED TO FIND MEDICATION CODE.");
		}
	}
	
	void handleSingularMedicationRequest(ECR ecr, MedicationRequest MedicationRequest) {
		gatech.edu.STIECR.JSON.CodeableConcept ecrCode = new gatech.edu.STIECR.JSON.CodeableConcept();
		gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
		log.info("MedicationRequest --- Trying MedicationRequest: " + MedicationRequest.getId());
		Type medicationCodeUntyped = MedicationRequest.getMedication();
		log.info("MedicationRequest --- medication code element class: " + medicationCodeUntyped.getClass());

		CodeableConcept code = null;

		if (medicationCodeUntyped instanceof CodeableConcept) {
			code = (CodeableConcept) medicationCodeUntyped;
		}
		if (code != null) {
			log.info("MedicationRequest --- Trying code with this many codings: " + code.getCoding().size());
			for (Coding coding : code.getCoding()) {
				log.info("MedicationRequest --- Trying coding: " + coding.getDisplay());
				gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
				log.info("MedicationRequest --- Translated to ECRconcept:" + concept.toString());
				ecrMedication.setCode(coding.getCode());
				ecrMedication.setSystem(coding.getSystem());
				ecrMedication.setDisplay(coding.getDisplay());
				ecrCode.setcode(coding.getCode());
				ecrCode.setsystem(coding.getSystem());
				ecrCode.setdisplay(coding.getDisplay());
				if (ControllerUtils.isSTIMed(ecrCode)) {
					break; // Found a code already so stop here.
				}
			}
		}
		if (ControllerUtils.isSTIMed(ecrCode)) {
			for (Dosage dosageInstruction : MedicationRequest.getDosageInstruction()) {
				gatech.edu.STIECR.JSON.Dosage ecrDosage = new gatech.edu.STIECR.JSON.Dosage();
				Type doseUntyped = dosageInstruction.getDose();
				if (doseUntyped != null) {
					log.info("MedicationRequest --- Found Dosage: " + doseUntyped.toString());
					if (doseUntyped instanceof SimpleQuantity) {
						SimpleQuantity doseTyped = (SimpleQuantity) doseUntyped;
						log.info("MedicationRequest --- Dosage is of SimpleQuentity Type");
						ecrDosage.setValue(doseTyped.getValue().toString());
						ecrDosage.setUnit(doseTyped.getUnit());
						ecrMedication.setDosage(ecrDosage);
					}
					String periodUnit = dosageInstruction.getTiming().getRepeat().getDurationUnit().getDisplay();
					BigDecimal period = dosageInstruction.getTiming().getRepeat().getPeriod();
					Integer frequency = dosageInstruction.getTiming().getRepeat().getFrequency();
					// String commonFrequency= "" + frequency + " times per " + period + " " +
					// periodUnit;
					// log.info("MedicationRequest --- Found Frequency: " + commonFrequency);
					// ecrMedication.setFrequency(commonFrequency);
				} else {
					log.info("MedicationRequest --- DOSE NOT FOUND.");
				}
			}

			log.info("MedicationRequest --- ECRCode: " + ecrCode);
			if (ControllerUtils.isSTIMed(ecrCode)
					&& !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
				log.info("MedicationRequest --- Found New Entry: " + ecrCode);
				ecr.getPatient().getMedicationProvided().add(ecrMedication);
			} else {
				log.info("MedicationRequest --- Didn't Match or found duplicate! " + ecrCode);
			}
			// String periodUnit =
			// dosageInstruction.getTiming().getRepeat().getDurationUnit().getDisplay();
			// BigDecimal period = dosageInstruction.getTiming().getRepeat().getPeriod();
			// Integer frequency = dosageInstruction.getTiming().getRepeat().getFrequency();
			// /*String commonFrequency= "" + frequency + " times per " + period + " " +
			// periodUnit;
			// log.info("MedicationRequest --- Found Frequency: " + commonFrequency);
			// ecrMedication.setFrequency(commonFrequency); */
		}

		Period period = MedicationRequest.getDispenseRequest().getValidityPeriod();
		if ( period != null && period.getStart() != null ) {
			log.info("MedicationRequest --- Found Validity Period: " + period.getStart().toLocaleString());
			ecrMedication.setDate(period.getStart().toString());
		}
		log.info("MedicationRequest --- ECRCode: " + ecrCode);
		if (ControllerUtils.isSTIMed(ecrCode)
				&& !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
			log.info("MedicationRequest --- Found New Entry: " + ecrCode);
			ecr.getPatient().getMedicationProvided().add(ecrMedication);
		} else {
			log.info("MedicationRequest --- Didn't Match or found duplicate! " + ecrCode);
		}
		if (MedicationRequest.getReasonCode() != null && !MedicationRequest.getReasonCode().isEmpty()) {
			if (MedicationRequest.getReasonCode() instanceof CodeableConcept) {
				handleSingularConditionConceptCode(ecr, (CodeableConcept) MedicationRequest.getReasonCode());
			}
			if (MedicationRequest.getReasonCode() != null && !MedicationRequest.getReasonCode().isEmpty()) {
				if (MedicationRequest.getReasonCode() instanceof CodeableConcept) {
					handleSingularConditionConceptCode(ecr, (CodeableConcept) MedicationRequest.getReasonCode());
				}
			}
		} else {
			log.info("MedicationRequest --- Didn't Match  " + ecrCode);
		}
	}
	
	void handleSingularMedicationStatement(ECR ecr, MedicationStatement medicationStatement) {
		gatech.edu.STIECR.JSON.CodeableConcept ecrCode = new gatech.edu.STIECR.JSON.CodeableConcept();
		gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
		log.info("MEDICATIONSTATEMENT  --- Trying MedicationStatement: " + medicationStatement.getId());
		Type medicationCodeUntyped = medicationStatement.getMedication();
		log.info("MEDICATIONSTATEMENT  --- medication code element class: " + medicationCodeUntyped.getClass());
		if (medicationCodeUntyped instanceof CodeableConcept) {
			CodeableConcept code = (CodeableConcept) medicationCodeUntyped;
			log.info("MEDICATIONSTATEMENT  --- Trying code with this many codings: " + code.getCoding().size());
			for (Coding coding : code.getCoding()) {
				log.info("MEDICATIONSTATEMENT  --- Trying coding: " + coding.getDisplay());
				gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
				log.info("MEDICATIONSTATEMENT  --- Translated to ECRconcept:" + concept.toString());
				ecrMedication.setCode(coding.getCode());
				ecrMedication.setSystem(coding.getSystem());
				ecrMedication.setDisplay(coding.getDisplay());
				ecrCode.setcode(coding.getCode());
				ecrCode.setsystem(coding.getSystem());
				ecrCode.setdisplay(coding.getDisplay());
			}
		}
		if (!medicationStatement.getDosage().isEmpty()) {
			gatech.edu.STIECR.JSON.Dosage ecrDosage = new gatech.edu.STIECR.JSON.Dosage();
			Type dosageQuantityUntyped = medicationStatement.getDosage().get(0).getDose();
			if (dosageQuantityUntyped instanceof SimpleQuantity) {
				SimpleQuantity dosageQuantity = (SimpleQuantity) dosageQuantityUntyped;
				ecrDosage.setValue(dosageQuantity.getValue().toString());
				ecrDosage.setUnit(dosageQuantity.getUnit().toString());
			} else
			if (dosageQuantityUntyped instanceof Range) {
				Range dosageRange = (Range) dosageQuantityUntyped;
				BigDecimal high = dosageRange.getHigh().getValue();
				BigDecimal low = dosageRange.getLow().getValue();
				BigDecimal mean = high.add(low);
				mean = mean.divide(new BigDecimal(2));
				ecrDosage.setValue(mean.toString());
				ecrDosage.setUnit(dosageRange.getHigh().getUnit());
				ecrMedication.setDosage(ecrDosage);
			}
		}
		if (!medicationStatement.getDateAssertedElement().isEmpty()) {
			if (medicationStatement.getDateAsserted() != null) {
				String dateTimeAsString = DateUtil.dateTimeToStdString(medicationStatement.getDateAsserted());
				log.info("MEDICATIONSTATEMENT  --- Found Medication Date: " + dateTimeAsString);
				ecrMedication.setDate(dateTimeAsString);
			}
			log.info("MEDICATIONSTATEMENT  --- ECRCode: " + ecrCode);
		}
		if (ControllerUtils.isSTIMed(ecrCode)
				&& !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
			log.info("MEDICATIONSTATEMENT  --- Found New Entry: " + ecrCode);
			ecr.getPatient().getMedicationProvided().add(ecrMedication);
		} else {
			log.info("MEDICATIONSTATEMENT  --- Didn't Match or found duplicate! " + ecrCode);
		}
	}

	void handleSingularImmunization(ECR ecr,Immunization immunization) {
		ImmunizationHistory ecrImmunization = new ImmunizationHistory();
		if (immunization != null && immunization.getVaccineCode().getCoding().size() > 0) {
			ecrImmunization.setCode(immunization.getVaccineCode().getCoding().get(0).getCode());
			ecrImmunization.setSystem(immunization.getVaccineCode().getCoding().get(0).getSystem());
		} else
		if (immunization != null && StringUtils.isNotBlank(immunization.getVaccineCode().getText())) {
			ecrImmunization.setCode(immunization.getVaccineCode().getText());
		} else
		if (immunization != null && StringUtils.isNotBlank(immunization.getText().getDivAsString())) {
			ecrImmunization.setCode(immunization.getText().getDivAsString());
		}
		ecrImmunization.setDate(DateUtil.dateToStdString(immunization.getDate()));
		if (!ecr.getPatient().getimmunizationHistory().contains(ecrImmunization)) {
			log.info("Adding Immunization For " + immunization.getId());
			ecr.getPatient().getimmunizationHistory().add(ecrImmunization);
		}
	}

	public void handleSingularConditionConceptCode(ECR ecr, CodeableConcept code) {
		log.info("CONDITION --- Trying code with this many codings: " + code.getCoding().size());
		for (Coding coding : code.getCoding()) {
			log.info("CONDITION --- Trying coding: " + coding.getDisplay());
			gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
			log.info("CONDITION --- Translated to ECRconcept:" + concept.toString());
			if (ControllerUtils.isSTICode(concept) && !ecr.getPatient().getsymptoms().contains(concept)) {
				log.info("CONDITION --- SYMPTOM MATCH!" + concept.toString());
				ecr.getPatient().getsymptoms().add(concept);
				break; // Stop once we get a Coding that matches our list of codes.
			}
		}
	}

	protected boolean conceptIsSTI(CodeableConcept code) {
		for (Coding coding : code.getCoding()) {
			gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
			if (ControllerUtils.isSTICode(concept)) {
				return true;
			}
		}
		return false;
	}

	public void handleSingularCondition(ECR ecr, Condition condition) {
		log.info("CONDITION --- Trying condition: " + condition.getId());
		try {
			if (condition.getAbatement() != null && condition.getAbatementDateTimeType() != null) {
				Date abatementDate = HAPIFHIRUtil.getDate(condition.getAbatementDateTimeType());
				if (abatementDate != null & abatementDate.compareTo(new Date()) <= 0) {
					log.info("CONDITION --- Found abatement date of: " + abatementDate);
					log.info("CONDITION --- Condition is not current, ignoring condition.");
					return;
				}
			}
		} catch (FHIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Date onsetDate = null;
		try {
			onsetDate = HAPIFHIRUtil.getDate(condition.getOnsetDateTimeType());
		} catch (FHIRException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (onsetDate == null) {
			onsetDate = HAPIFHIRUtil.getDate(condition.getAssertedDateElement());
		}
		Date ecrDate = null;
		try {
			String onsetDateStr = ecr.getPatient().getdateOfOnset();
			ecrDate = DateUtil.parse(onsetDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		CodeableConcept code = condition.getCode();
		log.info("CONDITION --- Trying code with this many codings: " + code.getCoding().size());
		for (Coding coding : code.getCoding()) {
			log.info("CONDITION --- Trying coding: " + coding.getDisplay());
			gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
			log.info("CONDITION --- Translated to ECRconcept:" + concept.toString());
			log.info("CONDITION ---DIAGNOSIS MATCH!" + concept.toString());
			Diagnosis updatedDiagnosis = new Diagnosis();
			updatedDiagnosis.setCode(coding.getCode());
			updatedDiagnosis.setDisplay(coding.getDisplay());
			updatedDiagnosis.setSystem(coding.getSystem());
			if ((ecrDate == null && onsetDate != null)
					|| (ecrDate != null && onsetDate != null && onsetDate.before(ecrDate))) {
				log.info("CONDITION --- Found onset date of: " + onsetDate);
				log.info("CONDITION --- Eariler date than previously found. Replacing patient onset date.");
				ecr.getPatient().setdateOfOnset(DateUtil.dateTimeToStdString(onsetDate));
				updatedDiagnosis.setDate(DateUtil.dateTimeToStdString(onsetDate));
			} else {
				updatedDiagnosis.setDate(ecr.getPatient().getdateOfOnset());
			}
			ecr.getPatient().getDiagnosis().add(updatedDiagnosis);
		}
		handleSingularConditionConceptCode(ecr, code);
		// TODO: distinguish between symptom list and diagnosis list here
		// TODO: Map Pregnant from encounters
	}

	
	void handleSingularClaim(ECR ecr, Claim claim) {
		//TODO: Find how to get coverage from a single claim
	}
	
	void handleSingularEncounter(ECR ecr,Encounter encounter) {
		/*
		for (CodeableConcept reason : encounter.getReasonCode()) {
			for (Coding coding : reason.getCoding()) {
				gatech.edu.STIECR.JSON.CodeableConcept concept = FHIRCoding2ECRConcept(coding);
				if (coding.getSystem().equals("SNOMED CT") && ControllerUtils.isSTICode(concept)
						&& !ecr.getPatient().getsymptoms().contains(concept)) {
					ecr.getPatient()
							.setvisitDateTime(DateUtil.dateTimeToStdString(encounter.getPeriod().getStart()));
				}
				// TODO: Figure out the right strategy for mapping an Onset
				// TODO: distinguish between symptom list and diagnosis list here
				// TODO: Map Pregnant from encounters
			}
		}*/
	}
	
	void handleSingularObservation(ECR ecr, Observation observation) {
		CodeableConcept code = observation.getCode();
		for (Coding coding : code.getCoding()) {
			LabOrderCode labOrder = new LabOrderCode();
			labOrder.setcode(coding.getCode());
			labOrder.setdisplay(coding.getDisplay());
			labOrder.setsystem(coding.getSystem());
			LabResult labResult = new LabResult();
			labResult.setsystem("n/a");
			Type untypedValue = observation.getValue();
			if (untypedValue instanceof Quantity) {
				Quantity quantity = (Quantity) untypedValue;
				labResult.setValue(quantity.getValue().toString());
				labResult.setUnit(new gatech.edu.STIECR.JSON.CodeableConcept(quantity.getSystem(),quantity.getUnit(),quantity.getCode()));
			} else
			if (untypedValue instanceof CodeableConcept) {
				Coding valueCoding = ((CodeableConcept) untypedValue).getCodingFirstRep();
				labResult.setValue(valueCoding.getDisplay());
				labResult.setcode(valueCoding.getCode());
				labResult.setdisplay(valueCoding.getDisplay());
			} else
			if (untypedValue instanceof StringType) {
				labResult.setValue(untypedValue.toString());
			} else
			if (untypedValue instanceof Range) {
				Range range = (Range) untypedValue;
				labResult.setValue("High:" + range.getHigh() + ";low:" + range.getLow());
			} else
			if (untypedValue instanceof Ratio) {
				Ratio ratio = (Ratio) untypedValue;
				labResult.setValue(
						ratio.getNumerator().toString() + "/" + ratio.getDenominator().toString());
			} else
			if (untypedValue instanceof SampledData) {
				labResult.setValue(((SampledData) untypedValue).getData());
			} else
			if (untypedValue instanceof DateTimeType) {
				labResult.setValue(HAPIFHIRUtil.getDate((DateTimeType) untypedValue).toString());
			} else
			if (untypedValue instanceof Period) {
				Period period = (Period) untypedValue;
				labResult.setValue(
						"Start:" + period.getStart().toString() + ";End" + period.getEnd().toString());
			}
			try {
				labResult.setDate(HAPIFHIRUtil.getDate(observation.getEffectiveDateTimeType()).toString());
			} catch (FHIRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			labResult.setcode(coding.getCode());
			labResult.setsystem(coding.getSystem());
			labOrder.getLaboratory_Results().add(labResult);
			ecr.getPatient().getlabOrderCode().add(labOrder);
		}
	}

	void handleSingularProcedure(ECR ecr,Procedure procedure) {
		if (procedure.getReasonCode() != null && !procedure.getReasonCode().isEmpty()) {
			if (procedure.getReasonCode() instanceof CodeableConcept) {
				handleSingularConditionConceptCode(ecr, (CodeableConcept) procedure.getReasonCode());
			}
		}
	}
	void updateParentGuardian(ParentGuardian pg, RelatedPerson rp) {
		for (ContactPoint contact : rp.getTelecom()) {
			if (contact.getSystem().equals("Phone") && pg.getphone().isEmpty()) {
				pg.setphone(contact.getValue());
			} else
			if (contact.getSystem().equals("Email") && pg.getemail().isEmpty()) {
				pg.setemail(contact.getValue());
			}
		}
	}

	public static gatech.edu.STIECR.JSON.CodeableConcept FHIRCoding2ECRConcept(Coding fhirCoding) {
		gatech.edu.STIECR.JSON.CodeableConcept ecrConcept = new gatech.edu.STIECR.JSON.CodeableConcept();
		ecrConcept.setcode(fhirCoding.getCode());
		ecrConcept.setsystem(fhirCoding.getSystem());
		if (fhirCoding.getSystem().equals("http://snomed.info/sct")) {
			ecrConcept.setsystem("SNOMED CT");
		} else
		if (fhirCoding.getSystem().equals("http://www.nlm.nih.gov/research/umls/rxnorm")) {
			ecrConcept.setsystem("RxNorm");
		}
		ecrConcept.setdisplay(fhirCoding.getDisplay());
		return ecrConcept;
	}
}
