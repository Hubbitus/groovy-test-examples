import groovy.time.*
import javax.xml.datatype.*
import javax.xml.datatype.XMLGregorianCalendar
import java.sql.Timestamp

//Date d = new Date().parse('yyyy-MM-dd', '2012-10-27');

//Date d1 = new Date().parse('yyyy-MM-dd', '2012-10-01');
//Date d2 = new Date().parse('yyyy-MM-dd', '2012-11-30');

Date d1 = new Date().parse('yyyy-MM-dd', '2012-10-25');
Date d2 = new Date().parse('yyyy-MM-dd', '2012-11-05');

// First day of current month
c = new GregorianCalendar();
c.set(Calendar.DATE, 1)

GregorianCalendar g1 = new GregorianCalendar(2012, Calendar.OCTOBER, 1);
GregorianCalendar g2 = new GregorianCalendar(2012, Calendar.NOVEMBER, 3);

//g1.getActualMaximum(Calendar.DATE) // Last month day

//Date d = c.getTime()
//use(TimeCategory){
//    List l = (0..<c.getActualMaximum(Calendar.DATE)).collect{
//        [(c.getTime()+it.days): 0L]
//    }
//}

//use(groovy.time.TimeCategory){
//((d2 - d1) + 1).days // Diff in days
//}

//(g2 - g1 + 1).dump()


// Current day
//d.date

// Prev Sunday (full backup)
//d.date - d[Calendar.DAY_OF_WEEK] + 1


//d.date
//DatatypeFactory df = DatatypeFactory.newInstance();
//XMLGregorianCalendar d1 = df.newXMLGregorianCalendar();

use(groovy.time.TimeCategory){
//    Map<Date, Long> dayPlans = ( 0..<( (d2 - d1).days + 1) ).collectEntries{e->
//        [(new Timestamp((g1.getTime()+e.days).getTime())): 0L];
//    }
//    (d2 - d1)
    (g1..g2).groupBy{e->
        e.get(Calendar.MONTH)
    }.each{month->
        Map<Date, Long> dayPlans = month.value.collectEntries{day-> [(day.getTime()): 0L] }
//println "<=Month: ${month.key}; Days: ${month.value}; dayPlans=${dayPlans}; dayPlans.submap=${dayPlans.subMap( g1.getTime()..g2.getTime() ).findAll{ null != it.value }}>"
//println "<=Days: ${month.value.subMap( g1.getTime()..g2.getTime() )}; month: ${month.key}=>"

    }

/*
GregorianCalendar g11 = g1.clone();
g11.set(Calendar.DATE, g1.getActualMaximum(Calendar.DATE))
    (g1.getTime()..g1.getTime() + g1.getActualMaximum(Calendar.DATE) - 1).collectEntries{d-> [ (d): 0L ]}
*/



}