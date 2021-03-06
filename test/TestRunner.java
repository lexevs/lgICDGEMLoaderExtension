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

/**
 * Runs the test suite by invoking the Ant launcher.
 * <pre>
 * Usage: java TestRunner
 * </pre>
 */
public class TestRunner {

	public static void main(String[] args) {
		try {
			new TestRunner().run(args);
		} catch (Exception e) {
			System.out.println("[lgICDGEMLoaderEx] *** Launch failed: "
				+ e.toString());
		}
	}

	public TestRunner() {
		super();
	}

	/**
	 * Primary entry point for the program.
	 * 
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
		
		org.apache.tools.ant.launch.Launcher.main(
			new String[] {"-buildfile", "lgICDGEMLoaderEx-GenerateReport.xml", "lgICDGEMLoaderEx-html"});
	}
}