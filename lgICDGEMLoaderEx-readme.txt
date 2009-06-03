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


 
