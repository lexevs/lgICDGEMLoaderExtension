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
package org.lexgrid.extension.loaders.icdgem.admin;

import java.net.URI;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.Exceptions.LBResourceUnavailableException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.Impl.dataAccess.ResourceManager;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceManager;
import org.LexGrid.annotations.LgAdminFunction;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.lexgrid.extension.loaders.icdgem.impl.ICDGEMLoaderImpl;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

/**
 * Imports ICD GEM (General Equivalence Mapping data to the LexBIG repository.
 * 
 * <pre>
 * Example: java org.lexgrid.extension.loaders.icdgem.admin.LoadICDGEM
 *   -in,--input &lt;uri&gt; URI or path specifying location of the source file
 *   -type, --input &lt;type&gt; type of mapping.  vlaid values are: i10to9cm, i9to10cm, i10to9pcs, i9to10pcs 
 *   -a, --activate ActivateScheme on successful load; if unspecified the vocabulary is loaded but not activated   
 *   -t, --tag &lt;id&gt; An optional tag ID (e.g. 'PRODUCTION' or 'TEST') to assign. 
 *   -ver, --version &lt;ver&gt; Optional. Assign a version to the coding scheme. Default is to assign a timestamp as the version.   
 * 
 * Example: java -Xmx512m -cp lgRuntime.jar
 *  org.lexgrid.extension.loaders.icdgem.admin.LoadICDGEM -in &quot;file:///path/to/file&quot; -type i10to9cm -t Test -ver 1.5
 * </pre>
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 */
@LgAdminFunction
public class LoadICDGEM {

	private ICDGEMLoaderImpl _loader = null;
	private String _formattedTempFile =  null;
	private ICDGEMProperties _props = null;
	
    public static void main(String[] args) {
        try {
//        	String[] blah = {"-in", "file:///C:/ibm/eclipse341b/workspace/lgLoaderExtensions/resources/testData/icd10", "-t", "mytest", "-ver", "1.5"};
//            new LoadICDGEM().run(blah);        	
            new LoadICDGEM().run(args);
        } catch (LBResourceUnavailableException e) {
        	ICDGEMAdminUtils.displayTaggedMessage(e.getMessage());
        } catch (Exception e) {
        	ICDGEMAdminUtils.displayAndLogError("LoadICDGEM: main: REQUEST FAILED.", e);
        }
    }

    public LoadICDGEM() {
        super();
    }
    
    public ICDGEMLoaderImpl getLoader() {
    	return _loader;
    }

    /**
     * Primary entry point for the program.
     * 
     * @throws Exception
     */
    public void run(String[] args) throws Exception {
        synchronized (ResourceManager.instance()) {

            // Parse the command line ...
            CommandLine cl  = null;
            Options options = getCommandOptions();
            URI inFile   = null;
            String codingSchemeVersion  = null;
            String icd10Type = null;
            
            try {
                cl = new BasicParser().parse(options, args);
                if (cl.hasOption("in") && cl.hasOption("type")) {
                	inFile = ICDGEMAdminUtils.string2FileURI(cl.getOptionValue("in"));
                	icd10Type = cl.getOptionValue("type");
	       	         if(cl.hasOption("ver") == true) {
	    	        	 codingSchemeVersion = cl.getOptionValue("ver");
	    	         }
	       	      _props = new ICDGEMProperties(icd10Type, codingSchemeVersion, null);
                	
                } else {
                	ICDGEMAdminUtils.displayTaggedMessage("LoadICDGEM: run: ERROR: parameter \"in\" or \"type\" missing.");
                }
            } catch (ParseException e) {
            	ICDGEMAdminUtils.displayCommandOptions(
                                "LoadICDGEM",
                                options,
                                "\n LoadICDGEM -in \"file:///path/to/gem/source/file\" -type i10to9cm -ver 1.0 -t Test -a" 
                                + ICDGEMAdminUtils.getURIHelp(), e);
                return;
            }

            boolean activate = cl.hasOption("a");
            ICDGEMAdminUtils.displayTaggedMessage("LoadICDGEM: run: loading:  " + inFile.toString());
            
            ICDGEMAdminUtils.displayTaggedMessage(activate ? "LoadICDGEM: run: ACTIVATE ON SUCCESS" : "LoadICDGEM: run: NO ACTIVATION");
            
            // Find the registered extension handling this type of load ...
            LexBIGService lbs = LexBIGServiceImpl.defaultInstance();
            LexBIGServiceManager lbsm = lbs.getServiceManager(null);

            _loader = new ICDGEMLoaderImpl(_props);
            _loader.load( inFile, true, true);         
	        ICDGEMAdminUtils.displayLoaderStatus(_loader);           

            // If specified, set the associated tag on the newly loaded
            // scheme(s) ...
            if (cl.hasOption("t")) {
                String tag = cl.getOptionValue("t");
                AbsoluteCodingSchemeVersionReference[] refs = _loader.getCodingSchemeReferences();
                for (int i = 0; i < refs.length; i++) {
                    AbsoluteCodingSchemeVersionReference ref = refs[i];
                    lbsm.setVersionTag(ref, tag);
                    ICDGEMAdminUtils.displayTaggedMessage("LoadICDGEM: run: tag assigned: " + tag + " version: "
                            + ref.getCodingSchemeVersion());
                }
            }

            // If requested, activate the newly loaded scheme(s) ...
            if (activate) {
                AbsoluteCodingSchemeVersionReference[] refs = _loader.getCodingSchemeReferences();
                for (int i = 0; i < refs.length; i++) {
                    AbsoluteCodingSchemeVersionReference ref = refs[i];
                    lbsm.activateCodingSchemeVersion(ref);
                    ICDGEMAdminUtils.displayTaggedMessage("LoadICDGEM: run: scheme activated: " + ref.getCodingSchemeURN() + " version: "
                            + ref.getCodingSchemeVersion());
                }
            }
        }
    }
    
    /**
     * Return supported command options.
     * 
     * @return org.apache.commons.cli.Options
     */
    private Options getCommandOptions() {
        Options options = new Options();
        Option o;

        o = new Option("in", "in", true, "ICD GEM source file.");
        o.setArgName("in");
        o.setRequired(true);
        options.addOption(o);
        
        o = new Option("type", "type", true, "Type of ICD GEM input data; 'i10to9cm', 'i9to10cm', 'i10to9pcs', 'i9to10pcs' are supported values.");
        o.setArgName("type");
        o.setRequired(true);
        options.addOption(o);        
        
        o = new Option("ver", "ver", true, "ICD GEM version. If not specified today's date/time will be used as a verison number.");
        o.setArgName("ver");
        o.setRequired(false);
        options.addOption(o);        
        
        o = new Option("a", "activate", false, "ActivateScheme on successful load; if unspecified the "
                + "vocabulary is loaded but not activated.");
        o.setRequired(false);
        options.addOption(o);        

        o = new Option("t", "tag", true, "An optional tag ID (e.g. 'PRODUCTION' or 'TEST') to assign.");
        o.setArgName("id");
        o.setRequired(false);
        options.addOption(o);

        return options;
    }

}