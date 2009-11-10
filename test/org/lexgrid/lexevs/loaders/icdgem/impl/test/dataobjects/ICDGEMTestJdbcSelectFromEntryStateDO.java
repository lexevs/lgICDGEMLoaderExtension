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

public class ICDGEMTestJdbcSelectFromEntryStateDO {
	public static final int I9_10_CM   = 1;
	public static final int I10_9_CM   = 2;
	public static final int I9_10_PCS  = 3;
	public static final int I10_9_PCS  = 4;

	private ICDGEMTestBasicDO _bdo;  // get csUri and csVer from here
	private String _targetEntityCode; 
	private String _expectedEntryType;
	
	private ICDGEMTestJdbcSelectFromEntryStateDO(
			ICDGEMTestBasicDO bdo, 
			String targetEntityCode,
			String expectedEntryType) 
	{
		_bdo = bdo;
		_targetEntityCode = targetEntityCode;
		_expectedEntryType = expectedEntryType;
	}
	
	public static ICDGEMTestJdbcSelectFromEntryStateDO createObj(int type) {
		switch(type) {
			case I9_10_CM: return dataForI9To10Cm();
			case I10_9_CM: return dataForI10To9Cm();
			case I9_10_PCS: return dataForI9To10Pcs();
			case I10_9_PCS: return dataForI10To9Pcs();
			default: System.out.println("ICDGEMTestJdbcSelectFromEntryStateDO: createObj: invalid switch: " + type ); return null;
		}
	}
	
	private static ICDGEMTestJdbcSelectFromEntryStateDO dataForI9To10Cm() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_CM);
		String targetEntityCode = "combination0"; 
		String expectedEntryType = "entity";		
		return new ICDGEMTestJdbcSelectFromEntryStateDO(bdo, targetEntityCode, expectedEntryType);
	}
	
	private static ICDGEMTestJdbcSelectFromEntryStateDO dataForI10To9Cm() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_CM);
		String targetEntityCode = "combination0"; 
		String expectedEntryType = "entity";
		return new ICDGEMTestJdbcSelectFromEntryStateDO(bdo, targetEntityCode, expectedEntryType);
	}
	
	private static ICDGEMTestJdbcSelectFromEntryStateDO dataForI9To10Pcs() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_PCS);
		String targetEntityCode = "combination0"; 
		String expectedEntryType = "entity";
		return new ICDGEMTestJdbcSelectFromEntryStateDO(bdo, targetEntityCode, expectedEntryType);
	}
	
	private static ICDGEMTestJdbcSelectFromEntryStateDO dataForI10To9Pcs() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_PCS);
		String targetEntityCode = "combination0"; 
		String expectedEntryType = "entity";
		return new ICDGEMTestJdbcSelectFromEntryStateDO(bdo, targetEntityCode, expectedEntryType);
	}
	
	public String getCsUri() {
		return _bdo.getCsUri();
	}
	public String getCsVer() {
		return _bdo.getCsVer();
	}
	public String getTargetEntityCode() {
		return _targetEntityCode;
	}
	public String getExpectedEntryType() {
		return _expectedEntryType;
	}
}
