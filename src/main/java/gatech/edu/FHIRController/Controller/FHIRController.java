package gatech.edu.FHIRController.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.RelatedPerson;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import gatech.edu.STIECR.JSON.CodeableConcept;
import gatech.edu.STIECR.JSON.Diagnosis;
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

	ClientService FHIRClient;
	@Autowired
	public FHIRController(ClientService FHIRClient) {
		this.FHIRClient = FHIRClient;
	}

	@RequestMapping(value = "/FHIRGET", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ECR> MapECR2StagingTables(@RequestParam("id") int id) {
		ECR ecr = new ECR();
		ecr = shallowInitECR(ecr);
		ecr.setId(id);
		try {
			FHIRClient.getPatient(ecr.getPatient().getid());
		}
		catch(FhirClientConnectionException e){
			ecr.getNotes().add(e.toString());
			return new ResponseEntity<ECR>(ecr,HttpStatus.FAILED_DEPENDENCY);
		}
		getFHIRRecords(ecr);
		return new ResponseEntity<ECR>(ecr,HttpStatus.OK);
	}
	
	private void getFHIRRecords(ECR ecr) {
		Patient ecrPatient = ecr.getPatient();
		Integer patientId = Integer.parseInt(ecrPatient.getid());
		IdDt patientIdDt = FHIRClient.transfrom2Id(patientId);
		ca.uhn.fhir.model.dstu2.resource.Patient patient = FHIRClient.getPatient(patientIdDt);
		
		handlePatient(ecr,patient);
		handleRelatedPersons(ecr,patientIdDt);
		for(ResourceReferenceDt practitionerRef: patient.getCareProvider()) {
			handlePractitioner(ecr,practitionerRef);
		}
		handleConditions(ecr,patientIdDt);
		handleEncounters(ecr,patientIdDt);
		handleImmunizations(ecr,patientIdDt);
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
			CodeableConceptDt code = condition.getCode();
			for(CodingDt coding : code.getCoding()) {
				CodeableConcept concept = FHIRCoding2ECRConcept(coding);
				if(concept.getsystem().equals("SNOMED CT") && ControllerUtils.isSTICode(concept) && !ecr.getPatient().getsymptoms().contains(concept)) {
					IDatatype onsetUntyped = condition.getOnset();
					String onsetDate = "";
					if(onsetUntyped instanceof DateDt) {
						onsetDate = DateUtil.dateToStdString(((DateDt)onsetUntyped).getValue());
					}
					else if(onsetUntyped instanceof PeriodDt) {
						onsetDate = DateUtil.dateToStdString(((PeriodDt)onsetUntyped).getStart());
					}
					else if(onsetUntyped instanceof StringDt) {
						onsetDate = ((StringDt)onsetUntyped).getValue();
					}
					
					ecr.getPatient().setDiagnosis(new Diagnosis(concept.getcode(),
																concept.getsystem(),
																concept.getdisplay(),
																onsetDate));
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
		ecrConcept.setsystem(fhirCoding.getSystem());
		ecrConcept.setdisplay(fhirCoding.getDisplay());
		return ecrConcept;
	}
}