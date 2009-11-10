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

public class ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO {
	public static final int I9_10_CM   = 1;
	public static final int I10_9_CM   = 2;
	public static final int I9_10_PCS  = 3;
	public static final int I10_9_PCS  = 4;

	private ICDGEMTestBasicDO _bdo;  // get csUri and csVer from here
	private String _targetEntityCode; 
	private String _expectedSourceEntityCode;
	private String _expectedCodingSchemeName;
	
	
	private ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO(
			ICDGEMTestBasicDO bdo, 
			String targetEntityCode,
			String expectedSourceEntityCode,
			String expectedCodingSchemeName) 
	{
		_bdo = bdo;
		_targetEntityCode = targetEntityCode;
		_expectedSourceEntityCode = expectedSourceEntityCode;
		_expectedCodingSchemeName = expectedCodingSchemeName;
	}
	
	public static ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO createObj(int type) {
		switch(type) {
			case I9_10_CM: return dataForI9To10Cm();
			case I10_9_CM: return dataForI10To9Cm();
			case I9_10_PCS: return dataForI9To10Pcs();
			case I10_9_PCS: return dataForI10To9Pcs();
			default: System.out.println("ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO: createObj: invalid switch: " + type ); return null;
		}
	}
	
	private static ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO dataForI9To10Cm() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_CM);
		String targetEntityCode = "combination0";
		String expectedSourceEntityCode = "482.42";
		String expectedCodingSchemeName = "ICD-9-to-10-CM-GEM";
		return new ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO(bdo, targetEntityCode, expectedSourceEntityCode, expectedCodingSchemeName);
	}
	
	private static ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO dataForI10To9Cm() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_CM);
		String targetEntityCode = "combination0";
		String expectedSourceEntityCode = "K57.41";
		String expectedCodingSchemeName = "ICD-10-to-9-CM-GEM";
		return new ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO(bdo, targetEntityCode, expectedSourceEntityCode, expectedCodingSchemeName);
	}
	
	private static ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO dataForI9To10Pcs() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_PCS);
		String targetEntityCode = "combination0";
		String expectedSourceEntityCode = "85.43";
		String expectedCodingSchemeName = "ICD-9-to-10-PCS-GEM";
		return new ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO(bdo, targetEntityCode, expectedSourceEntityCode, expectedCodingSchemeName);
	}
	
	private static ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO dataForI10To9Pcs() {		
		ICDGEMTestBasicDO bdo = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_PCS);
		String targetEntityCode = "combination0";
		String expectedSourceEntityCode = "03703DZ";
		String expectedCodingSchemeName = "ICD-10-to-9-PCS-GEM";
		return new ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO(bdo, targetEntityCode, expectedSourceEntityCode, expectedCodingSchemeName);
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
	public String getExpectedSourceEntityCode() {
		return _expectedSourceEntityCode;
	}
	public String getExpectedCodingSchemeName() {
		return _expectedCodingSchemeName;
	}	
}
