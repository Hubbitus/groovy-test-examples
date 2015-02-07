GregorianCalendar date = new GregorianCalendar(2014, 10 - 1, 1, 0, 0, 0)
println TimeZone.getTimeZone('GMT+5').rawOffset / 60 / 60 / 1000

date.setTimeZone(TimeZone.getTimeZone('GMT+5')); // Orenburg
date.get(Calendar.HOUR_OF_DAY); // Get call REQUIRED http://stackoverflow.com/questions/12137428/java-gregoriancalendar-change-timezone

date.set(Calendar.DATE, 1);
date.set(Calendar.HOUR_OF_DAY, 0);
//time.add(Calendar.HOUR, 3 / * Moscow * / - UTCoffset);
//println date.time
println String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS %<tZ', date)
println date.time
println date.time.toTimestamp()
println '---'

date.setTimeZone(TimeZone.getTimeZone('GMT+3')); // Moscow
println String.format('%tY-%<tm-%<td %<tH:%<tM:%<tS %<tZ', date)
println date.time
println date.time.toTimestamp()