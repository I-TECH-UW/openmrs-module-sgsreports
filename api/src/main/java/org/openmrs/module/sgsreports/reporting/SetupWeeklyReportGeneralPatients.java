/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.sgsreports.reporting;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Location;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
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
public class SetupWeeklyReportGeneralPatients {
	
	private List<VisitType> allVisitTypes;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "weeklyReportGeneralPatients.xls", "weeklyReportGeneralPatients.xls_", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset: Weekly Report General Patients Data Set");
		props.put("sortWeight", "5000");
		design.setProperties(props);
		Helper.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("weeklyReportGeneralPatients.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Weekly Report General Patients Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		rd.addParameter(getLocationListParameter());
		rd.setName("Weekly Report General Patients Report");
		rd.addDataSetDefinition(createBaseDataSet(), ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		
		// We only wont to see general patients in this report
		PersonAttributeCohortDefinition generalPatients = Cohorts.getGeneralPatients();
		
		VisitCohortDefinition patientsWithAnyVisitsAtLocationBetweenDates = new VisitCohortDefinition();
		patientsWithAnyVisitsAtLocationBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		patientsWithAnyVisitsAtLocationBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		patientsWithAnyVisitsAtLocationBetweenDates.addParameter(getLocationListParameter());
		patientsWithAnyVisitsAtLocationBetweenDates.setVisitTypeList(allVisitTypes);
		
		CompositionCohortDefinition generalPatientsWithAnyVisitAtLocation = new CompositionCohortDefinition();
		generalPatientsWithAnyVisitAtLocation.setName("newPediatricClientsFemales");
		generalPatientsWithAnyVisitAtLocation.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		generalPatientsWithAnyVisitAtLocation.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		generalPatientsWithAnyVisitAtLocation.addParameter(getLocationListParameter());
		generalPatientsWithAnyVisitAtLocation.getSearches().put("1", new Mapped<CohortDefinition>(generalPatients, null));
		generalPatientsWithAnyVisitAtLocation.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitsAtLocationBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore},locationList=${locationList}")));
		generalPatientsWithAnyVisitAtLocation.setCompositionString("(1 and 2");
		
		rd.setBaseCohortDefinition(generalPatientsWithAnyVisitAtLocation, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore},locationList=${locationList}"));
		Helper.saveReportDefinition(rd);
		return rd;
	}
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Weekly Report General Patients Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		VisitCohortDefinition patientsWithAnyVisitsBetweenDates = new VisitCohortDefinition();
		patientsWithAnyVisitsBetweenDates.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		patientsWithAnyVisitsBetweenDates.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		patientsWithAnyVisitsBetweenDates.setVisitTypeList(allVisitTypes);
		
		VisitCohortDefinition patientsWithAnyVisitBeforeDate = new VisitCohortDefinition();
		patientsWithAnyVisitBeforeDate.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		patientsWithAnyVisitBeforeDate.setVisitTypeList(allVisitTypes);
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setName("male Patients");
		males.setMaleIncluded(true);
		males.setFemaleIncluded(false);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setName("female Patients");
		females.setMaleIncluded(false);
		females.setFemaleIncluded(true);
		
		CohortDefinition pediatricPatients = Cohorts.patientWithAgeBelow(18);
		
		CohortDefinition adultPatients = Cohorts.patientWithAgeAbove(18);
		
		/* New Pediatric Clients Females */
		CompositionCohortDefinition newPediatricClientsFemales = new CompositionCohortDefinition();
		newPediatricClientsFemales.setName("newPediatricClientsFemales");
		newPediatricClientsFemales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		newPediatricClientsFemales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		newPediatricClientsFemales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		newPediatricClientsFemales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		newPediatricClientsFemales.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		newPediatricClientsFemales.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		newPediatricClientsFemales.setCompositionString("(1 and (not 2)) and 3 and 4");
		
