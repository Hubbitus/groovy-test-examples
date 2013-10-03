// 2013-08-20 20:01:46,487 INFO  [OperationPerfLogger] Latency_from_ukhtatgmain.prosoft: СВВ на 1000 сообщений, всего 543000 [кол-во: ср.время в пакете(min; max)/ср.время общее(min; max)] (мс):
//'latency'[1000: 7645369(7627053;7668862)/5826065(2140534;8176892)]

//def matcher = new File('/home/pasha/temp/imus-bus-eitp.log').text =~ /(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}) INFO  \[OperationPerfLogger\] Latency_from_([\w\.]+): СВВ на (\d+) сообщений, всего (\d+) \[кол-во: ср\.время в пакете\(min; max\)\/ср\.время общее\(min; max\)\] \(мс\):\s+'latency'\[(\d+): (\d+)\((\d+);(\d+)\)\/(\d+)\((\d+);(\d+)\)\]/
//def matcher = 'ssh mrg.rgc cat /cygdrive/D/IMUS/esb_eitp/imus-esb/log/imus-bus-eitp.log'.execute().pipeTo('enconv'.execute()).text =~ /(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}) INFO  \[OperationPerfLogger\] Latency_from_([\w\.]+): СВВ на (\d+) сообщений, всего (\d+) \[кол-во: ср\.время в пакете\(min; max\)\/ср\.время общее\(min; max\)\] \(мс\):\s+'latency'\[(\d+): (\d+)\((\d+);(\d+)\)\/(\d+)\((\d+);(\d+)\)\]/

//def matcher = '''ssh mrg.rgc "(grep -F '' -A 1000000000 /cygdrive/D/IMUS/esb_eitp/imus-esb/log/imus-bus-eitp.log || cat /cygdrive/D/IMUS/esb_eitp/imus-esb/log/imus-bus-eitp.log) | grep -F -A1 Latency_from_ | grep -v -F -- --" '''.execute().pipeTo('enconv'.execute()).text =~ /(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}) INFO  \[OperationPerfLogger\] Latency_from_([\w\.]+): СВВ на (\d+) сообщений, всего (\d+) \[кол-во: ср\.время в пакете\(min; max\)\/ср\.время общее\(min; max\)\] \(мс\):\s+'latency'\[(\d+): (\d+)\((\d+);(\d+)\)\/(\d+)\((\d+);(\d+)\)\]/

/*
def proc = '''ssh mrg.rgc (grep -F '' -A 1000000000 /cygdrive/D/IMUS/esb_eitp/imus-esb/log/imus-bus-eitp.log || cat /cygdrive/D/IMUS/esb_eitp/imus-esb/log/imus-bus-eitp.log) '''.execute()

println "ERR: ${proc.err.text}"
println "OUT: ${proc.text}"
*/

//println 'ssh mrg.rgc cat /cygdrive/D/IMUS/esb_eitp/imus-esb/log/imus-bus-eitp.log'.execute().pipeTo('enconv'.execute()).text
//println 'ls'.execute().pipeTo('enconv'.execute ()).text
/*
matcher.each{
    println "Date: ${it[1]}, System: ${it[2]}, Messages in pack: ${it[3]}, Total messages: ${it[4]}, Messages in packAgain?: ${it[5]}, Avg time in pack: ${it[6]}, Min time in pack: ${it[7]}, Max time in pack: ${it[8]}, Avg total time: ${it[9]}, Min total time: ${it[10]}, Max total time: ${it[11]}"
}
*/

Timestamp timestamp = Timestamp.valueOf("2007-09-23 10:10:10.0");