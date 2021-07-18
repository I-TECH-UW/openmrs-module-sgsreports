/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.sgsreports.dataset.definition.evaluator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.BeanPropertyComparator;
import org.openmrs.module.reporting.common.ObjectCounter;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.ObsValueConverter;
import org.openmrs.module.reporting.data.encounter.definition.ObsForEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.dataset.definition.evaluator.EncounterDataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.sgsreports.dataset.definition.SurgicalLogDataSetDefinition;
import org.openmrs.module.sgsreports.util.MetadataLookup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The logic that evaluates a {@link SurgicalLogDataSetDefinition} and produces an {@link DataSet}
 * 
 * @see SurgicalLogDataSetDefinition
 */
@Handler(supports = { SurgicalLogDataSetDefinition.class }, order = 25)
public class SurgicalLogDataSetEvaluator extends EncounterDataSetEvaluator {
	
	protected static final Log log = LogFactory.getLog(SurgicalLogDataSetEvaluator.class);
	
	@Autowired
	BuiltInEncounterDataLibrary encounterData;
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 * @should evaluate an EncounterAndObsDataSetDefinition
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		Concept surgicalProcedure = MetadataLookup.getConcept("4d17dc4e-c95d-4c2b-8cd0-d17a519e9504");
		Concept surgeon = MetadataLookup.getConcept("a97ea6ac-8f26-4000-a86d-15d828584855");
		Concept entryIntoTheatre = MetadataLookup.getConcept("4f33f025-1504-41b8-ab8c-914cd72baadc");
		Concept entryIntoOr = MetadataLookup.getConcept("6a0cb2c2-90e5-4e6b-8fea-c5bb29c74f74");
		Concept entryIntoPacu = MetadataLookup.getConcept("be11fa4a-4045-4f28-8217-3db062511e32");
		Concept handOverToWard = MetadataLookup.getConcept("d8db9dac-9529-485d-980a-8bd68d33816a");
		
		List<Concept> obsWeWant = Arrays.asList(surgicalProcedure, surgeon, entryIntoTheatre, entryIntoOr, entryIntoPacu, handOverToWard);
		
		SurgicalLogDataSetDefinition dsd = (SurgicalLogDataSetDefinition) dataSetDefinition;
		
		Object doctorParam = context.getParameterValue("surgeon");
		
		if (doctorParam == null) {
			Mapped<? extends EncounterQuery> filter = getFilter(dsd.getRowFilters(), "noDoctorFilter");
			dsd.getRowFilters().clear();
			dsd.addRowFilter(filter);
		} else {
			Mapped<? extends EncounterQuery> filter = getFilter(dsd.getRowFilters(), "withAllParamsFilter");
			dsd.getRowFilters().clear();
			dsd.addRowFilter(filter);
		}
		
		// If no specific columns definitions are specified, use some defaults
		if (dsd.getColumnDefinitions().isEmpty()) {
			dsd.addColumn("ENCOUNTER_ID", encounterData.getEncounterId(), "");
			dsd.addColumn("PATIENT_ID", encounterData.getPatientId(), "");
			dsd.addColumn("ENCOUNTER_TYPE", encounterData.getEncounterTypeName(), "");
			dsd.addColumn("ENCOUNTER_DATETIME", encounterData.getEncounterDatetime(), "");
			dsd.addColumn("LOCATION", encounterData.getLocationName(), "");
		}
		
		// Add all Obs for each encounter
		ObsForEncounterDataDefinition allObs = new ObsForEncounterDataDefinition();
		allObs.setSingleObs(false);
		dsd.addColumn("OBS", allObs, "");
		
		// Produce the core starting data set for encounter data
		SimpleDataSet data = (SimpleDataSet) super.evaluate(dsd, context);
		
		// Determine all necessary column headers and get necessary obs data to populate
		// these
		Map<String, DataSetColumn> obsColumnMap = new HashMap<String, DataSetColumn>();
		ObsValueConverter obsValueConverter = new ObsValueConverter();
		
		// Get the maximum number of occurrences for each Obs column, to determine which
		// need to have sequence numbers appended
		Map<String, Integer> maxNumForKey = new HashMap<String, Integer>();
		
		for (DataSetRow row : data.getRows()) {
			List<Obs> obsToRemove = new ArrayList<Obs>();
			List<Obs> obsList = (List<Obs>) row.getColumnValue("OBS");
			for (Obs obs : obsList) {
				if (!obsWeWant.contains(obs.getConcept())) {
					obsToRemove.add(obs);
				}
			}
			
			obsList.removeAll(obsToRemove);
			
			if (obsList != null) {
				ObjectCounter<String> currentNumForKey = new ObjectCounter<String>();
				for (Obs obs : obsList) {
					String key = getObsKey(obs);
					if (key != null) {
						currentNumForKey.increment(key);
					}
				}
				
				for (Map.Entry<String, Integer> e : currentNumForKey.getAllObjectCounts().entrySet()) {
					Integer existing = maxNumForKey.get(e.getKey());
					if (existing == null || (existing < e.getValue())) {
						maxNumForKey.put(e.getKey(), e.getValue());
					}
				}
			}
		}
		
		// Add the Obs values to each dataset row
		for (DataSetRow row : data.getRows()) {
			List<Obs> obsToRemove = new ArrayList<Obs>();
			List<Obs> obsList = (List<Obs>) row.getColumnValue("OBS");
			for (Obs obs : obsList) {
				if (!obsWeWant.contains(obs.getConcept())) {
					obsToRemove.add(obs);
				}
			}
			
			obsList.removeAll(obsToRemove);
			
			if (obsList != null) {
				ObjectCounter<String> currentNumForKey = new ObjectCounter<String>();
				for (Obs obs : obsList) {
					String key = getObsKey(obs);
					if (key != null) {
						int num = currentNumForKey.increment(key);
						String columnName = ObjectUtil.format(obs.getConcept()).replaceAll("\\s", "_").replaceAll("-", "_").toUpperCase();
						if (maxNumForKey.get(key) > 1) {
							columnName = columnName + "_" + num;
						}
						DataSetColumn obsColumn = obsColumnMap.get(columnName);
						if (obsColumn == null) {
							obsColumn = new DataSetColumn(columnName, columnName, Object.class);
							obsColumnMap.put(columnName, obsColumn);
						}
						row.addColumnValue(obsColumn, obsValueConverter.convert(obs));
					}
				}
			}
			
			row.removeColumn("OBS");
		}
		data.getMetaData().removeColumn("OBS");
		
		// Sort the obs columns by name, and add to metadata
		List<DataSetColumn> obsColumns = new ArrayList<DataSetColumn>(obsColumnMap.values());
		Collections.sort(obsColumns, new BeanPropertyComparator("name"));
		data.getMetaData().getColumns().addAll(obsColumns);
		
		return data;
	}
	
	protected String getObsKey(Obs obs) {
		if (obs.isObsGrouping()) {
			return null;
		}
		StringBuilder key = new StringBuilder(obs.getConcept().getConceptId().toString());
		for (Obs toCheck = obs.getObsGroup(); toCheck != null; toCheck = toCheck.getObsGroup()) {
			key.append("_").append(toCheck.getConcept().getConceptId());
		}
		return key.toString();
	}
	
	protected Mapped<? extends EncounterQuery> getFilter(List<Mapped<? extends EncounterQuery>> filters, String filterName) {
		for (Mapped<? extends EncounterQuery> mapped : filters) {
			if (mapped.getParameterizable().getName().equalsIgnoreCase(filterName)) {
				return mapped;
			}
		}
		return null;
	}
}
