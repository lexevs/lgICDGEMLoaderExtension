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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.LexGrid.LexBIG.Impl.testUtility.ServiceHolder;

public class ICDGEMAllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.lexgrid.lexevs.loaders.icdgem.impl.test");
		ServiceHolder.configureForSingleConfig();
		
		//$JUnit-BEGIN$
		suite.addTestSuite(ICDGEMTest.class);
		suite.addTestSuite(CleanUpTest.class);
		//$JUnit-END$
		return suite;
	}

}
