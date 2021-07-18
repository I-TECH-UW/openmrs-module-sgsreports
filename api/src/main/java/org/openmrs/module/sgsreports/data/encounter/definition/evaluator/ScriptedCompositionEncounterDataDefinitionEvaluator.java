/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.sgsreports.data.encounter.definition.evaluator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.evaluator.EncounterDataEvaluator;
import org.openmrs.module.reporting.data.encounter.service.EncounterDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.sgsreports.data.encounter.definition.ScriptedCompositionEncounterDataDefinition;
import org.openmrs.module.sgsreports.util.MetadataLookup;

/**
 * Evaluates a ScriptedCompositionEncounterDataDefinition to produce a EncounterData
 */
@Handler(supports = ScriptedCompositionEncounterDataDefinition.class, order = 50)
public class ScriptedCompositionEncounterDataDefinitionEvaluator implements EncounterDataEvaluator {
	
	protected final static Log log = LogFactory.getLog(ScriptedCompositionEncounterDataDefinitionEvaluator.class);
	
	@Override
	public EvaluatedEncounterData evaluate(EncounterDataDefinition definition, EvaluationContext context) throws EvaluationException {
		
		ScriptedCompositionEncounterDataDefinition encountreDataDefinition = (ScriptedCompositionEncounterDataDefinition) definition;
		Map<String, Mapped<EncounterDataDefinition>> containedDataDefintions = encountreDataDefinition.getContainedDataDefinitions();
		Map<String, EvaluatedEncounterData> evaluatedContainedDataDefinitions = new HashMap<String, EvaluatedEncounterData>();
		
		// fail if passed-in definition has no encounter data definitions on it
		if (containedDataDefintions.size() < 1) {
			throw new EvaluationException("No encounter data definition(s) found on this ScriptedCompositionEncounterDataDefinition");
		}
		
		// fail if passed-in definition has no script code specified
		if (encountreDataDefinition.getScriptCode() == null) {
			throw new EvaluationException("No script code found on this ScriptedCompositionEncounterDataDefinition");
		}
		
		// fail if passed-in definition has no script type specified
		if (encountreDataDefinition.getScriptType() == null) {
			throw new EvaluationException("No script type found on this ScriptedCompositionEncounterDataDefinition");
		}
		
		// evaluate the contained data definitions and put the results in the
		// "evaluatedContainedDataDefinitions" map
		for (Entry<String, Mapped<EncounterDataDefinition>> d : containedDataDefintions.entrySet()) {
			EvaluatedEncounterData encounterDataResult = Context.getService(EncounterDataService.class).evaluate(d.getValue(), context);
			evaluatedContainedDataDefinitions.put(d.getKey(), encounterDataResult);
		}
		
		EvaluatedEncounterData evaluationResult = new EvaluatedEncounterData(encountreDataDefinition, context);
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine scriptEngine = manager.getEngineByName(encountreDataDefinition.getScriptType().getLanguage());
		scriptEngine.put("evaluationContext", context);
		scriptEngine.put("parameters", context.getParameterValues());
		
		// Kind of base cohort, we want encounters that have the CURE surgical log form
		List<Obs> surgicalLogObs = Context.getObsService().getObservationsByPersonAndConcept(null, MetadataLookup.getConcept("4df362b7-5994-4cc2-ba81-fe48fd4b30a3"));
		HashSet<Integer> surgicalLogencounters = new HashSet<Integer>();
		for (Obs obs : surgicalLogObs) {
			surgicalLogencounters.add(obs.getEncounter().getEncounterId());
		}
		for (Integer encounterId : surgicalLogencounters) { // iterate across all surgical log encounters
			for (Entry<String, EvaluatedEncounterData> dataEntry : evaluatedContainedDataDefinitions.entrySet()) {
				Object o = dataEntry.getValue().getData().get(encounterId);
				// put the definition result key and the corresponding actual object directly in
				// the scripting context
				scriptEngine.put(dataEntry.getKey(), o);
			}
			
			try {
				// execute the script for the current encounter.
				Object o = scriptEngine.eval(encountreDataDefinition.getScriptCode());
				// put the returned object value in the evaluationResult for the current
				// encounter
				evaluationResult.addData(encounterId, o);
			}
			catch (ScriptException ex) {
				throw new EvaluationException("An error occured while evaluating script", ex);
			}
		}
		
		return evaluationResult;
		
	}
}
