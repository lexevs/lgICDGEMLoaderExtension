 # Runs the test suite by invoking the Ant launcher.

java -Xmx1000m -cp ./lbTest.jar:./lgICDGEMLoaderEx-test.jar:../runtime/lgICDGEMLoaderEx.jar:../runtime/lbPatch.jar:../runtime/lbRuntime.jar:./lgICDGEMLoaderEx-extlib/ant/ant.jar:./lgICDGEMLoaderEx-extlib/ant/ant-junit.jar:./lgICDGEMLoaderEx-extlib/ant/ant-trax.jar:./lgICDGEMLoaderEx-extlib/ant/ant-launcher.jar:./lgICDGEMLoaderEx-extlib/junit/junit.jar TestRunner $@
