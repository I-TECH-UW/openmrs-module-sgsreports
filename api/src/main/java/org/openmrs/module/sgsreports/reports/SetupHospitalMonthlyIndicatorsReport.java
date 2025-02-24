/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.sgsreports.reports;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.EncounterType;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.sgsreports.util.Cohorts;
import org.openmrs.module.sgsreports.util.Indicators;

/**
 * @author Bailly RURANGIRWA
 */
public class SetupHospitalMonthlyIndicatorsReport {
	
	private VisitType Ipd;
	
	private EncounterType admissionEncounterType;
	
	private List<VisitType> opdVisitTypes;
	
	private List<VisitType> allVisitTypes;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "HospitalMonthlyIndicatorsReport.xls", "HospitalMonthlyIndicatorsReport.xls_", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:Hospital Monthly Indicators Report Data Set");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		Helper.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("HospitalMonthlyIndicatorsReport.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Hospital Monthly Indicators Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		rd.setName("Hospital Monthly Indicators Report");
		rd.addDataSetDefinition(createBaseDataSet(), ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		Helper.saveReportDefinition(rd);
		return rd;
	}
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Hospital Monthly Indicators Report Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		VisitCohortDefinition patientsWithOpdVisitsBetweenDates = new VisitCohortDefinition();
		patientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		patientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		patientsWithOpdVisitsBetweenDates.setVisitTypeList(opdVisitTypes);
		
