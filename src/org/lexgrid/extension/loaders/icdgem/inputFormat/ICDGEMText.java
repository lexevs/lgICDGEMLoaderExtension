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
package org.lexgrid.extension.loaders.icdgem.inputFormat;

import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMConstants;
import org.lexgrid.extension.loaders.icdgem.utils.ICDGEMProperties;

import edu.mayo.informatics.lexgrid.convert.exceptions.ConnectionFailure;
import edu.mayo.informatics.lexgrid.convert.formats.InputFormatInterface;
import edu.mayo.informatics.lexgrid.convert.formats.Option;
import edu.mayo.informatics.lexgrid.convert.formats.baseFormats.FileBase;
import edu.mayo.informatics.lexgrid.convert.formats.outputFormats.LexGridLDAPOut;
import edu.mayo.informatics.lexgrid.convert.formats.outputFormats.LexGridSQLOut;
import edu.mayo.informatics.lexgrid.convert.formats.outputFormats.LexGridXMLOut;

/**
 * @author <A HREF="mailto:turk.michael@mayo.edu">Michael Turk</A>
 * @version subversion $Revision: 8819 $ checked in on $Date: 2008-06-13
 *          16:15:16 +0000 (Fri, 13 Jun 2008) $
 */
public class ICDGEMText extends FileBase implements InputFormatInterface {
    
	ICDGEMProperties props;
	
    public ICDGEMText(String inputFile, ICDGEMProperties props) {
        this.fileLocation = inputFile;
        this.props = props;
    }

    public ICDGEMText() {

    }
    
    public String testConnection() throws ConnectionFailure {
        StringBuffer rv = new StringBuffer();
        FileBase fb = new FileBase();
        fb.setFileLocation(this.fileLocation);
        rv.append(fb.testConnection());
        return rv.toString();
    }
    
    public String[] getSupportedOutputFormats() {
        return new String[] { LexGridSQLOut.description, LexGridLDAPOut.description, LexGridXMLOut.description };
    }

    public Option[] getOptions() {
        return new Option[] {};
    }

    public String[] getAvailableTerminologies() {
        return null;
    }

	public String getConnectionSummary() {
		return props.getLoaderTextDescrption();
	}

	public String getDescription() {
		return getConnectionSummary(props.getLoaderTextDescrption());
	}
}