		CohortIndicator newPediatricClientsFemalesIndicator = Indicators.newCohortIndicator("newPediatricClientsFemalesIndicator", newPediatricClientsFemales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("newPediatricClientsFemales", "New Pediatric Clients Females", new Mapped<CohortIndicator>(newPediatricClientsFemalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* New Pediatric Clients Males */
		CompositionCohortDefinition newPediatricClientsMales = new CompositionCohortDefinition();
		newPediatricClientsMales.setName("newPediatricClientsMales");
		newPediatricClientsMales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		newPediatricClientsMales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		newPediatricClientsMales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		newPediatricClientsMales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		newPediatricClientsMales.getSearches().put("3", new Mapped<CohortDefinition>(males, null));
		newPediatricClientsMales.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		newPediatricClientsMales.setCompositionString("(1 and (not 2)) and 3 and 4");
		
		CohortIndicator newPediatricClientsMalesIndicator = Indicators.newCohortIndicator("newClientsFemalesIndicator", newPediatricClientsMales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("newPediatricClientsMales", "New Pediatric Clients Males", new Mapped<CohortIndicator>(newPediatricClientsMalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* New Adult Clients Females */
		CompositionCohortDefinition newAdultsClientsFemales = new CompositionCohortDefinition();
		newAdultsClientsFemales.setName("newAdultsClientsFemales");
		newAdultsClientsFemales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		newAdultsClientsFemales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		newAdultsClientsFemales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		newAdultsClientsFemales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		newAdultsClientsFemales.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		newAdultsClientsFemales.getSearches().put("4", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		newAdultsClientsFemales.setCompositionString("(1 and (not 2)) and 3 and 4");
		
		CohortIndicator newAdultsClientsFemalesIndicator = Indicators.newCohortIndicator("newAdultsClientsFemalesIndicator", newAdultsClientsFemales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("newAdultsClientsFemales", "New Adults Clients Females", new Mapped<CohortIndicator>(newAdultsClientsFemalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* New Adults Clients Males */
		CompositionCohortDefinition newAdultsClientsMales = new CompositionCohortDefinition();
		newAdultsClientsMales.setName("newPediatricClientsMales");
		newAdultsClientsMales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		newAdultsClientsMales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		newAdultsClientsMales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		
		newAdultsClientsMales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		newAdultsClientsMales.getSearches().put("3", new Mapped<CohortDefinition>(males, null));
		newAdultsClientsMales.getSearches().put("4", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		newAdultsClientsMales.setCompositionString("(1 and (not 2)) and 3 and 4");
		
		CohortIndicator newAdultsClientsMalesIndicator = Indicators.newCohortIndicator("newAdultsClientsMalesIndicator", newAdultsClientsMales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("newAdultsClientsMales", "New Adults Clients Males", new Mapped<CohortIndicator>(newAdultsClientsMalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Follow-up Pediatric Clients Females */
		CompositionCohortDefinition followUpPediatricClientsFemales = new CompositionCohortDefinition();
		followUpPediatricClientsFemales.setName("followUpPediatricClientsFemales");
		followUpPediatricClientsFemales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		followUpPediatricClientsFemales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		followUpPediatricClientsFemales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		followUpPediatricClientsFemales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		followUpPediatricClientsFemales.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		followUpPediatricClientsFemales.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		followUpPediatricClientsFemales.setCompositionString("1 and 2 and 3 and 4");
		
		CohortIndicator followUpPediatricClientsFemalesIndicator = Indicators.newCohortIndicator("followUpPediatricClientsFemalesIndicator", followUpPediatricClientsFemales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("followUpPediatricClientsFemales", "Follow-up Pediatric Clients Females", new Mapped<CohortIndicator>(followUpPediatricClientsFemalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Follow-up Pediatric Clients Males */
		CompositionCohortDefinition followUpPediatricClientsMales = new CompositionCohortDefinition();
		followUpPediatricClientsMales.setName("followUpPediatricClientsMales");
		followUpPediatricClientsMales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		followUpPediatricClientsMales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		followUpPediatricClientsMales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		followUpPediatricClientsMales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		followUpPediatricClientsMales.getSearches().put("3", new Mapped<CohortDefinition>(males, null));
		followUpPediatricClientsMales.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		followUpPediatricClientsMales.setCompositionString("1 and 2 and 3 and 4");
		
		CohortIndicator followUpPediatricClientsMalesIndicator = Indicators.newCohortIndicator("followUpPediatricClientsMalesIndicator", followUpPediatricClientsMales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("followUpPediatricClientsMales", "Follow-up Pediatric Clients Males", new Mapped<CohortIndicator>(followUpPediatricClientsMalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Follow-up Adults Clients Females */
		CompositionCohortDefinition followUpAdultsClientsFemales = new CompositionCohortDefinition();
		followUpAdultsClientsFemales.setName("followUpAdultsClientsFemales");
		followUpAdultsClientsFemales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		followUpAdultsClientsFemales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		followUpAdultsClientsFemales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		followUpAdultsClientsFemales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		followUpAdultsClientsFemales.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		followUpAdultsClientsFemales.getSearches().put("4", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		followUpAdultsClientsFemales.setCompositionString("1 and 2 and 3 and 4");
		
		CohortIndicator followUpAdultsClientsFemalesIndicator = Indicators.newCohortIndicator("followUpAdultsClientsFemalesIndicator", followUpAdultsClientsFemales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("followUpAdultsClientsFemales", "Follow-up Adults Clients Females", new Mapped<CohortIndicator>(followUpAdultsClientsFemalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Follow-up Adults Clients Males */
		CompositionCohortDefinition followUpAdultsClientsMales = new CompositionCohortDefinition();
		followUpAdultsClientsMales.setName("followUpAdultsClientsMales");
		followUpAdultsClientsMales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		followUpAdultsClientsMales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		followUpAdultsClientsMales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		followUpAdultsClientsMales.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithAnyVisitBeforeDate, ParameterizableUtil.createParameterMappings("startedOnOrBefore=${startedOnOrAfter}")));
		followUpAdultsClientsMales.getSearches().put("3", new Mapped<CohortDefinition>(males, null));
		followUpAdultsClientsMales.getSearches().put("4", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		
		followUpAdultsClientsMales.setCompositionString("1 and 2 and 3 and 4");
		
		CohortIndicator followUpAdultsClientsMalesIndicator = Indicators.newCohortIndicator("followUpAdultsClientsMalesIndicator", followUpAdultsClientsMales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("followUpAdultsClientsMales", "Follow-up Adults Clients Males", new Mapped<CohortIndicator>(followUpAdultsClientsMalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Total Pediatric Clients Females */
		CompositionCohortDefinition totalPediatricClientsFemales = new CompositionCohortDefinition();
		totalPediatricClientsFemales.setName("totalPediatricClientsFemales");
		totalPediatricClientsFemales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		totalPediatricClientsFemales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		totalPediatricClientsFemales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		totalPediatricClientsFemales.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		totalPediatricClientsFemales.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalPediatricClientsFemales.setCompositionString("1 and 3 and 4");
		
		CohortIndicator totalPediatricClientsFemalesIndicator = Indicators.newCohortIndicator("totalPediatricClientsFemalesIndicator", totalPediatricClientsFemales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("totalPediatricClientsFemales", "Total Pediatric Clients Females", new Mapped<CohortIndicator>(totalPediatricClientsFemalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Total Pediatric Clients Males */
		CompositionCohortDefinition totalPediatricClientsMales = new CompositionCohortDefinition();
		totalPediatricClientsMales.setName("totalPediatricClientsMales");
		totalPediatricClientsMales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		totalPediatricClientsMales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		totalPediatricClientsMales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		totalPediatricClientsMales.getSearches().put("3", new Mapped<CohortDefinition>(males, null));
		totalPediatricClientsMales.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalPediatricClientsMales.setCompositionString("1 and 3 and 4");
		
		CohortIndicator totalPediatricClientsMalesIndicator = Indicators.newCohortIndicator("totalPediatricClientsMalesIndicator", totalPediatricClientsMales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("totalPediatricClientsMales", "Total Pediatric Clients Males", new Mapped<CohortIndicator>(totalPediatricClientsMalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Total Adults Clients Females */
		CompositionCohortDefinition totalAdultsClientsFemales = new CompositionCohortDefinition();
		totalAdultsClientsFemales.setName("totalAdultsClientsFemales");
		totalAdultsClientsFemales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		totalAdultsClientsFemales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		totalAdultsClientsFemales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		totalAdultsClientsFemales.getSearches().put("3", new Mapped<CohortDefinition>(females, null));
		totalAdultsClientsFemales.getSearches().put("4", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalAdultsClientsFemales.setCompositionString("1 and 3 and 4");
		
		CohortIndicator totalAdultsClientsFemalesIndicator = Indicators.newCohortIndicator("totalAdultsClientsFemalesIndicator", totalAdultsClientsFemales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("totalAdultsClientsFemales", "Total Adults Clients Females", new Mapped<CohortIndicator>(totalAdultsClientsFemalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Total Adults Clients Males */
		CompositionCohortDefinition totalAdultsClientsMales = new CompositionCohortDefinition();
		totalAdultsClientsMales.setName("totalAdultsClientsMales");
		totalAdultsClientsMales.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		totalAdultsClientsMales.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		totalAdultsClientsMales.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		totalAdultsClientsMales.getSearches().put("3", new Mapped<CohortDefinition>(males, null));
		totalAdultsClientsMales.getSearches().put("4", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalAdultsClientsMales.setCompositionString("1 and 3 and 4");
		
		CohortIndicator totalAdultsClientsMalesIndicator = Indicators.newCohortIndicator("totalAdultsClientsMalesIndicator", totalAdultsClientsMales, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("totalAdultsClientsMales", "Total Adults Clients Males", new Mapped<CohortIndicator>(totalAdultsClientsMalesIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Total Pediatric Clients */
		CompositionCohortDefinition totalPediatricClients = new CompositionCohortDefinition();
		totalPediatricClients.setName("totalPediatricClients");
		totalPediatricClients.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		totalPediatricClients.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		totalPediatricClients.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		totalPediatricClients.getSearches().put("3", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalPediatricClients.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalPediatricClients.setCompositionString("1 and 4 and (not 3)");
		
		CohortIndicator totalPediatricClientsIndicator = Indicators.newCohortIndicator("totalPediatricClientsIndicator", totalPediatricClients, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("totalPediatricClients", "Total Pediatric Clients", new Mapped<CohortIndicator>(totalPediatricClientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		/* Total Adults Clients */
		CompositionCohortDefinition totalAdultsClients = new CompositionCohortDefinition();
		totalAdultsClients.setName("totalAdultsClients");
		totalAdultsClients.addParameter(new Parameter("startedOnOrAfter", "Started on or after", Date.class));
		totalAdultsClients.addParameter(new Parameter("startedOnOrBefore", "Started on or before", Date.class));
		totalAdultsClients.getSearches().put("1", new Mapped<CohortDefinition>(patientsWithAnyVisitsBetweenDates, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		totalAdultsClients.getSearches().put("3", new Mapped<CohortDefinition>(adultPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalAdultsClients.getSearches().put("4", new Mapped<CohortDefinition>(pediatricPatients, ParameterizableUtil.createParameterMappings("effectiveDate=${startedOnOrBefore}")));
		totalAdultsClients.setCompositionString("1 and 3 and (not 4)");
		
		CohortIndicator totalAdultsClientsIndicator = Indicators.newCohortIndicator("totalAdultsClientsIndicator", totalAdultsClients, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		dsd.addColumn("totalAdultsClients", "Total Adults Clients", new Mapped<CohortIndicator>(totalAdultsClientsIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
	}
	
	private void setUpProperties() {
		allVisitTypes = Context.getVisitService().getAllVisitTypes(false);
	}
	
	public Parameter getLocationListParameter() {
		Parameter locationList = new Parameter("locationList", "Visit Location(s)", Location.class, List.class, null);
		locationList.setRequired(false);
		return locationList;
	}
}
