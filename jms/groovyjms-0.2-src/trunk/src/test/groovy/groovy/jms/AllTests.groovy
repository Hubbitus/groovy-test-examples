package groovy.jms

import junit.framework.TestSuite
import junit.framework.Test

class AllTests  extends TestSuite {
     public static Test suite() throws Exception {
         TestSuite suite = new TestSuite();
         GroovyTestSuite gsuite = new GroovyTestSuite();
         suite.addTestSuite(JMSImplTest.class);
         suite.addTestSuite(JMSCategoryTest.class);
         suite.addTestSuite(JMSTest.class);
         suite.addTestSuite(JMSPoolTest.class);
         return suite;
     }
}