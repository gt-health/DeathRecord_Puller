
package gatech.edu.FHIRController.Controller;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.xml.ws.http.HTTPException;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.joda.DateTimeParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.RatioDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.SampledDataDt;
import ca.uhn.fhir.model.dstu2.composite.SimpleQuantityDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.model.dstu2.resource.Claim;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResource;
import ca.uhn.fhir.model.dstu2.resource.Conformance.RestResourceInteraction;
import ca.uhn.fhir.model.dstu2.resource.Coverage;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Immunization;
import ca.uhn.fhir.model.dstu2.resource.Medication;
import ca.uhn.fhir.model.dstu2.resource.MedicationAdministration;
import ca.uhn.fhir.model.dstu2.resource.MedicationDispense;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DispenseRequest;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DosageInstruction;
import ca.uhn.fhir.model.dstu2.resource.MedicationStatement;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;
import ca.uhn.fhir.model.dstu2.resource.Procedure;
import ca.uhn.fhir.model.dstu2.resource.RelatedPerson;
import ca.uhn.fhir.model.dstu2.valueset.TypeRestfulInteractionEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.TimeDt;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;

import gatech.edu.FHIRController.DomainServices.DomainService;
import gatech.edu.FHIRController.PHCRClient.PHCRClientService;
import gatech.edu.FHIRController.Scheduler.SchedulerService;
import gatech.edu.FHIRController.util.HAPIFHIRUtil;
import gatech.edu.STIECR.DB.model.ECRJob;
import gatech.edu.STIECR.DB.repo.ECRJobRepository;
import gatech.edu.STIECR.JSON.CodeableConcept;
import gatech.edu.STIECR.JSON.Diagnosis;
import gatech.edu.STIECR.JSON.Dosage;
import gatech.edu.STIECR.JSON.ECR;
import gatech.edu.STIECR.JSON.Facility;
import gatech.edu.STIECR.JSON.ImmunizationHistory;
import gatech.edu.STIECR.JSON.LabOrderCode;
import gatech.edu.STIECR.JSON.LabResult;
import gatech.edu.STIECR.JSON.Name;
import gatech.edu.STIECR.JSON.ParentGuardian;
import gatech.edu.STIECR.JSON.Patient;
import gatech.edu.STIECR.JSON.Provider;
import gatech.edu.STIECR.JSON.TypeableID;
import gatech.edu.STIECR.JSON.utils.DateUtil;
import gatech.edu.STIECR.controller.ControllerUtils;
import gatech.edu.common.FHIR.client.ClientService;

@RestController
public class FHIRController{

	private static final Logger log = LoggerFactory.getLogger(FHIRController.class);
	
	ClientService FHIRClient;
	DomainService DomainService;
	PHCRClientService PHCRClient;
	ECRJobRepository ECRJobRepository;
	SchedulerService SchedulerService;
	@Autowired
	public FHIRController(ClientService FHIRClient,PHCRClientService PHCRClient,DomainService DomainService,ECRJobRepository ECRJobRepository,SchedulerService SchedulerService) {
		this.PHCRClient = PHCRClient;
		this.FHIRClient = FHIRClient;
		this.DomainService = DomainService;
		this.ECRJobRepository = ECRJobRepository;
		this.SchedulerService = SchedulerService;
		this.FHIRClient.initializeClient();
	}

