
package gatech.edu.FHIRController.Controller;

import java.math.BigDecimal;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DosageInstruction;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.RelatedPerson;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import gatech.edu.FHIRController.PHCRClient.PHCRClientService;
import gatech.edu.STIECR.JSON.CodeableConcept;
import gatech.edu.STIECR.JSON.Diagnosis;
import gatech.edu.STIECR.JSON.Dosage;
import gatech.edu.STIECR.JSON.ECR;
import gatech.edu.STIECR.JSON.Facility;
import gatech.edu.STIECR.JSON.ImmunizationHistory;
import gatech.edu.STIECR.JSON.Name;
import gatech.edu.STIECR.JSON.ParentGuardian;
import gatech.edu.STIECR.JSON.Patient;
import gatech.edu.STIECR.JSON.Provider;
import gatech.edu.STIECR.JSON.utils.DateUtil;
import gatech.edu.STIECR.controller.ControllerUtils;
import gatech.edu.common.FHIR.client.ClientService;

@RestController
public class FHIRController{

	private static final Logger log = LoggerFactory.getLogger(FHIRController.class);
	
	ClientService FHIRClient;
	PHCRClientService PHCRClient;
	@Autowired
	public FHIRController(ClientService FHIRClient,PHCRClientService PHCRClient) {
		this.PHCRClient = PHCRClient;
		this.FHIRClient = FHIRClient;
		this.FHIRClient.initializeClient();
	}

