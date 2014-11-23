#!/bin/env groovy

@Grab(group='net.sourceforge.jtds', module='jtds', version='1.2')
@Grab(group='ojdbc', module='ojdbc6', version='11.2.0.3.0')
@Grab(group='postgresql', module='postgresql', version='9.1-901-1.jdbc4')

@Grab(group='com.thoughtworks.xstream', module='xstream', version='1.4.6')
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.core.util.QuickWriter
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter
import com.thoughtworks.xstream.io.xml.XppDriver
import common.runners.SQLExecute
import common.sql.SqlCode
import diff.xstream.ConfigObjectNamedConverter

XStream xstream = new XStream(
	new XppDriver(){ // For console run
//	new KXml2Driver() { // For IDEA
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new PrettyPrintWriter(out) {
				protected void writeText(QuickWriter writer, String text) {
					if(text ==~ /(?s).*[<>&].*/) {
						writer.write('<![CDATA[');
						writer.write(text);
						writer.write(']]>');
					} else {
						writer.write(text);
					}
				}
			};
		}
	}
);
xstream.setMode(XStream.NO_REFERENCES); // http://xstream.codehaus.org/graphs.html
xstream.autodetectAnnotations(true)
xstream.classLoader = getClass().classLoader;

// ConfigObject convert, break circles
xstream.omitField(MapWithDefault, 'initClosure')
xstream.omitField(Closure, 'delegate') // ConfigObject
xstream.omitField(Closure, 'owner') // ConfigObject
xstream.omitField(Closure, 'binding') // ConfigObject
xstream.omitField(ConfigSlurper, 'classLoader') // ConfigObject
xstream.omitField(Script, 'binding') // ConfigObject
xstream.omitField(ConfigObject, 'unserializable-parents') // ConfigObject

xstream.omitField(SQLExecute, 'res')
xstream.omitField(SQLExecute, 'definedIn') // @Lazy inner implementation
xstream.omitField(List, 'definedIn')
xstream.omitField(List, 'definedIn')
xstream.omitField(SqlCode, 'definedIn')

ConfigObject config = new ConfigSlurper().parse(new File('TestConfig.groovy').text).config;

xstream.registerConverter(new ConfigObjectNamedConverter(config));

Map m = [:].withDefault {[:]}
m[config.checks.eitpzone.supply] = 'test value'

File file = new File('test.xml');
if(file.exists()) file.delete();
file << xstream.toXML(m)

xstream.fromXML(new File('test.xml').toURL().openConnection().getInputStream());