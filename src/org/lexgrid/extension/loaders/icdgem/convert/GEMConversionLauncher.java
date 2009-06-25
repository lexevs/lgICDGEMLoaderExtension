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
package org.lexgrid.extension.loaders.icdgem.convert;

import org.LexGrid.messaging.LgMessageDirectorIF;
import org.lexgrid.extension.loaders.icdgem.convert.ICDGEMToLex;
import org.lexgrid.extension.loaders.icdgem.inputFormat.ICDGEMText;
import org.lexgrid.extension.loaders.icdgem.utils.CodingScheme;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

import edu.mayo.informatics.lexgrid.convert.formats.InputFormatInterface;
import edu.mayo.informatics.lexgrid.convert.formats.Option;
import edu.mayo.informatics.lexgrid.convert.formats.OptionHolder;
import edu.mayo.informatics.lexgrid.convert.formats.OutputFormatInterface;
import edu.mayo.informatics.lexgrid.convert.formats.outputFormats.LexGridSQLOut;
import edu.mayo.informatics.lexgrid.convert.utility.URNVersionPair;

/**
 * Tool to take a pair of input and output formats, map them to the appropriate
 * conversion tool, and run the conversion.
 * 
 * Note: this file is a modified version of 
 * 
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 */
public class GEMConversionLauncher {
    /**
     * Execute the conversion
     * 
     * @param inputFormat
     * @param outputFormat
     * @param codingSchemes
     * @param options
     * @param md
     * @return The coding schemes that were converted - this can be useful in
     *         cases where the conversion doesn't take in any coding schemes
     *         (for example, owl). In the case of RRF, this actually returns the
     *         table names that were created.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static CodingScheme startConversion(InputFormatInterface inputFormat,
            OutputFormatInterface outputFormat, String[] codingSchemes, OptionHolder options,
            LgMessageDirectorIF md, ICDGEMProperties props)
            throws Exception {
    	
    	ICDGEMText in = (ICDGEMText) inputFormat;

        if (outputFormat.getDescription().equals(LexGridSQLOut.description)) {
            LexGridSQLOut out = (LexGridSQLOut) outputFormat;
            
            ICDGEMToLex icd10ToLex = new ICDGEMToLex(
            		in.getFileLocation(),
            		out.getServer(),
            		out.getDriver(),
            		out.getUsername(),
            		out.getPassword(),
            		out.getTablePrefix(),
            		props );
//            URNVersionPair[] rv = { new URNVersionPair(textToSQL.getCodingSchemeName(), textToSQL
//                    .getVersion()) };            
//            return URNVersionPair.stringArrayToNullVersionPairArray(new String[] { icd10ToLex.getCodingSchemeName() });
            CodingScheme rv = icd10ToLex.getCodingScheme();
            return rv;

        }
        else {
        	md.error("ExConversionLauncher: startConversion: unsupported output format: " + outputFormat.getDescription());            	
        }
        return null;

    }


    public static URNVersionPair[] finishConversion(InputFormatInterface inputFormat,
            OutputFormatInterface outputFormat, URNVersionPair[] codingSchemes, OptionHolder options,
            LgMessageDirectorIF md) throws Exception {

        // this only applies to EMF loaders at the moment
    	return null;
    }


    public static boolean willBeDoneWithEMF(InputFormatInterface inputFormat, OutputFormatInterface outputFormat,
            OptionHolder options) {
        if (inputFormat == null || outputFormat == null) {
            return false;
        }

        if (options == null) {
            options = new OptionHolder();
        }

        // Has an override flag been set to do a direct conversion with EMF
        // instead?
        Option overrideOption = options.get(Option.getNameForType(Option.DO_WITH_EMF));
        if (overrideOption != null) {
            if (((Boolean) overrideOption.getOptionValue()).booleanValue()) {
                return true;
            }
        }

        // first, figure out if it is a specific (non emf) conversion
        // TODO: palceholder for ICD-10 

        // End of specific direct conversions. Havent found a match yet - can we
        // do it with EMF?
        return true;
    }

    private static String getStringOption(OptionHolder options, int option) throws Exception {
        try {
            return ((String) options.get(Option.getNameForType(option)).getOptionValue());
        } catch (RuntimeException e) {
            throw new Exception(
                    "Converter implementation error - required option is missing - needs a string option of "
                            + Option.getNameForType(option));
        }
    }

    private static boolean getBooleanOption(OptionHolder options, int option) throws Exception {
        try {
            return ((Boolean) options.get(Option.getNameForType(option)).getOptionValue()).booleanValue();
        } catch (RuntimeException e) {
            throw new Exception(
                    "Converter implementation error - required option is missing - needs a string option of "
                            + Option.getNameForType(option));
        }
    }

}