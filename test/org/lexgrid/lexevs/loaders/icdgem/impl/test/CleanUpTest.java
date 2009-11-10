/*
 * Copyright: (c) 2004-2007 Mayo Foundation for Medical Education and 
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

import junit.framework.TestCase;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Impl.testUtility.ServiceHolder;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceManager;
import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
import org.lexgrid.lexevs.loaders.icdgem.impl.test.dataobjects.ICDGEMTestBasicDO;


public class CleanUpTest extends TestCase {
	
	public CleanUpTest(String serverName) {
		super(serverName);
	}
   
	private void removeLoad(String uri, String version) throws LBException {
//		System.out.println("CleanUpTest: remove data: " + uri + "/" + version);
		ServiceHolder.configureForSingleConfig();
		LexBIGServiceManager lbsm = ServiceHolder.instance().getLexBIGService().getServiceManager(null);
		AbsoluteCodingSchemeVersionReference a = ConvenienceMethods.createAbsoluteCodingSchemeVersionReference(
				uri, version);
		lbsm.deactivateCodingSchemeVersion(a,null);
		lbsm.removeCodingSchemeVersion(a);		
	}
	
	public void testRemoveLoadIcd9to10cm()throws LBException {
		ICDGEMTestBasicDO helper = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_CM);
		this.removeLoad(helper.getCsUri(), helper.getCsVer());
	}
	
	public void testRemoveLoadIcd9to10pcs()throws LBException {
		ICDGEMTestBasicDO helper = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I9_10_PCS);
		this.removeLoad(helper.getCsUri(), helper.getCsVer());
	}
	
	public void testRemoveLoadIcd10to9cm()throws LBException {
		ICDGEMTestBasicDO helper = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_CM);
		this.removeLoad(helper.getCsUri(), helper.getCsVer());
	}

	public void testRemoveLoadIcd10to9pcs()throws LBException {
		ICDGEMTestBasicDO helper = ICDGEMTestBasicDO.createObj(ICDGEMTestBasicDO.I10_9_PCS);
		this.removeLoad(helper.getCsUri(), helper.getCsVer());
	}
	
	
}



