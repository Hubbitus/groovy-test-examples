@Grab('net.sourceforge.nekohtml:nekohtml:1.9.15')
def parser = new org.cyberneko.html.parsers.SAXParser()
parser.setFeature('http://xml.org/sax/features/namespaces', false)
def page = new XmlParser(parser).parse('http://basicdata.ru/download/fias/')
def data = page.depthFirst().A.'@href'.grep{ it != null && it =~ /_(table|index|data)\.sql\.bz2/ }
//data.each { println it }
println data