	@RequestMapping(value = "/FHIRGET", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<ECR> FHIRGET(@RequestParam(value="id") int id, @RequestParam(name = "scheduled", required = false, defaultValue = "false")boolean scheduled) {
		HttpStatus returnStatus = HttpStatus.OK;
		log.info("Getting ECR with id="+id);
		//Get original ECR record
		ECR ecr = PHCRClient.requestECRById(id);
		//Find Domainservice endpoints given a provider's name
		Map<String,Collection<URL> > domainEndpoints = new HashMap<String, Collection<URL> >();
		try {
			for(Provider provider: ecr.getProvider()) {
				List<URL> urls;
				if(DomainService.getDefaultUsername() != null && !DomainService.getDefaultUsername().isEmpty()) {
					urls = DomainService.getURLs("", "public");
				}
				else {
					urls = DomainService.getURLs(provider.getname(), provider.getid().gettype());
				}
				domainEndpoints.put(provider.getid().getvalue(), urls);
			}
		} catch (RestClientException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//For each organization searched and added from domain services
		for(String organization: domainEndpoints.keySet()) {
			log.info("TRYING ORGANIZATION :" + organization);
			TypeableID patientId = ecr.getPatient().getIdMatchingType("MR"); //Get Patient ID that matches the organization
			if(patientId != null) {
				log.info("FOUND MATCHING PATIENTID MR:" + patientId);
				//For each url pulled by that organization key
				for(URL endpoint: domainEndpoints.get(organization)) {
					log.info("Trying endpoint:" + endpoint.toString());
					FHIRClient.setServerBaseUrl(endpoint.toString());
					FHIRClient.initializeClient(); //This is an expensive operation
					String identifier = patientId.getvalue();
					Bundle fhirPatientBundle = null;
					//Test the connection
					try {
						if(organization.equalsIgnoreCase("Regenstrief"))
							fhirPatientBundle = FHIRClient.getPatientUsingIdentifierAndOrganization(identifier, organization);
						else
							fhirPatientBundle = FHIRClient.getPatient(ecr.getPatient().getname());
					}
					catch(FhirClientConnectionException e){
						ecr.getNotes().add(e.toString());
						return new ResponseEntity<ECR>(ecr,HttpStatus.FAILED_DEPENDENCY);
					}
					getFHIRRecords(ecr,fhirPatientBundle);
				}
			}
		}
		//Replace the old record in the PHCR
		log.info("PUTTING THIS ECR RECORD:" + ecr.toString());
		try {
			PHCRClient.putECR(ecr);
		}
		catch(HTTPException e) {
			ecr.getNotes().add(e.getMessage());
			returnStatus = HttpStatus.NO_CONTENT;
			return new ResponseEntity<ECR>(ecr,returnStatus);
		}
		if(scheduled) {
			updateJobEntry(ecr);
		}
		return new ResponseEntity<ECR>(ecr,returnStatus);
	}
	
	@RequestMapping(value = "/FHIRGET", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<ECRJob> schedule(@RequestParam(name="id") int id, @RequestParam(name="cron",required=false,defaultValue="0 0 * * *") String cron){
		ECR ecr = PHCRClient.requestECRById(id);
		ECRJob job = new ECRJob(ecr);
		try {
			SchedulerService.addScheduleService(cron, job);
		} catch (Exception e) {
			return new ResponseEntity<ECRJob>(job,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		ECRJobRepository.save(job);
		return new ResponseEntity<ECRJob>(job,HttpStatus.OK);
	}
	
	private void getFHIRRecords(ECR ecr, Bundle fhirPatientBundle) {
		Patient ecrPatient = ecr.getPatient();
		log.info("Handling a patient bundle with total patients:"+fhirPatientBundle.getTotal());
		for(Entry entry : fhirPatientBundle.getEntry()) {
			ca.uhn.fhir.model.dstu2.resource.Patient patient = (ca.uhn.fhir.model.dstu2.resource.Patient)entry.getResource();
			log.info("Getting patient with id="+patient.getId().getIdPart());
			List<RestResource> availableResources = FHIRClient.getConformanceStatementResources();
			//For all available resources
			for(RestResource resource : availableResources) {
				Dictionary<String,RestResourceInteraction> interactionDict = new Hashtable<String,RestResourceInteraction>();
				for(RestResourceInteraction interaction : resource.getInteraction()) {
					interactionDict.put(interaction.getCode(), interaction);
				}
				if(interactionDict.get("read") != null) {
					switch(resource.getType()) {
						case "Condition":
							handleConditions(ecr,patient.getId());
							break;
						case "Claim":
							handleClaims(ecr,patient.getId());
							break;
						case "Encounter":
							handleEncounters(ecr,patient.getId());
							break;
						case "Immunization":
							handleImmunizations(ecr,patient.getId());
							break;
						case "MedicationAdministration":
							handleMedicationAdministrations(ecr,patient.getId());
							break;
						case "MedicationDispense":
							handleMedicationDispenses(ecr,patient.getId());
							break;
						case "MedicationOrder":
							handleMedicationOrders(ecr,patient.getId());
							break;
						case "MedicationStatement":
							handleMedicationStatements(ecr,patient.getId());
							break;
						case "Observation":
							handleObservation(ecr,patient.getId());
							break;
						case "Patient":
							handlePatient(ecr,patient);
							break;
						case "Practictioner":
							for(ResourceReferenceDt practitionerRef: patient.getCareProvider()) {
								handlePractitioner(ecr,practitionerRef);
							}
							break;
						case "Procedure":
							handleProcedure(ecr,patient.getId());
							break;
						case "RelatedPersons":
							handleRelatedPersons(ecr,patient.getId());
							break;
					}
				}
			}
			handlePatient(ecr,patient);
			//handleRelatedPersons(ecr,patientIdDt);
			for(ResourceReferenceDt practitionerRef: patient.getCareProvider()) {
				handlePractitioner(ecr,practitionerRef);
			}
			handleConditions(ecr,patient.getId());
			handleEncounters(ecr,patient.getId());
			handleMedicationOrders(ecr,patient.getId());
			handleObservation(ecr,patient.getId());
			//handleImmunizations(ecr,patientIdDt);
			//TODO: Handle ingressing visits correctly
			//TODO: Handle All Observations correctly
		}
	}
	
	public void updateJobEntry(ECR ecr) {
		List<ECRJob> searchResults = ECRJobRepository.findByReportIdOrderByIdDesc(ecr.getECRId());
		if(searchResults.size() == 0) {
			log.info("JOBUPDATE --- No Job found for Id "+ ecr.getECRId() + "!");
			return;
		}
		ECRJob job = searchResults.get(0);
		job.instantUpdate();
		ECRJobRepository.save(job);
		if(job.getStatusCode().equalsIgnoreCase("C")) {
			try {
				SchedulerService.removeScheduleService(job);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		do {
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
			relatedPersons = FHIRClient.getNextPage(relatedPersons);
		}
		while(relatedPersons != null);
	}
	
	private void handlePractitioner(ECR ecr, ResourceReferenceDt refDt) {
		Practitioner provider = FHIRClient.getPractictioner(refDt.getId());
		Provider ecrProvider = new Provider();
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
		//Update or add to the current provider list
		if(ecr.getProvider().contains(ecrProvider)) {
			for(Provider originalProvider:ecr.getProvider()) {
				if(originalProvider.equals(ecrProvider))
					originalProvider.update(ecrProvider);
			}
		}
		else {
			ecr.getProvider().add(ecrProvider);
		}
	}
	
	private void handleMedicationAdministrations(ECR ecr,IdDt IdDt) {
		Bundle medications = FHIRClient.getMedicationAdministrations(IdDt);
		do {
			for(Entry entry : medications.getEntry()) {
				CodeableConcept ecrCode = new CodeableConcept();
				MedicationAdministration medicationAdministration = (MedicationAdministration)entry.getResource();
				gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
				log.info("MEDICATIONADMINISTRATION --- Trying medicationAdministration: " + medicationAdministration.getId());
				IDatatype medicationCodeUntyped = medicationAdministration.getMedication();
				log.info("MEDICATIONADMINISTRATION --- medication code element class: " + medicationCodeUntyped.getClass());
				if(medicationCodeUntyped instanceof CodeableConceptDt) {
					CodeableConceptDt code = (CodeableConceptDt)medicationCodeUntyped;
					log.info("MEDICATIONADMINISTRATION --- Trying code with this many codings: " + code.getCoding().size());
					for(CodingDt coding : code.getCoding()) {
						log.info("MEDICATIONADMINISTRATION --- Trying coding: " + coding.getDisplay());
						CodeableConcept concept = FHIRCoding2ECRConcept(coding);
						log.info("MEDICATIONADMINISTRATION --- Translated to ECRconcept:" + concept.toString());
						ecrMedication.setCode(concept.getcode());
						ecrMedication.setSystem(concept.getsystem());
						ecrMedication.setDisplay(concept.getdisplay());
						ecrCode.setcode(concept.getcode());
						ecrCode.setsystem(concept.getsystem());
						ecrCode.setdisplay(concept.getdisplay());
					}
				}
				if(!medicationAdministration.getDosage().isEmpty()) {
					Dosage ecrDosage = new Dosage();
					ecrDosage.setValue(medicationAdministration.getDosage().getQuantity().getValue().toString());
					ecrDosage.setUnit(medicationAdministration.getDosage().getQuantity().getUnit());
					ecrMedication.setDosage(ecrDosage);
				}
				if(!medicationAdministration.getEffectiveTime().isEmpty()) {
					ecrMedication.setDate(HAPIFHIRUtil.getDate(medicationAdministration.getEffectiveTime()).toString());
				}
				log.info("MEDICATIONADMINISTRATION --- ECRCode: " + ecrCode);
				if(ControllerUtils.isSTIMed(ecrCode) && !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
					log.info("MEDICATIONADMINISTRATION --- Found New Entry: " + ecrCode);
					ecr.getPatient().getMedicationProvided().add(ecrMedication);
				}
				else {
					log.info("MEDICATIONADMINISTRATION --- Didn't Match or found duplicate! " + ecrCode);
				}
				
				if(!medicationAdministration.getReasonGiven().isEmpty()) {
					for(CodeableConceptDt reason: medicationAdministration.getReasonGiven()) {
						handleSingularConditionConceptCode(ecr, reason);
					}
				}
			}
			medications = FHIRClient.getNextPage(medications);
		}
		while(medications != null);
	}
	
	private void handleMedicationDispenses(ECR ecr,IdDt IdDt) {
		Bundle medications = FHIRClient.getMedicationDispenses(IdDt);
		do {
			for(Entry entry : medications.getEntry()) {
				CodeableConcept ecrCode = new CodeableConcept();
				MedicationDispense medicationDispense = (MedicationDispense)entry.getResource();
				gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
				log.info("MEDICATIONDISPENSE --- Trying medicationDispense: " + medicationDispense.getId());
				IDatatype medicationCodeUntyped = medicationDispense.getMedication();
				log.info("MEDICATIONDISPENSE --- medication code element class: " + medicationCodeUntyped.getClass());
				if(medicationCodeUntyped instanceof CodeableConceptDt) {
					CodeableConceptDt code = (CodeableConceptDt)medicationCodeUntyped;
					log.info("MEDICATIONDISPENSE --- Trying code with this many codings: " + code.getCoding().size());
					for(CodingDt coding : code.getCoding()) {
						log.info("MEDICATIONDISPENSE --- Trying coding: " + coding.getDisplay());
						CodeableConcept concept = FHIRCoding2ECRConcept(coding);
						log.info("MEDICATIONDISPENSE --- Translated to ECRconcept:" + concept.toString());
						ecrMedication.setCode(concept.getcode());
						ecrMedication.setSystem(concept.getsystem());
						ecrMedication.setDisplay(concept.getdisplay());
						ecrCode.setcode(concept.getcode());
						ecrCode.setsystem(concept.getsystem());
						ecrCode.setdisplay(concept.getdisplay());
					}
				}
				for(ca.uhn.fhir.model.dstu2.resource.MedicationDispense.DosageInstruction dosageInstruction : medicationDispense.getDosageInstruction()) {
					Dosage ecrDosage = new Dosage();
					IDatatype doseUntyped = dosageInstruction.getDose();
					log.info("MEDICATIONDISPENSE --- Found Dosage: " + doseUntyped.toString());
					if(doseUntyped instanceof SimpleQuantityDt) {
						SimpleQuantityDt doseTyped = (SimpleQuantityDt)doseUntyped;
						log.info("MEDICATIONDISPENSE --- Dosage is of SimpleQuentityDt Type");
						ecrDosage.setValue(doseTyped.getValue().toString());
						ecrDosage.setUnit(doseTyped.getUnit());
						ecrMedication.setDosage(ecrDosage);
					}
					String periodUnit = dosageInstruction.getTiming().getRepeat().getPeriodUnits();
					BigDecimal period = dosageInstruction.getTiming().getRepeat().getPeriod();
					Integer frequency = dosageInstruction.getTiming().getRepeat().getFrequency();
					String commonFrequency= "" + frequency + " times per " + period + " " + periodUnit;
					log.info("MEDICATIONDISPENSE --- Found Frequency: " + commonFrequency);
					ecrMedication.setFrequency(commonFrequency);
				}
				Date timeDispensed = medicationDispense.getWhenHandedOver();
				log.info("MEDICATIONDISPENSE --- Found Handed Over Date: " + timeDispensed);
				ecrMedication.setDate(DateUtil.DateTimeToStdString(timeDispensed));
				log.info("MEDICATIONDISPENSE --- ECRCode: " + ecrCode);
				if(ControllerUtils.isSTIMed(ecrCode) && !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
					log.info("MEDICATIONDISPENSE --- Found New Entry: " + ecrCode);
					ecr.getPatient().getMedicationProvided().add(ecrMedication);
				}
				else {
					log.info("MEDICATIONDISPENSE --- Didn't Match or found duplicate! " + ecrCode);
				}
			}
			medications = FHIRClient.getNextPage(medications);
		}
		while(medications != null);
	}
	
	private void handleMedicationOrders(ECR ecr, IdDt IdDt) {
		Bundle medications = FHIRClient.getMedicationOrders(IdDt);
		do {
			for(Entry entry : medications.getEntry()) {
				CodeableConcept ecrCode = new CodeableConcept();
				MedicationOrder medicationOrder = (MedicationOrder)entry.getResource();
				gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
				log.info("MEDICATIONORDER --- Trying medicationOrder: " + medicationOrder.getId());
				IDatatype medicationCodeUntyped = medicationOrder.getMedication();
				log.info("MEDICATIONORDER --- medication code element class: " + medicationCodeUntyped.getClass());
				if(medicationCodeUntyped instanceof CodeableConceptDt) {
					CodeableConceptDt code = (CodeableConceptDt)medicationCodeUntyped;
					log.info("MEDICATIONORDER --- Trying code with this many codings: " + code.getCoding().size());
					for(CodingDt coding : code.getCoding()) {
						log.info("MEDICATIONORDER --- Trying coding: " + coding.getDisplay());
						CodeableConcept concept = FHIRCoding2ECRConcept(coding);
						log.info("MEDICATIONORDER --- Translated to ECRconcept:" + concept.toString());
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
					log.info("MEDICATIONORDER --- Found Dosage: " + doseUntyped.toString());
					if(doseUntyped instanceof SimpleQuantityDt) {
						SimpleQuantityDt doseTyped = (SimpleQuantityDt)doseUntyped;
						log.info("MEDICATIONORDER --- Dosage is of SimpleQuentityDt Type");
						ecrDosage.setValue(doseTyped.getValue().toString());
						ecrDosage.setUnit(doseTyped.getUnit());
						ecrMedication.setDosage(ecrDosage);
					}
					String periodUnit = dosageInstruction.getTiming().getRepeat().getPeriodUnits();
					BigDecimal period = dosageInstruction.getTiming().getRepeat().getPeriod();
					Integer frequency = dosageInstruction.getTiming().getRepeat().getFrequency();
					String commonFrequency= "" + frequency + " times per " + period + " " + periodUnit;
					log.info("MEDICATIONORDER --- Found Frequency: " + commonFrequency);
					ecrMedication.setFrequency(commonFrequency);
				}
				
				PeriodDt period = medicationOrder.getDispenseRequest().getValidityPeriod();
				log.info("MEDICATIONORDER --- Found Validity Period: " + period);
				ecrMedication.setDate(period.getStart().toString());
				log.info("MEDICATIONORDER --- ECRCode: " + ecrCode);
				if(ControllerUtils.isSTIMed(ecrCode) && !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
					log.info("MEDICATIONORDER --- Found New Entry: " + ecrCode);
					ecr.getPatient().getMedicationProvided().add(ecrMedication);
				}
				else {
					log.info("MEDICATIONORDER --- Didn't Match or found duplicate! " + ecrCode);
				}
				if(medicationOrder.getReason() != null && !medicationOrder.getReason().isEmpty()) {
					if(medicationOrder.getReason() instanceof CodeableConceptDt) {
						handleSingularConditionConceptCode(ecr, (CodeableConceptDt)medicationOrder.getReason());
					}
					else if(medicationOrder.getReason() instanceof ResourceReferenceDt) {
						handleSingularCondition(ecr, (ResourceReferenceDt)medicationOrder.getReason());
					}
				}
			}
			medications = FHIRClient.getNextPage(medications);
		}
		while(medications != null);
	}
	
	private void handleMedicationStatements(ECR ecr,IdDt IdDt) {
		Bundle medications = FHIRClient.getMedicationStatements(IdDt);
		do {
			for(Entry entry : medications.getEntry()) {
				CodeableConcept ecrCode = new CodeableConcept();
				MedicationStatement medicationStatement = (MedicationStatement)entry.getResource();
				gatech.edu.STIECR.JSON.Medication ecrMedication = new gatech.edu.STIECR.JSON.Medication();
				log.info("MEDICATIONSTATEMENT  --- Trying medicationOrder: " + medicationStatement.getId());
				IDatatype medicationCodeUntyped = medicationStatement.getMedication();
				log.info("MEDICATIONSTATEMENT  --- medication code element class: " + medicationCodeUntyped.getClass());
				if(medicationCodeUntyped instanceof CodeableConceptDt) {
					CodeableConceptDt code = (CodeableConceptDt)medicationCodeUntyped;
					log.info("MEDICATIONSTATEMENT  --- Trying code with this many codings: " + code.getCoding().size());
					for(CodingDt coding : code.getCoding()) {
						log.info("MEDICATIONSTATEMENT  --- Trying coding: " + coding.getDisplay());
						CodeableConcept concept = FHIRCoding2ECRConcept(coding);
						log.info("MEDICATIONSTATEMENT  --- Translated to ECRconcept:" + concept.toString());
						ecrMedication.setCode(concept.getcode());
						ecrMedication.setSystem(concept.getsystem());
						ecrMedication.setDisplay(concept.getdisplay());
						ecrCode.setcode(concept.getcode());
						ecrCode.setsystem(concept.getsystem());
						ecrCode.setdisplay(concept.getdisplay());
					}
				}
				if(!medicationStatement.getDosage().isEmpty()) {
					Dosage ecrDosage = new Dosage();
					IDatatype dosageQuantityUntyped = medicationStatement.getDosage().get(0).getQuantity();
					if(dosageQuantityUntyped instanceof SimpleQuantityDt) {
						SimpleQuantityDt dosageQuantity = (SimpleQuantityDt)dosageQuantityUntyped;
						ecrDosage.setValue(dosageQuantity.getValue().toString());
						ecrDosage.setUnit(dosageQuantity.getUnit().toString());
					}
					else if(dosageQuantityUntyped instanceof RangeDt) {
						RangeDt dosageRange = (RangeDt)dosageQuantityUntyped;
						BigDecimal high = dosageRange.getHigh().getValue();
						BigDecimal low = dosageRange.getLow().getValue();
						BigDecimal mean = high.add(low);
						mean = mean.divide(new BigDecimal(2));
						ecrDosage.setValue(mean.toString());
						ecrDosage.setUnit(dosageRange.getHigh().getUnit());
						ecrMedication.setDosage(ecrDosage);
					}
				}
				if(!medicationStatement.getDateAssertedElement().isEmpty()) {
					String dateTimeAsString = DateUtil.DateTimeToStdString(medicationStatement.getDateAsserted());
					log.info("MEDICATIONSTATEMENT  --- Found Medication Date: " + dateTimeAsString);
					ecrMedication.setDate(dateTimeAsString);
					log.info("MEDICATIONSTATEMENT  --- ECRCode: " + ecrCode);
				}
				if(ControllerUtils.isSTIMed(ecrCode) && !ecr.getPatient().getMedicationProvided().contains(ecrMedication)) {
					log.info("MEDICATIONSTATEMENT  --- Found New Entry: " + ecrCode);
					ecr.getPatient().getMedicationProvided().add(ecrMedication);
				}
				else {
					log.info("MEDICATIONSTATEMENT  --- Didn't Match or found duplicate! " + ecrCode);
				}
				if(!medicationStatement.getReasonForUse().isEmpty()) {
					if(medicationStatement.getReasonForUse() instanceof CodeableConceptDt) {
						handleSingularConditionConceptCode(ecr, (CodeableConceptDt)medicationStatement.getReasonForUse());
					}
					else if(medicationStatement.getReasonForUse() instanceof ResourceReferenceDt) {
						handleSingularCondition(ecr, (ResourceReferenceDt)medicationStatement.getReasonForUse());
					}
				}
			}
			medications = FHIRClient.getNextPage(medications);
		}
		while(medications != null);
	}
	
	private void handleImmunizations(ECR ecr, IdDt IdDt) {
		Bundle immunizations = FHIRClient.getImmunizations(IdDt);
		do {
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
			immunizations = FHIRClient.getNextPage(immunizations);
		}
		while(immunizations != null);
	}
	
	private void handleConditions(ECR ecr, IdDt IdDt) {
		Bundle conditions = FHIRClient.getConditions(IdDt);
		do {
			for(Entry entry : conditions.getEntry()) {
				Condition condition = (Condition)entry.getResource();
				handleSingularCondition(ecr,condition);
			}
			conditions = FHIRClient.getNextPage(conditions);
		}
		while(conditions != null);
	}
	
	public void handleSingularConditionConceptCode(ECR ecr,CodeableConceptDt code) {
		log.info("CONDITION --- Trying code with this many codings: " + code.getCoding().size());
		for(CodingDt coding : code.getCoding()) {
			log.info("CONDITION --- Trying coding: " + coding.getDisplay());
			CodeableConcept concept = FHIRCoding2ECRConcept(coding);
			log.info("CONDITION --- Translated to ECRconcept:" + concept.toString());
			if(ControllerUtils.isSTICode(concept) && !ecr.getPatient().getsymptoms().contains(concept)) {
				log.info("CONDITION --- SYMPTOM MATCH!" + concept.toString());
				ecr.getPatient().getsymptoms().add(concept);
			}
		}
	}
	
	public void handleSingularCondition(ECR ecr,Condition condition) {
		log.info("CONDITION --- Trying condition: " + condition.getId());
		Date abatementDate = condition.getAbatement() != null ? HAPIFHIRUtil.getDate(condition.getAbatement()) : null;
		if(abatementDate != null && abatementDate.compareTo(new Date()) <= 0) {
			log.info("CONDITION --- Found abatement date of: " + abatementDate);
			log.info("CONDITION --- Condition is not current, ignoring condition.");
			return;
		}
		Date onsetDate = HAPIFHIRUtil.getDate(condition.getOnset());
		Date ecrDate = null;
		try {
			ecrDate = DateUtil.DateTimeStringToDateTime(ecr.getPatient().getdateOfOnset());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CodeableConceptDt code = condition.getCode();
		log.info("CONDITION --- Trying code with this many codings: " + code.getCoding().size());
		for(CodingDt coding : code.getCoding()) {
			log.info("CONDITION --- Trying coding: " + coding.getDisplay());
			CodeableConcept concept = FHIRCoding2ECRConcept(coding);
			log.info("CONDITION --- Translated to ECRconcept:" + concept.toString());
			if(ControllerUtils.isSTIDiagnosisCode(concept) && (ecr.getPatient().getDiagnosis() == null || !ecr.getPatient().getDiagnosis().getCode().equals(concept.getcode()))){
				log.info("CONDITION ---DIAGNOSIS MATCH!" + concept.toString());
				Diagnosis updatedDiagnosis = new Diagnosis();
				updatedDiagnosis.setCode(concept.getcode());
				updatedDiagnosis.setDisplay(concept.getdisplay());
				updatedDiagnosis.setSystem(concept.getsystem());
				if(ecrDate == null || (onsetDate != null && ecrDate != null && onsetDate.compareTo(ecrDate) < 0)) {
					log.info("CONDITION --- Found onset date of: " + onsetDate);
					log.info("CONDITION --- Eariler date than previously found. Replacing patient onset date.");
					ecr.getPatient().setdateOfOnset(DateUtil.DateTimeToStdString(onsetDate));
					updatedDiagnosis.setDate(DateUtil.DateTimeToStdString(onsetDate));
				}
				else{
					updatedDiagnosis.setDate(ecr.getPatient().getdateOfOnset());
				}
				ecr.getPatient().setDiagnosis(updatedDiagnosis);
				return;
			}
		}
		handleSingularConditionConceptCode(ecr,code);
		//TODO: distinguish between symptom list and diagnosis list here
		//TODO: Map Pregnant from encounters
	}
	
	public void handleSingularCondition(ECR ecr,ResourceReferenceDt conditionReference) {
		Condition condition = FHIRClient.getConditionById(conditionReference.getReference());
		handleSingularCondition(ecr,condition);
	}
	
	private void handleClaims(ECR ecr, IdDt IdDt) {
		Bundle claims = FHIRClient.getClaims(IdDt);
		do {
			for(Entry entry : claims.getEntry()) {
				Claim claim = (Claim)entry.getResource();
				if(!claim.getCoverage().isEmpty()) {
					log.info("CLAIMS --- Found an example of coverage:");
					Coverage coverage = FHIRClient.getCoverageById(claim.getCoverage().get(0).getCoverage().getReference()); //Handling only the first coverage
					CodingDt coding = coverage.getType(); //Use the first code
					log.info("CLAIMS --- Found coverage type:" + coding.getDisplay());
					ecr.getPatient().setinsuranceType(new CodeableConcept(coding.getCode(),coding.getSystem(),coding.getDisplay()));
				}
			}
			claims = FHIRClient.getNextPage(claims);
		}
		while(claims != null);
	}
	
	private void handleEncounters(ECR ecr, IdDt IdDt) {
		Bundle encounters = FHIRClient.getEncounters(IdDt);
		do {
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
			}
			encounters = FHIRClient.getNextPage(encounters);
		}
		while(encounters != null);
	}
	
	private void handleObservation(ECR ecr, IdDt IdDt) {
		Bundle observations = FHIRClient.getObservations(IdDt);
		do {
			for(Entry entry : observations.getEntry()) {
				Observation observation = (Observation)entry.getResource();
				CodeableConceptDt code = observation.getCode();
				for(CodingDt coding: code.getCoding()) {
					if(coding.getCode().equalsIgnoreCase("laboratory")) { //HIT! Found a lab result
						LabOrderCode labOrder = new LabOrderCode();
						labOrder.setcode(coding.getCode());
						labOrder.setdisplay(coding.getDisplay());
						labOrder.setsystem(coding.getSystem());
						LabResult labResult = new LabResult();
						IDatatype untypedValue = observation.getValue();
						if(untypedValue instanceof QuantityDt) {
							labResult.setValue(((QuantityDt)untypedValue).getValue().toString());
						}
						else if(untypedValue instanceof CodeableConcept) {
							labResult.setValue(((CodeableConcept)untypedValue).getdisplay());
						}
						else if(untypedValue instanceof StringDt) {
							labResult.setValue(((StringDt)untypedValue).toString());
						}
						else if(untypedValue instanceof RangeDt) {
							RangeDt range = (RangeDt)untypedValue;
							labResult.setValue("High:"+range.getHigh()+";low:"+range.getLow());
						}
						else if(untypedValue instanceof RatioDt) {
							RatioDt ratio = (RatioDt)untypedValue;
							labResult.setValue(ratio.getNumerator().toString()+"/"+ratio.getDenominator().toString());
						}
						else if(untypedValue instanceof SampledDataDt) {
							labResult.setValue(((SampledDataDt)untypedValue).getData());
						}
						else if(untypedValue instanceof TimeDt) {
							labResult.setValue(((TimeDt)untypedValue).getValue());
						}
						else if(untypedValue instanceof DateTimeDt) {
							labResult.setValue(((DateTimeDt)untypedValue).getValueAsString());
						}
						else if(untypedValue instanceof PeriodDt) {
							PeriodDt period = (PeriodDt)untypedValue;
							labResult.setValue("Start:"+period.getStart().toString()+";End"+period.getEnd().toString());
						}
						labOrder.getLaboratory_Results().add(labResult);
					}
				}
			}
			observations = FHIRClient.getNextPage(observations);
		}
		while(observations != null);
	}
	
	private void handleProcedure(ECR ecr,IdDt IdDt) {
		Bundle procedures = FHIRClient.getProcedures(IdDt);
		do {
			for(Entry entry : procedures.getEntry()) {
				Procedure procedure = (Procedure)entry.getResource();
				if(!procedure.getReason().isEmpty()) {
					if(procedure.getReason() instanceof CodeableConceptDt) {
						handleSingularConditionConceptCode(ecr, (CodeableConceptDt)procedure.getReason());
					}
					else if(procedure.getReason() instanceof ResourceReferenceDt) {
						handleSingularCondition(ecr, (ResourceReferenceDt)procedure.getReason());
					}
				}
				
			}
			procedures = FHIRClient.getNextPage(procedures);
		}
		while(procedures != null);
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
