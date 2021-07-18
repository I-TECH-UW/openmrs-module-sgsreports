/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.sgsreports.data.encounter.definition;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.common.ScriptingLanguage;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

/**
 * A Encounter data definition made by performing calculations based on multiple encounter data
 * definitions
 */
@Localized("reporting.ScriptedCompositionEncounterDataDefinition")
public class ScriptedCompositionEncounterDataDefinition extends BaseDataDefinition implements EncounterDataDefinition {
	
	// ***** PROPERTIES *****
	
	@ConfigurationProperty(required = true)
	private Map<String, Mapped<EncounterDataDefinition>> containedDataDefinitions;
	
	@ConfigurationProperty(required = true)
	private ScriptingLanguage scriptType;
	
	@ConfigurationProperty(required = true)
	private String scriptCode;
	
	@Override
	public Class<?> getDataType() {
		return Object.class;
	}
	
	/**
	 * Gets the contained encounter data definitions
	 * 
	 * @return the containedDataDefinitions
	 */
	public Map<String, Mapped<EncounterDataDefinition>> getContainedDataDefinitions() {
		if (containedDataDefinitions == null) {
			containedDataDefinitions = new HashMap<String, Mapped<EncounterDataDefinition>>();
		}
		return containedDataDefinitions;
	}
	
	/**
	 * Sets the contained patients data definitions
	 * 
	 * @param containedDataDefinitions the containedDataDefinitions to set
	 */
	
	public void setContainedDataDefinitions(Map<String, Mapped<EncounterDataDefinition>> containedDataDefinitions) {
		this.containedDataDefinitions = containedDataDefinitions;
	}
	
	/**
	 * Adds contained patients data definitions
	 * 
	 * @param key
	 * @param encountreDataDefinitions the encounter data definitions
	 */
	public void addContainedDataDefinition(String key, Mapped<EncounterDataDefinition> encounterDataDefinitions) {
		getContainedDataDefinitions().put(key, encounterDataDefinitions);
	}
	
	/**
	 * Adds a contained encounter data definition
	 * 
	 * @param key
	 * @param encounterDataDefintion the encounter data definition
	 * @param mappings
	 */
	public void addContainedDataDefinition(String key, EncounterDataDefinition encounterDataDefintion, Map<String, Object> mappings) {
		addContainedDataDefinition(key, new Mapped<EncounterDataDefinition>(encounterDataDefintion, mappings));
	}
	
	public ScriptingLanguage getScriptType() {
		return scriptType;
	}
	
	public void setScriptType(ScriptingLanguage scriptType) {
		this.scriptType = scriptType;
	}
	
	public String getScriptCode() {
		return scriptCode;
	}
	
	public void setScriptCode(String scriptCode) {
		this.scriptCode = scriptCode;
	}
}
