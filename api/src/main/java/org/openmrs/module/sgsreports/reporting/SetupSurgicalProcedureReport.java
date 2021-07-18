package org.openmrs.module.sgsreports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sgsreports.dataset.definition.SurgicalLogDataSetDefinition;
import org.openmrs.module.sgsreports.reporting.library.BasePatientDataLibrary;
import org.openmrs.module.sgsreports.reporting.library.DataFactory;
import org.openmrs.module.sgsreports.util.MetadataLookup;

public class SetupSurgicalProcedureReport {
	
	private Concept cureSurgicalLogForm;
	
	private Concept surgeonName;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	private DataFactory dataFactory = new DataFactory();
	
	protected final static Log log = LogFactory.getLog(SetupSurgicalProcedureReport.class);
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createCsvReportDesign(rd, "surgicalProcedureReport");
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("surgicalProcedureReport".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Surgical Procedure Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Surgical Procedure Report");
		reportDefinition.setParameters(getParameters());
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		SurgicalLogDataSetDefinition dsd = new SurgicalLogDataSetDefinition();
		dsd.setName("dataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery noDoctoriFilter = new SqlEncounterQuery();
		noDoctoriFilter.setParameters(getQueryParameters());
		noDoctoriFilter.setQuery("select encounter_id from obs where concept_id = " + cureSurgicalLogForm.getConceptId() + " and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery noDoctorFilterMapp = new MappedParametersEncounterQuery(noDoctoriFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,surgeon=surgeon"));
		noDoctorFilterMapp.setName("noDoctorFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(noDoctorFilterMapp));
		
		SqlEncounterQuery withAllParamsFilter = new SqlEncounterQuery();
		withAllParamsFilter.setParameters(getQueryParameters());
		withAllParamsFilter.setQuery("select encounter_id from obs where concept_id = " + surgeonName.getConceptId() + " and value_coded=:surgeon and encounter_id in (select encounter_id from obs where concept_id = " + cureSurgicalLogForm.getConceptId() + ") and obs_datetime >=:onOrAfter and obs_datetime <=:onOrBefore and voided=0");
		
		MappedParametersEncounterQuery withAllParamsFilterMapp = new MappedParametersEncounterQuery(withAllParamsFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate,diagnosis=diagnosis,surgeon=surgeon"));
		withAllParamsFilterMapp.setName("withAllParamsFilter");
		dsd.addRowFilter(Mapped.mapStraightThrough(withAllParamsFilterMapp));
		
		PatientIdentifierDataDefinition i = new PatientIdentifierDataDefinition();
		i.addType(Context.getPatientService().getPatientIdentifierType(3));
		dsd.addColumn("CRK", i, (String) null);
		
		dsd.addColumn("familyName", basePatientData.getPreferredFamilyName(), "");
		dsd.addColumn("middleName", basePatientData.getPreferredMiddleName(), "");
		dsd.addColumn("givenName", basePatientData.getPreferredGivenName(), "");
		dsd.addColumn("age", basePatientData.getAgeAtEndInYears(), "");
		dsd.addColumn("procedureDate", encounterData.getEncounterDatetime(), "");
		dsd.addColumn("OR Time", dataFactory.getCureSurgicalLogOrTime(), "");
		
		reportDefinition.addDataSetDefinition("dataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	private void setupProperties() {
		cureSurgicalLogForm = MetadataLookup.getConcept("4df362b7-5994-4cc2-ba81-fe48fd4b30a3");
		surgeonName = MetadataLookup.getConcept("a97ea6ac-8f26-4000-a86d-15d828584855");
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("startDate", "From Date", Date.class));
		l.add(new Parameter("endDate", "To Date", Date.class));
		
		Parameter surgeon = new Parameter("surgeon", "Surgeon", Concept.class);
		surgeon.setRequired(false);
		l.add(surgeon);
		return l;
	}
	
	public List<Parameter> getQueryParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("onOrAfter", "On Or After", Date.class));
		l.add(new Parameter("onOrBefore", "On Or Before", Date.class));
		
		Parameter surgeon = new Parameter("surgeon", "Surgeon", Concept.class);
		surgeon.setRequired(false);
		l.add(surgeon);
		return l;
	}
	
}
