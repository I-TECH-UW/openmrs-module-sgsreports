package org.openmrs.module.sgsreports.reports.library;

import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.encounter.definition.AuditInfoEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.ConvertedEncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.library.BuiltInEncounterDataLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;

public class BuiltInEncounterDataLibrary2 extends BuiltInEncounterDataLibrary {
	
	public BuiltInEncounterDataLibrary2() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@DocumentedDefinition
	public EncounterDataDefinition getAuditInfo() {
		return new AuditInfoEncounterDataDefinition();
	}
	
	@DocumentedDefinition
	public EncounterDataDefinition getCreatorName() {
		return convert(getAuditInfo(), new DataConverterLibrary().getAuditInfoCreatorNameConverter());
	}
	
	protected ConvertedEncounterDataDefinition convert(EncounterDataDefinition d, DataConverter... converters) {
		return new ConvertedEncounterDataDefinition(d, converters);
	}
	
}
