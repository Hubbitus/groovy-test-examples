@Grab(group='xmlunit', module='xmlunit', version='1.0')
import groovy.xml.MarkupBuilder
import org.custommonkey.xmlunit.*

class XmlExamples {
  static def CAR_RECORDS = '''
    <records>
      <car name='HSV Maloo' make='Holden' year='2006'>
        <country>Australia</country>
        <record type='speed'>Production Pickup Truck with speed of 271kph</record>
      </car>
      <car name='P50' make='Peel' year='1962'>
        <country>Isle of Man</country>
        <record type='size'>Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record>
      </car>
      <car name='Royale' make='Bugatti' year='1931'>
        <country>France</country>
        <record type='price'>Most Valuable Car at $15 million</record>
      </car>
    </records>
  '''
}

def tagName = 'someTag'

def writer = new StringWriter()
def xml = new MarkupBuilder(writer)
xml.root() {
    columns{
        column(Name: 'one', Type:'Type one')
        column(Name: 'two', Type:'Type two')
    }
    records(amount: 2){
        record{
            period( '08.04.2013 0:00:00' )
            "$tagName"('value')
//            <regular>22 848,391</regular>
//            <regInd Null="true"></regInd>
//            <indInd>35 402,566</indInd>
//            <independent>35 402,566</independent>
//            <total>58 250,957</total>
        }
        record{
            period( '09.04.2013 0:00:00' )
//            <regular>22 847,344</regular>
//            <regInd Null="true"></regInd>
//            <indInd>35 402,566</indInd>
//            <independent>35 402,566</independent>
//            <total>58 249,91</total>
        }
    }
}

writer