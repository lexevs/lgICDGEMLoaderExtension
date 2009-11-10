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
 *      http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexgrid.extension.loaders.icdgem.utils;

/**
 * A global number generator utility.
 * 
 * @author Michael Turk (turk.michael@mayo.edu)
 */

public class NumGen {
	
	private static int _entryStateId = 0;
	private static int _complexConcpetId = 0;	
	public synchronized static int getNextEntryStateId() {
		int rv = _entryStateId;
		++_entryStateId;
		return rv;
	}

	public synchronized static int getNextComplexConceptId() {
		int rv = _complexConcpetId;
		++_complexConcpetId;
		return rv;
	}	
	
	/*
	 * Reset the counters at the start of each load.
	 * Usually the loader is run via a script where
	 * one load is done and the JVM goes away.
	 * 
	 * We run in a single JVM when testing so we should
	 * reset the counters when the loader is run again.
	 * 
	 * Should only be called from:
	 *   public ICDGEMLoaderImpl(ICDGEMProperties props)
	 */
	public synchronized static void resetCounters() {
		_entryStateId = 0;
		_complexConcpetId = 0;
	}
}
