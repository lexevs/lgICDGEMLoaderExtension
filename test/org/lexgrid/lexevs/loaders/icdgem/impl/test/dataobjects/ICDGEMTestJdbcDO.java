/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects;

public class ICDGEMTestJdbcDO {
	public static final int I9_10_CM   = 1;
	public static final int I10_9_CM   = 2;
	public static final int I9_10_PCS  = 3;
	public static final int I10_9_PCS  = 4;

	private ICDGEMTestBasicDO _basicDO;
	private ICDGEMTestJdbcSelectFromEntityDO _entityDO;
	private ICDGEMTestJdbcSelectFromEntryStateDO _entryStateDO;
	private ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO _assnsToEntityDO;
	
	
	private ICDGEMTestJdbcDO(
			ICDGEMTestBasicDO basicDO, 
			ICDGEMTestJdbcSelectFromEntityDO entityDO,
			ICDGEMTestJdbcSelectFromEntryStateDO entryStateDO,
			ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO assnsToEntityDO
			)
	{
		_basicDO = basicDO;
		_entityDO = entityDO;
		_entryStateDO = entryStateDO;
		_assnsToEntityDO = assnsToEntityDO;
	}
	
	public static ICDGEMTestJdbcDO createObj(int type) {
		switch(type) {
			case I9_10_CM: return dataForI9To10Cm();
			case I10_9_CM: return dataForI10To9Cm();
			case I9_10_PCS: return dataForI9To10Pcs();
			case I10_9_PCS: return dataForI10To9Pcs();
			default: System.out.println("ICDGEMTestJdbcDO: createObj: invalid switch: " + type ); return null;
		}
	}
	
	private static ICDGEMTestJdbcDO dataForI9To10Cm() {
		ICDGEMTestBasicDO basicDO = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_CM);
		ICDGEMTestJdbcSelectFromEntityDO entityDO = ICDGEMTestJdbcSelectFromEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityDO.I9_10_CM);
		ICDGEMTestJdbcSelectFromEntryStateDO entryStateDO = ICDGEMTestJdbcSelectFromEntryStateDO.createObj(ICDGEMTestJdbcSelectFromEntryStateDO.I9_10_CM);
		ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO assnsToEntityDO = ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.I9_10_CM);		
		return new ICDGEMTestJdbcDO(basicDO, entityDO, entryStateDO, assnsToEntityDO);
	}
	
	private static ICDGEMTestJdbcDO dataForI10To9Cm() {
		ICDGEMTestBasicDO basicDO = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_CM);
		ICDGEMTestJdbcSelectFromEntityDO entityDO = ICDGEMTestJdbcSelectFromEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityDO.I10_9_CM);
		ICDGEMTestJdbcSelectFromEntryStateDO entryStateDO = ICDGEMTestJdbcSelectFromEntryStateDO.createObj(ICDGEMTestJdbcSelectFromEntryStateDO.I10_9_CM);
		ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO assnsToEntityDO = ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.I10_9_CM);		
		return new ICDGEMTestJdbcDO(basicDO, entityDO, entryStateDO, assnsToEntityDO);
	}
	
	private static ICDGEMTestJdbcDO dataForI9To10Pcs() {
		ICDGEMTestBasicDO basicDO = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_PCS);
		ICDGEMTestJdbcSelectFromEntityDO entityDO = ICDGEMTestJdbcSelectFromEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityDO.I9_10_PCS);
		ICDGEMTestJdbcSelectFromEntryStateDO entryStateDO = ICDGEMTestJdbcSelectFromEntryStateDO.createObj(ICDGEMTestJdbcSelectFromEntryStateDO.I9_10_PCS);
		ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO assnsToEntityDO = ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.I9_10_PCS);		
		return new ICDGEMTestJdbcDO(basicDO, entityDO, entryStateDO, assnsToEntityDO);
	}
	
	private static ICDGEMTestJdbcDO dataForI10To9Pcs() {
		ICDGEMTestBasicDO basicDO = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_PCS);
		ICDGEMTestJdbcSelectFromEntityDO entityDO = ICDGEMTestJdbcSelectFromEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityDO.I10_9_PCS);
		ICDGEMTestJdbcSelectFromEntryStateDO entryStateDO = ICDGEMTestJdbcSelectFromEntryStateDO.createObj(ICDGEMTestJdbcSelectFromEntryStateDO.I10_9_PCS);
		ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO assnsToEntityDO = ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.I10_9_PCS);		
		return new ICDGEMTestJdbcDO(basicDO, entityDO, entryStateDO, assnsToEntityDO);
	}
	
	public ICDGEMTestBasicDO getBasicDO() {
		return _basicDO;
	}	
	public ICDGEMTestJdbcSelectFromEntityDO getSelectFromEntityDO() {
		return _entityDO;
	}
	public ICDGEMTestJdbcSelectFromEntryStateDO getSelectFromEntryStateDO() {
		return _entryStateDO;
	}
	public ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO getSelectFromEntityAssnsToEntityDO() {
		return _assnsToEntityDO;
	}
}
