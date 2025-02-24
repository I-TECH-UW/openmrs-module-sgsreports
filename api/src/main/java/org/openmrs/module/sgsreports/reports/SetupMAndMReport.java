package org.openmrs.module.sgsreports.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.definition.MappedParametersEncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sgsreports.dataset.definition.MAndMFormDataSetDefinition;
import org.openmrs.module.sgsreports.reports.library.BasePatientDataLibrary;
import org.openmrs.module.sgsreports.util.GlobalPropertiesManagement;
import org.openmrs.module.sgsreports.util.MetadataLookup;

public class SetupMAndMReport {
	
	private Concept mAndM;
	
	BuiltInEncounterDataLibrary encounterData = new BuiltInEncounterDataLibrary();
	
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	protected final static Log log = LogFactory.getLog(SetupMAndMReport.class);
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "MAndMReport.xls", "MAndMReport.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("MAndMReport.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("M and M Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("M and M Report");
		reportDefinition.addParameter(new Parameter("startDate", "From Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "To Date", Date.class));
		
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		MAndMFormDataSetDefinition dsd = new MAndMFormDataSetDefinition();
		dsd.setName("dataSet");
		dsd.setParameters(getParameters());
		
		SqlEncounterQuery rowFilter = new SqlEncounterQuery();
		rowFilter.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		rowFilter.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		rowFilter.setQuery("select encounter_id from obs where concept_id = " + mAndM.getConceptId() + " and obs_datetime >=:onOrAfter and   obs_datetime <=:onOrBefore and voided=0 ");
		
		MappedParametersEncounterQuery q = new MappedParametersEncounterQuery(rowFilter, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
		dsd.addRowFilter(Mapped.mapStraightThrough(q));
		
		dsd.addColumn("ENCOUNTER_DATETIME", encounterData.getEncounterDatetime(), "");
		PatientIdentifierDataDefinition i = new PatientIdentifierDataDefinition();
		i.addType(Context.getPatientService().getPatientIdentifierType(2));
		dsd.addColumn("PATIENT_ID", i, (String) null);
		
		PreferredNameDataDefinition d = new PreferredNameDataDefinition();
		
		dsd.addColumn("familyName", d, "", new PropertyConverter(PersonName.class, "familyName"));
		dsd.addColumn("middleName", d, "", new PropertyConverter(PersonName.class, "middleName"));
		dsd.addColumn("givenName", d, "", new PropertyConverter(PersonName.class, "givenName"));
		
		dsd.addColumn("AGE", basePatientData.getAgeAtEndInYears(), "");
		
		reportDefinition.addDataSetDefinition("dataSet", Mapped.mapStraightThrough(dsd));
		
	}
	
	public List<Parameter> getParameters() {
		List<Parameter> l = new ArrayList<Parameter>();
		l.add(new Parameter("startDate", "From Date", Date.class));
		l.add(new Parameter("endDate", "To Date", Date.class));
		return l;
	}
	
	private void setupProperties() {
		
		mAndM = MetadataLookup.getConcept("57cc7302-eb7d-4da2-920c-452149f7e7bc");
		
	}
	
}
