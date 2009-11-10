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
package org.lexgrid.lexevs.loaders.icdgem.impl.test;

import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcSelectFromEntityDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestJdbcSelectFromEntryStateDO;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.runner.ICDGEMJdbcTestRunner;

import junit.framework.TestCase;

/*
 * Assumption: Coding Scheme has already been loaded into LexGrid.
 */

public class I10To9CmJdbcTests  extends TestCase {
	
	public I10To9CmJdbcTests(String serverName) {
		super(serverName);
	}
	
	public void testSelectFromEntityI10To9Cm() {
		ICDGEMTestJdbcSelectFromEntityDO helper = ICDGEMTestJdbcSelectFromEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityDO.I10_9_CM);
		boolean rv = ICDGEMJdbcTestRunner.selectFromEntity(
				helper.getCsUri(), 
				helper.getCsVer(), 
				helper.getTargetEntityCode(), 
				helper.getExpectedEntityDescription());
		assertTrue(rv);
	}
	
	public void testSelectFromEntryStateI10To9Cm() {
		ICDGEMTestJdbcSelectFromEntryStateDO helper = ICDGEMTestJdbcSelectFromEntryStateDO.createObj(ICDGEMTestJdbcSelectFromEntryStateDO.I10_9_CM);
		boolean rv = ICDGEMJdbcTestRunner.selectFromEntryState(
				helper.getCsUri(), 
				helper.getCsVer(), 
				helper.getTargetEntityCode(), 
				helper.getExpectedEntryType());
		assertTrue(rv);
	}
	
	public void testSelectFromEntityAssnsToEntityI10To9Cm() {
		ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO helper = ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.createObj(ICDGEMTestJdbcSelectFromEntityAssnsToEntityDO.I10_9_CM);
		boolean rv = ICDGEMJdbcTestRunner.selectFromEntityAssnsToEntity(
				helper.getCsUri(), 
				helper.getCsVer(), 
				helper.getTargetEntityCode(),
				helper.getExpectedSourceEntityCode(), 
				helper.getExpectedCodingSchemeName());
		assertTrue(rv);
	}

}