	@RequestMapping(value = "/FHIRGET", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ECR> FHIRGET(@RequestParam(value="id") int id) {
		log.info("Getting ECR with id="+id);
		ECR ecr = PHCRClient.requestECRById(id);
		Integer patientId = Integer.parseInt(ecr.getPatient().getid());
		IdDt patientIdDt = FHIRClient.transfrom2Id(patientId);
		try {
			FHIRClient.getPatient(patientIdDt);
		}
		catch(FhirClientConnectionException e){
			ecr.getNotes().add(e.toString());
			return new ResponseEntity<ECR>(ecr,HttpStatus.FAILED_DEPENDENCY);
		}
		getFHIRRecords(ecr,patientIdDt);
		log.info("PUTTING THIS ECR RECORD:" + ecr.toString());
		HttpStatus returnStatus = HttpStatus.OK;
		try {
			PHCRClient.putECR(ecr);
		}
		catch(HTTPException e) {
			ecr.getNotes().add(e.getMessage());
			returnStatus = HttpStatus.NO_CONTENT;
			return new ResponseEntity<ECR>(ecr,returnStatus);
		}
		return new ResponseEntity<ECR>(ecr,returnStatus);
	}
	
	private void getFHIRRecords(ECR ecr, IdDt patientIdDt) {
		Patient ecrPatient = ecr.getPatient();
		log.info("Getting patient with id="+patientIdDt.getIdPart());
		ca.uhn.fhir.model.dstu2.resource.Patient patient = FHIRClient.getPatient(patientIdDt);
		
		handlePatient(ecr,patient);
		//handleRelatedPersons(ecr,patientIdDt);
		for(ResourceReferenceDt practitionerRef: patient.getCareProvider()) {
			handlePractitioner(ecr,practitionerRef);
		}
		handleConditions(ecr,patientIdDt);
		handleEncounters(ecr,patientIdDt);
		handleMedications(ecr,patientIdDt);
		//handleImmunizations(ecr,patientIdDt);
		//TODO: Handle ingressing visits correctly
		//TODO: Handle All Observations correctly
	}
	
	private void handlePatient(ECR ecr, ca.uhn.fhir.model.dstu2.resource.Patient patient) {
		ecr.getPatient().setbirthDate(patient.getBirthDate().toString());
		IDatatype deceasedValue = patient.getDeceased();
		if(deceasedValue instanceof DateDt) {
			ecr.getPatient().setdeathDate(DateUtil.dateToStdString(((DateDt) deceasedValue).getValue()));
		}
		ecr.getPatient().setsex(patient.getGender());
	}
	
	private void handleRelatedPersons(ECR ecr, IdDt IdDt) {
		Bundle relatedPersons = FHIRClient.getRelatedPersons(IdDt);
		for(Entry entry : relatedPersons.getEntry()) {
			RelatedPerson relatedPerson = (RelatedPerson)entry.getResource();
			Name nameToSearch = new Name(relatedPerson.getName().getFamily().get(0).getValue(),
					relatedPerson.getName().getGiven().get(0).getValue());
			ParentGuardian ecrParentGuardian = ecr.findParentGuardianWithName(nameToSearch);
			if(ecrParentGuardian == null) {
				ecrParentGuardian = new ParentGuardian();
				ecrParentGuardian.setname(nameToSearch);
				updateParentGuardian(ecrParentGuardian,relatedPerson);
				ecr.getPatient().getparentsGuardians().add(ecrParentGuardian);
			}
			else {
				updateParentGuardian(ecrParentGuardian,relatedPerson);
			}
		}
	}
	
	private void handlePractitioner(ECR ecr, ResourceReferenceDt refDt) {
		Practitioner provider = FHIRClient.getPractictioner(refDt.getId());
		Provider ecrProvider = ecr.getProvider();
		ecrProvider.setaddress(provider.getAddress().get(0).getText());
		ecrProvider.setcountry(provider.getAddress().get(0).getCountry());
		for(ContactPointDt contact: provider.getTelecom()) {
			if(contact.getSystem().equals("Phone") && ecrProvider.getphone().isEmpty()) {
				ecrProvider.setphone(contact.getValue());
			}
			else if(contact.getSystem().equals("Email") && ecrProvider.getemail().isEmpty()) {
				ecrProvider.setemail(contact.getValue());
			}
		}
	}
	
	private void handleMedications(ECR ecr, IdDt IdDt) {
		Bundle medications = FHIRClient.getMedicationOrders(IdDt);
		for(Entry entry : medications.getEntry()) {
			CodeableConcept ecrCode = new CodeableConcept();
			MedicationOrder medicationOrder = (MedicationOrder)entry.getResource();
			gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
			log.info("MEDICATION --- Trying medicationOrder: " + medicationOrder.getId());
			IDatatype medicationCodeUntyped = medicationOrder.getMedication();
			log.info("MEDICATION --- medication code element class: " + medicationCodeUntyped.getClass());
			if(medicationCodeUntyped instanceof CodeableConceptDt) {
				CodeableConceptDt code = (CodeableConceptDt)medicationCodeUntyped;
				log.info("MEDICATION --- Trying code with this many codings: " + code.getCoding().size());
				for(CodingDt coding : code.getCoding()) {
					log.info("MEDICATION --- Trying coding: " + coding.getDisplay());
					CodeableConcept concept = FHIRCoding2ECRConcept(coding);
					log.info("MEDICATION --- Translated to ECRconcept:" + concept.toString());
					ecrMedication.setCode(concept.getcode());
					ecrMedication.setSystem(concept.getsystem());
					ecrMedication.setDisplay(concept.getdisplay());
					ecrCode.setcode(concept.getcode());
					ecrCode.setsystem(concept.getsystem());
					ecrCode.setdisplay(concept.getdisplay());
				}
			}
			for(DosageInstruction dosageInstruction : medicationOrder.getDosageInstruction()) {
				Dosage ecrDosage = new Dosage();
				IDatatype doseUntyped = dosageInstruction.getDose();
				log.info("MEDICATION --- Found Dosage: " + doseUntyped.toString());
				if(doseUntyped instanceof SimpleQuantityDt) {
					SimpleQuantityDt doseTyped = (SimpleQuantityDt)doseUntyped;
					log.info("MEDICATION --- Dosage is of SimpleQuentityDt Type");
					ecrDosage.setValue(doseTyped.getValue().toString());
					ecrDosage.setUnit(doseTyped.getUnit());
					ecrMedication.setDosage(ecrDosage);
				}
				String periodUnit = dosageInstruction.getTiming().getRepeat().getPeriodUnits();
				BigDecimal period = dosageInstruction.getTiming().getRepeat().getPeriod();
				Integer frequency = dosageInstruction.getTiming().getRepeat().getFrequency();
				String commonFrequency= "" + frequency + " times per " + period + " " + periodUnit;
				log.info("MEDICATION --- Found Frequency: " + commonFrequency);
				ecrMedication.setFrequency(commonFrequency);
			}
			PeriodDt period = medicationOrder.getDispenseRequest().getValidityPeriod();
			log.info("MEDICATION --- Found Validity Period: " + period);
			ecrMedication.setDate(period.getStart().toString());
			log.info("MEDICATION --- ECRCode: " + ecrCode);
			if(ControllerUtils.isSTIMed(ecrCode) && !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
				log.info("MEDICATION --- Found New Entry: " + ecrCode);
				ecr.getPatient().getMedicationProvided().add(ecrMedication);
			}
			else {
				log.info("MEDICATION --- Didn't Match! " + ecrCode);
			}
		}
	}
	
	private void handleImmunizations(ECR ecr, IdDt IdDt) {
		Bundle immunizations = FHIRClient.getImmunizations(IdDt);
		for(Entry entry : immunizations.getEntry()) {
			Immunization immunization = (Immunization)entry.getResource();
			ImmunizationHistory ecrImmunization = new ImmunizationHistory();
			ecrImmunization.setCode(immunization.getVaccineCode().getCoding().get(0).getCode());
			ecrImmunization.setSystem(immunization.getVaccineCode().getCoding().get(0).getSystem());
			ecrImmunization.setDate(DateUtil.dateToStdString(immunization.getDate()));
			if(!ecr.getPatient().getimmunizationHistory().contains(ecrImmunization)) {
				ecr.getPatient().getimmunizationHistory().add(ecrImmunization);
			}
		}
	}
	
	private void handleConditions(ECR ecr, IdDt IdDt) {
		Bundle conditions = FHIRClient.getConditions(IdDt);
		for(Entry entry : conditions.getEntry()) {
			Condition condition = (Condition)entry.getResource();
			log.info("CONDITION --- Trying condition: " + condition.getId());
			CodeableConceptDt code = condition.getCode();
			log.info("CONDITION --- Trying code with this many codings: " + code.getCoding().size());
			for(CodingDt coding : code.getCoding()) {
				log.info("CONDITION --- Trying coding: " + coding.getDisplay());
				CodeableConcept concept = FHIRCoding2ECRConcept(coding);
				log.info("CONDITION --- Translated to ECRconcept:" + concept.toString());
				if(ControllerUtils.isSTICode(concept) && !ecr.getPatient().getsymptoms().contains(concept)) {
					log.info("CONDITION --- MATCH!" + concept.toString());
					ecr.getPatient().getsymptoms().add(concept);
				}
				//TODO: Figure out the right strategy for mapping an Onset
				//TODO: distinguish between symptom list and diagnosis list here
				//TODO: Map Pregnant from encounters
			}
		}
	}
	
	private void handleEncounters(ECR ecr, IdDt IdDt) {
		Bundle encounters = FHIRClient.getEncounters(IdDt);
		for(Entry entry : encounters.getEntry()) {
			Encounter encounter = (Encounter)entry.getResource();
			for(CodeableConceptDt reason : encounter.getReason()) {
				for(CodingDt coding : reason.getCoding()) {
					CodeableConcept concept = FHIRCoding2ECRConcept(coding);
					if(concept.getsystem().equals("SNOMED CT") && ControllerUtils.isSTICode(concept) && !ecr.getPatient().getsymptoms().contains(concept)) {
						ecr.getPatient().setvisitDateTime(DateUtil.DateTimeToStdString(encounter.getPeriod().getStart()));
					}
					//TODO: Figure out the right strategy for mapping an Onset
					//TODO: distinguish between symptom list and diagnosis list here
					//TODO: Map Pregnant from encounters
				}
			}
			ImmunizationHistory ecrImmunization = new ImmunizationHistory();
		}
	}
	
	private void handleObservation(ECR ecr, IdDt IdDt) {
		
	}
	
	private void updateParentGuardian(ParentGuardian pg, RelatedPerson rp) {
		for(ContactPointDt contact: rp.getTelecom()) {
			if(contact.getSystem().equals("Phone") && pg.getphone().isEmpty()) {
				pg.setphone(contact.getValue());
			}
			else if(contact.getSystem().equals("Email") && pg.getemail().isEmpty()) {
				pg.setemail(contact.getValue());
			}
		}
	}
	
	private static ECR shallowInitECR(ECR ecr) {
		if(ecr.getPatient() == null)
			ecr.setPatient(new Patient());
		if(ecr.getProvider() == null)
			ecr.setProvider(new Provider());
		if(ecr.getFacility() == null)
			ecr.setFacility(new Facility());
		return ecr;
	}
	
	public static CodeableConcept FHIRCoding2ECRConcept(CodingDt fhirCoding) {
		CodeableConcept ecrConcept = new CodeableConcept();
		ecrConcept.setcode(fhirCoding.getCode());
		if(fhirCoding.getSystem().equals("http://snomed.info/sct")) {
			ecrConcept.setsystem("SNOMED CT");
		}
		if(fhirCoding.getSystem().equals("http://www.nlm.nih.gov/research/umls/rxnorm"))
			ecrConcept.setsystem("RxNorm");
		if(fhirCoding.getSystem().equals("http://snomed.info/sct"))
			ecrConcept.setsystem("SNOMED CT");
		ecrConcept.setdisplay(fhirCoding.getDisplay());
		return ecrConcept;
	}
}