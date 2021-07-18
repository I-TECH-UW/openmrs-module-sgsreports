package org.openmrs.module.sgsreports.dataset.definition;

import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.definition.EncounterAndObsDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

/**
 * Definition of an EncounterAndObs DataSet
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("reporting.EncounterAndObsDataSetDefinition")
public class EncounterAndObsDataSetDefinition22 extends EncounterAndObsDataSetDefinition {
	
	public EncounterAndObsDataSetDefinition22() {
	}
}
