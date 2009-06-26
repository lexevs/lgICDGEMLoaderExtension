----------------------------------------------------------------------------------
Instructions to deploy ICD GEM Loader Extension into LexBIG environment and run the test cases.
----------------------------------------------------------------------------------

1) Make sure runtime/lbRuntime.jar matches with the lbRuntime.jar of the lexBIG instance on which the GEM Loader Extensions are being run. 

2) Run build.xml (default target) located in root directory 

	- Source files get compiled and wrapped into lgICDGEMLoaderEx.jar under "dist" folder
	
	- Source files get wrapped into lgICDGEMLoaderEx-src.jar under "dist" folder.
	
	- Intermediate class files get deleted
	
	- Test Suite and Test Cases are compiled and wrapped into lgICDGEMLoaderEx-test.jar under "dist" folder.
	
	- lgICDGEMLoaderEx.jar, lgICDGEMLoaderEx-test.jar, lgICDGEMLoaderEx-GenerateReport.xml, lgICDGEMLoaderEx-TestRunner.sh, 
	lgICDGEMLoaderEx-TestRunner.bat, required external jars and source ontologies are bundled into "deployGEM.zip" file.
	  
3) Extract the contents of "deployGEM.zip" into the root of lexBIG installation (ex: C:/Program Files/LexGrid/LexBIG/5.0.0a/)      

	- The Loader Extensions are integrated with your lexBIG instance. 
	
4) Go to "test" folder in your lexBIG instance and run 
	
	- lgICDGEMLoaderEx-TestRunner.bat file in windows environment or 
	
	- lgICDGEMLoaderEx-TestRunner.sh file in unix/Linux environment.

5) Test Results can be viewed under "test/lgICDGEMLoaderEx-reports" folder.


NOTE: The GEM Loader Extensions are compiled using jdk1.5. Make sure your JAVA_HOME and PATH are set to jdk1.5


----------------------------------------------------------------------------------
Usage and other notes.
----------------------------------------------------------------------------------
1. The ICD GEM loader can load 4 different mappings:
	- ICD 9 to ICD 10 CM
	- ICD 10 to ICD 9 CM
	- ICD 9 to ICD 10 PCS
	- ICD 10 to ICD 9 PCS

2. Included with the program (in the testData/icdgem folder when deployed) are 8 files in
two groups of 4 files each.  In one group, the files are complete.  The second group (those
prefixed with 'small_' are small versions of the files used for running the JUNIT test
so that the tests run quicker.

	- 2009_I9gem.txt  		// ICD 9 to 10 CM mapping data
	- small_2009_I9gem.txt	// subset of above file
	
	- 2009_I10gem.txt 		// ICD 10 to 9 CM
	- small_2009_I10gem.txt // subset of above file
	
	- gem_i9pcs.txt   		// ICD 9 to 10 PCS data
	- small_gem_i9pcs.txt	// subset of above file
	
	- gem_pcsi9.txt   		// ICD 10 to 9 PCS
	- small_gem_pcsi9.txt   // subset of above file


3. The program loads one mapping at a time. You, Dear user, must specify what mapping
will be loaded via the program parameters.

4. Program parameters:
	-in		// URI specifying the fully qualified file name of the input file. Required. 
	-type	// Type of GEM data contained in the input file.  
			// Valid values are 'i10to9cm', 'i9to10cm', 'i10to9pcs', or 'i9to10pcs'. Required.
	-ver	// The coding scheme version. Optional.  If it is not used a date/time stamp will be used.
	-t		// An optional tag ID (e.g. 'PRODUCTION' or 'TEST') to assign.
	-a		// Activate. Set this option if you want the coding scheme activated after it is loaded. Optional.

5. Example program call.  Say you want to load the ICD 10 to ICD 9 CM mapping from the
file provided in test data.  Further, suppose your LexBIG install root is C:\mayo\lexbig\test501, and
you want to provide a version of v2009, a tag of 'test', and you want the coding scheme 
activated when done.  Your program call would look like:

	LoadICDGEM.bat -in "file:///C:/mayo/lexbig/test501/test/resources/testData/icdgem/2009_I10gem.txt" -type i10to9cm -ver v2009 -t test -a

6. Design note:  The GEM files do not contain description information about the codes contained therein.
So the GEM loader will add the necessary information to the coding scheme to reference the concept
descriptions from the appropriate terminology if they have already been loaded in LexBIG.  If that data
has not been previously loaded, that is OK.  It just means the concept descriptions will not appear when you 
view the coding scheme in the LexBIG GUI.   


7. Final note: The General Equivalence Mapping are provided by The Centers for Medicare & Medicaid Services (CMS)
located at: http://www.cms.hhs.gov
PCS GEM files can be found at:
	- go to http://www.cms.hhs.gov/ICD10/01m_2009_ICD10PCS.asp
	- click link "2009 Mapping - "ICD-10-PCS to ICD-9-CM"  and "ICD-9-CM to ICD-10-PCS"; and User Guide, Reimbursement Guide, Diagnosis, and Procedures [ZIP, 1.2MB]"
CM files:
	- go to http://www.cms.hhs.gov/ICD10/02m_2009_ICD_10_CM.asp
	- click link "2009 Diagnosis Code Set General Equivalence Mappings [ZIP 1.1MB]"
	
----------------------------------------------------------------------------------
Known Issues and limitations.
----------------------------------------------------------------------------------
1. If using the LexBIG GUI, it is not practical to resolve the entire coding scheme as a graph. 
This is due to the number of entities immediately present beneath the root node. However, It is 
possible to view a portion of the graph by indicating a focus code.
 
2. If using the LexBIG GUI, there is an issue prevents the focus code from resolving its concept description. 
In the case if the GEM loader, that mean the concept on the left side of a 'MapsTo' association will not have concept description information 
even if that information should be available (by having the loaded ICD data), whereas the concept on the right
side of the association will have the description info. NCI Gforge LexEVS bug 22036. 
   	  
