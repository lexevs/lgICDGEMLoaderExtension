package org.lexgrid.extension.loaders.icdgem.utils;

import org.LexGrid.messaging.LgMessageDirectorIF;

public class ICDConceptFactory {
	
	public static BaseConcept createConcept(int conType, String code, ICDGEMProperties props) {
		BaseConcept rv = null;
		if(conType == ICDGEMConstants.CON_TYPE_10_CM) {
			rv = new ICD10CMConcept(code, props);
		} else if (conType == ICDGEMConstants.CON_TYPE_10_PCS) {
			rv = new ICD10PCSConcept(code, props);
		} else if(conType == ICDGEMConstants.CON_TYPE_9_CM) {
			rv = new ICD9CMConcept(code, props);
		} else if(conType == ICDGEMConstants.CON_TYPE_9_PCS) {
			rv = new ICD9PCSConcept(code, props);
		} else if(conType == ICDGEMConstants.CON_TYPE_ROOT) {
			rv = new RootConcept(props);
		} else {
			LgMessageDirectorIF md = props.getMessageDirector();
			md.error("ICDConceptFactory: createConcpet: invalid concept type identifier: " + conType);
			rv = null;
		}
		return rv;
	}
}