		CohortIndicator patientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("patientsWithOpdVisitsBetweenDatesIndicator", patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		dsd.addColumn("oPdVisits", "Outpatient Visits", new Mapped<CohortIndicator>(patientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		VisitCohortDefinition patientsWithAnyVisitBeforeDate = new VisitCohortDefinition();
		patientsWithAnyVisitBeforeDate.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		patientsWithAnyVisitBeforeDate.setVisitTypeList(allVisitTypes);
		
		CompositionCohortDefinition newPatients = new CompositionCohortDefinition();
		newPatients.setName("newPatients");
		newPatients.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		newPatients.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		newPatients.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		newPatients.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		newPatients.setCompositionString("1 and (not 2)");
		
		CohortIndicator newPatientsIndicator = Indicators.newCohortIndicator("newPatientsIndicator", newPatients, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("newPatients", "Initial/New", new Mapped<CohortIndicator>(newPatientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition followUpPatients = new CompositionCohortDefinition();
		followUpPatients.setName("followUpPatients");
		followUpPatients.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		followUpPatients.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		followUpPatients.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		followUpPatients.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		followUpPatients.setCompositionString("1 and 2");
		
		CohortIndicator followUpPatientsIndicator = Indicators.newCohortIndicator("followUpPatientsIndicator", followUpPatients, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("followUpPatients", "Follow-up/Return", new Mapped<CohortIndicator>(followUpPatientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setName("male Patients");
		males.setMaleIncluded(true);
		males.setFemaleIncluded(false);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setName("female Patients");
		females.setMaleIncluded(false);
		females.setFemaleIncluded(true);
		
		// Male
		
		CompositionCohortDefinition malePatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		malePatientsWithOpdVisitsBetweenDates.setName("malePatientsWithOpdVisitsBetweenDates");
		malePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		malePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		malePatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		malePatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
		malePatientsWithOpdVisitsBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator malePatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("malePatientsWithOpdVisitsBetweenDatesIndicator", malePatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("malePatients", "Male", new Mapped<CohortIndicator>(malePatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Female
		
		CompositionCohortDefinition femalePatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		femalePatientsWithOpdVisitsBetweenDates.setName("femalePatientsWithOpdVisitsBetweenDates");
		femalePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		femalePatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		femalePatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		femalePatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(females, null));
		femalePatientsWithOpdVisitsBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator femalePatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("femalePatientsWithOpdVisitsBetweenDatesIndicator", femalePatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("femalePatients", "Female", new Mapped<CohortIndicator>(femalePatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition unknownGenderPatientsWithOpdVisitsBetweenDates = new CompositionCohortDefinition();
		unknownGenderPatientsWithOpdVisitsBetweenDates.setName("unknownGenderPatientsWithOpdVisitsBetweenDates");
		unknownGenderPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		unknownGenderPatientsWithOpdVisitsBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		unknownGenderPatientsWithOpdVisitsBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		unknownGenderPatientsWithOpdVisitsBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
		unknownGenderPatientsWithOpdVisitsBetweenDates.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		unknownGenderPatientsWithOpdVisitsBetweenDates.setCompositionString("1 and ( not 2) and ( not 3 )");
		
		CohortIndicator unknownGenderPatientsWithOpdVisitsBetweenDatesIndicator = Indicators.newCohortIndicator("unknownGenderPatientsWithOpdVisitsBetweenDatesIndicator", unknownGenderPatientsWithOpdVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("unknownGenderPatients", "Unknown Gender", new Mapped<CohortIndicator>(unknownGenderPatientsWithOpdVisitsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Admissions
		CohortDefinition patientsWithAdmissionBetweenDates = Cohorts.getAnyEncounterOfTypesDuringPeriod(Arrays.asList(admissionEncounterType));
		
		CohortIndicator patientsWithAdmissionIndicator = Indicators.newCohortIndicator("patientsWithAdmissionIndicator", patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		dsd.addColumn("admissions", "Admissions", new Mapped<CohortIndicator>(patientsWithAdmissionIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Male
		
		CompositionCohortDefinition malePatientsWithAdmissionBetweenDates = new CompositionCohortDefinition();
		malePatientsWithAdmissionBetweenDates.setName("malePatientsWithAdmissionBetweenDates");
		malePatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		malePatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		malePatientsWithAdmissionBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startedOnOrAfter},onOrBefore=${startedOnOrBefore}")));
		
		malePatientsWithAdmissionBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
		malePatientsWithAdmissionBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator malePatientsWithAdmissionBetweenDatesIndicator = Indicators.newCohortIndicator("malePatientsWithAdmissionBetweenDatesIndicator", malePatientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("admissionsMale", "Admissions Male", new Mapped<CohortIndicator>(malePatientsWithAdmissionBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// Female
		
		CompositionCohortDefinition femalePatientsWithAdmissionBetweenDates = new CompositionCohortDefinition();
		femalePatientsWithAdmissionBetweenDates.setName("femalePatientsWithAdmissionBetweenDates");
		femalePatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		femalePatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		femalePatientsWithAdmissionBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startedOnOrAfter},onOrBefore=${startedOnOrBefore}")));
		
		femalePatientsWithAdmissionBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(females, null));
		femalePatientsWithAdmissionBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator femalePatientsWithAdmissionBetweenDatesIndicator = Indicators.newCohortIndicator("femalePatientsWithAdmissionBetweenDatesIndicator", femalePatientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("admissionsFemale", "Admissions Female", new Mapped<CohortIndicator>(femalePatientsWithAdmissionBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition unknownGenderPatientsWithAdmissionBetweenDates = new CompositionCohortDefinition();
		unknownGenderPatientsWithAdmissionBetweenDates.setName("unknownGenderPatientsWithAdmissionBetweenDates");
		unknownGenderPatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		unknownGenderPatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		unknownGenderPatientsWithAdmissionBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startedOnOrAfter},onOrBefore=${startedOnOrBefore}")));
		
		unknownGenderPatientsWithAdmissionBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(males, null));
		unknownGenderPatientsWithAdmissionBetweenDates.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		unknownGenderPatientsWithAdmissionBetweenDates.setCompositionString("1 and ( not 2) and ( not 3 )");
		
		CohortIndicator unknownGenderPatientsWithAdmissionsBetweenDatesIndicator = Indicators.newCohortIndicator("unknownGenderPatientsWithAdmissionsBetweenDatesIndicator", unknownGenderPatientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("amdmissionsunknownGender", "Admissions Unknown Gender", new Mapped<CohortIndicator>(unknownGenderPatientsWithAdmissionsBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		// In-Patient Mix
		CohortDefinition inpatientMix = Cohorts.getAnyEncounterOfTypesDuringPeriod(Arrays.asList(admissionEncounterType));
		
		CohortIndicator inpatientMixIndicator = Indicators.newCohortIndicator("inpatientMixIndicator", inpatientMix, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		dsd.addColumn("inpatientMix", "In-Patient Mix", new Mapped<CohortIndicator>(inpatientMixIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CohortDefinition pediatricPatients = Cohorts.patientWithAgeBelow(18);
		
		CompositionCohortDefinition padiatricPatientsWithAdmissionBetweenDates = new CompositionCohortDefinition();
		padiatricPatientsWithAdmissionBetweenDates.setName("femalePatientsWithAdmissionBetweenDates");
		padiatricPatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		padiatricPatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		padiatricPatientsWithAdmissionBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startedOnOrAfter},onOrBefore=${startedOnOrBefore}")));
		
		padiatricPatientsWithAdmissionBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		padiatricPatientsWithAdmissionBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator padiatricPatientsWithAdmissionBetweenDatesIndicator = Indicators.newCohortIndicator("padiatricPatientsWithAdmissionBetweenDatesIndicator", padiatricPatientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("admissionsPediatric", "Admissions Pediatric", new Mapped<CohortIndicator>(padiatricPatientsWithAdmissionBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CohortDefinition adultPatients = Cohorts.patientWithAgeAbove(18);
		
		CompositionCohortDefinition adultPatientsWithAdmissionBetweenDates = new CompositionCohortDefinition();
		adultPatientsWithAdmissionBetweenDates.setName("adultPatientsWithAdmissionBetweenDates");
		adultPatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		adultPatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		adultPatientsWithAdmissionBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startedOnOrAfter},onOrBefore=${startedOnOrBefore}")));
		
		adultPatientsWithAdmissionBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		adultPatientsWithAdmissionBetweenDates.setCompositionString("1 and 2");
		
		CohortIndicator adultPatientsWithAdmissionBetweenDatesIndicator = Indicators.newCohortIndicator("adultPatientsWithAdmissionBetweenDatesIndicator", adultPatientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("admissionsAdult", "Admissions Adult", new Mapped<CohortIndicator>(adultPatientsWithAdmissionBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		CompositionCohortDefinition unknownAgePatientsWithAdmissionBetweenDates = new CompositionCohortDefinition();
		unknownAgePatientsWithAdmissionBetweenDates.setName("unknownAgePatientsWithAdmissionBetweenDates");
		unknownAgePatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		unknownAgePatientsWithAdmissionBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		unknownAgePatientsWithAdmissionBetweenDates.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("onOrAfter=${startedOnOrAfter},onOrBefore=${startedOnOrBefore}")));
		unknownAgePatientsWithAdmissionBetweenDates.getSearches().put("2", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		unknownAgePatientsWithAdmissionBetweenDates.getSearches().put("3", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		unknownAgePatientsWithAdmissionBetweenDates.setCompositionString("1 and ( not 2) and ( not 3 )");
		
		CohortIndicator unknownAgePatientsWithAdmissionBetweenDatesIndicator = Indicators.newCohortIndicator("unknownAgePatientsWithAdmissionBetweenDatesIndicator", unknownAgePatientsWithAdmissionBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("admissionsUnknownAge", "Admissions UnknownAge", new Mapped<CohortIndicator>(unknownAgePatientsWithAdmissionBetweenDatesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
	}
	
	private void setUpProperties() {
		Ipd = Context.getVisitService().getVisitTypeByUuid("c228eab1-3f10-11e4-adec-0800271c1b75");
		admissionEncounterType = Context.getEncounterService().getEncounterTypeByUuid("81da9590-3f10-11e4-adec-0800271c1b75");
		allVisitTypes = Context.getVisitService().getAllVisitTypes(false);
		opdVisitTypes = allVisitTypes;
		opdVisitTypes.remove(Ipd);
		
	}
	
}
