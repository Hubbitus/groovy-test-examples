

//Date d = new Date();
//Date d = Date.parse("yyyy-MM-dd", "2012-01-22")

def test(Date d){
def c= new GregorianCalendar();
c.firstDayOfWeek = Calendar.MONDAY
c.setTime(d);
[
'd':d
,'d.day': d.day
,'d.date': d.date
,'d.calendarDate': d.calendarDate.class
,'d.calendarDate.dayOfWeek': d.calendarDate.dayOfWeek
,'d[Calendar.DAY_OF_WEEK]': d[Calendar.DAY_OF_WEEK]
,'c[Calendar.DAY_OF_WEEK]': c[Calendar.DAY_OF_WEEK]
,'res': "${d.date - d[Calendar.DAY_OF_WEEK] + 1}"
,'res1': d.date - ( 7 - d.calendarDate.dayOfWeek)
//,'locale': d.getDateString()
]
}

//println test(new Date());
println test(Date.parse("yyyy-MM-dd", "2012-01-22"));
println test(Date.parse("yyyy-MM-dd", "2012-01-23"));
println test(Date.parse("yyyy-MM-dd", "2012-01-24"));
println test(Date.parse("yyyy-MM-dd", "2012-01-25"));
println test(Date.parse("yyyy-MM-dd", "2012-01-26"));
println test(Date.parse("yyyy-MM-dd", "2012-01-27"));
println test(Date.parse("yyyy-MM-dd", "2012-01-28"));
println test(Date.parse("yyyy-MM-dd", "2012-01-29"));
println test(Date.parse("yyyy-MM-dd", "2012-01-30"));
println test(Date.parse("yyyy-MM-dd", "2013-01-29"));