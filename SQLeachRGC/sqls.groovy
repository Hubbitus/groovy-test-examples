#!/bin/env groovy

//import groovy.xml.MarkupBuilder

@Grab(group='net.sourceforge.jtds', module='jtds', version='1.2')

import groovy.sql.Sql
import net.sourceforge.jtds.jdbc.Driver
import groovy.util.Node

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
//@ Grab(group='ojdbc', module='ojdbc', version='14')
@Grab(group='postgresql', module='postgresql', version='9.1-901-1.jdbc4')

//this.class.classLoader.rootLoader.addURL(new URL("file:///home/pasha/temp/SQLeachRGC/jtds-1.2.4.jar"));
// http://groovy.codehaus.org/Class+Loading
//this.class.classLoader.addURL(new URL("file:///home/pasha/temp/SQLeachRGC/jtds-1.2.4.jar"));
//println Class.forName("net.sourceforge.jtds.jdbc.Driver", true, this.class.classLoader).newInstance()
//def c = new net.sourceforge.jtds.jdbc.Driver();
//println c;

//println this.class.classLoader.rootLoader

///	if (!args.length){
//	args[0] = 'SQLAliases23.xml';
//	}

def drivers = new XmlParser().parseText(new File('SQLDrivers.xml').text);
def objects = new XmlParser().parseText(new File('SQLAliases23.xml').text);
//def records = new XmlParser().parseText('-' == args[0] ? System.in.text : new File(args[0]).text);

def getIP(Node obj){
	String ip;
	try{ // MSSQL
		ip = (obj.url.text() =~ /sqlserver:\/\/([^:\/]+)/)[0][1];
	}
	catch(java.lang.IndexOutOfBoundsException ignore){ //Oracle
		ip = (obj.url.text() =~ /thin:@([^:\/]+)/)[0][1];
	}
	return ip;
}

class ActiveMQVersionCheck implements Runnable {
	String version;
	String name;
	String ip;
	int runNo;

	static synchronized out = System.out.&println;

	void run() {
		try{
//			println "ip=$ip; URL: http://${ip}:8161/admin/";
			version = ( ("http://${ip}:8161/admin/".toURL().text =~ /<td>Version<\/td>\s+<td><b>([^<]+)<\/b><\/td>/ )[0][1] );
		}
		catch(java.net.ConnectException ne){
			version = 'Network exception happened: ' + ne
//			System.err.println(version); System.err.flush();
		}
		catch(java.lang.IndexOutOfBoundsException ignore){
			version = 'ActiveMQ version is too old'
//			System.err.println(version); System.err.flush();
		}

		out("DEBUG: ($runNo)$name: version: $version");
	}
}

class SQLExecute implements Runnable {
	Node drivers;
	Node obj;
	String sql;
	def rows;
	int runNo;

	static synchronized out = System.out.&println;

	static String getDriverStr(Node drivers, Node obj){
		drivers.Bean.find{ it.identifier.string.text() == obj.driverIdentifier.string.text() }.driverClassName.text()
	}

	String getName(){
		obj.name.text();
	}

	void run() {
		out("Start $runNo");

		try{
			Sql sqlConnect = Sql.newInstance(obj.url.text(), obj.userName.text(), obj.password.text(), getDriverStr(drivers, obj));
			//	println sql.rows("SELECT * FROM ext_system WHERE  system_code LIKE '%.imus' AND system_code NOT IN ('mrgmain.imus', 'antmain.imus')").join('->\n')
//			rows = sqlConnect.rows(sql)
			rows = sqlConnect.execute(sql)
		}
		catch(java.sql.SQLException e){
			rows = ['Exception happened during connect or query: ' + e];
		}
		catch(Exception all){
			rows = ['Unknown Exception happened during connect or query: ' + all];
		}

		out("DEBUG: ($runNo)${obj.name.text()}: result: \n${rows.join('\n')}");
	}
}

// How many threads to kick off
// http://stackoverflow.com/questions/2720325/groovy-thread-for-urls
final int nThreads = 50
def pool = Executors.newFixedThreadPool( nThreads )
List results = [];

SQL = """
SELECT *
FROM free_queue
WHERE
	NOT (free_queue_id = 1 AND grafik_num = 1 AND free_queue_num = 1 AND comm = 'Гр.№1 - 1 Очередь')
	 AND
	NOT (free_queue_id = 2 AND grafik_num = 1 AND free_queue_num = 2 AND comm = 'Гр.№1 - 2 Очередь')
	 AND
	NOT (free_queue_id = 3 AND grafik_num = 1 AND free_queue_num = 3 AND comm = 'Гр.№1 - 3 Очередь')
	 AND
	NOT (free_queue_id = 4 AND grafik_num = 1 AND free_queue_num = 4 AND comm = 'Гр.№1 - 4 Очередь')
	 AND
	NOT (free_queue_id = 5 AND grafik_num = 2 AND free_queue_num = 1 AND comm = 'Гр.№2 - 1 Очередь, 20%')
	 AND
	NOT (free_queue_id = 6 AND grafik_num = 2 AND free_queue_num = 2 AND comm = 'Гр.№2 - 2 Очередь, 50%')
	 AND
	NOT (free_queue_id = 7 AND grafik_num = 2 AND free_queue_num = 3 AND comm = 'Гр.№2 - 3 Очередь, 80%')
""";

SQL = """
SELECT COUNT(*) dict
FROM free_queue fq
	JOIN (
		SELECT 1 AS free_queue_id,1 AS grafik_num,1 AS free_queue_num,'Гр.№1 - 1 Очередь' AS comm
		UNION ALL
		SELECT 2 AS free_queue_id,1 AS grafik_num,2 AS free_queue_num,'Гр.№1 - 2 Очередь' AS comm
		UNION ALL
		SELECT 3 AS free_queue_id,1 AS grafik_num,3 AS free_queue_num,'Гр.№1 - 3 Очередь' AS comm
		UNION ALL
		SELECT 4 AS free_queue_id,1 AS grafik_num,4 AS free_queue_num,'Гр.№1 - 4 Очередь' AS comm
		UNION ALL
		SELECT 5 AS free_queue_id,2 AS grafik_num,1 AS free_queue_num,'Гр.№2 - 1 Очередь, 20%' AS comm
		UNION ALL
		SELECT 6 AS free_queue_id,2 AS grafik_num,2 AS free_queue_num,'Гр.№2 - 2 Очередь, 50%' AS comm
		UNION ALL
		SELECT 7 AS free_queue_id,2 AS grafik_num,3 AS free_queue_num,'Гр.№2 - 3 Очередь, 80%' AS comm
	) dict
	ON (dict.free_queue_id = fq.free_queue_id AND dict.grafik_num = fq.grafik_num AND dict.free_queue_num = fq.free_queue_num AND dict.comm = fq.comm)
"""

//println objects.dump()
objects.Bean.eachWithIndex{obj, idx->
//objects.Bean[0..2].eachWithIndex{obj, idx->
//	println "${idx + 1}/${objects.Bean.size()} :: ${obj.name.text()} : ${obj.url.text()} : Driver(${obj.driverIdentifier.string.text()}) - ${ getDriverStr(drivers, obj) }"
	println "${idx + 1}/${objects.Bean.size()} :: ${obj.name.text()} : ${obj.url.text()} : Driver(${obj.driverIdentifier.string.text()}) - ${ SQLExecute.getDriverStr(drivers, obj) }"

/*
	try{
		Sql sql = Sql.newInstance(obj.url.text(), obj.userName.text(), obj.password.text(), getDriverStr(drivers, obj));
	//	println sql.rows("SELECT * FROM ext_system WHERE  system_code LIKE '%.imus' AND system_code NOT IN ('mrgmain.imus', 'antmain.imus')").join('->\n')
		println sql.rows("SELECT run_date, status, msg FROM up_db_script WHERE change_id = 598").join('->\n')
	}
	catch(java.sql.SQLException e){
		System.err.println('Exception happened during connect or query: ' + e);
		System.err.flush();
	}
*/

/*
	ActiveMQVersionCheck u = new ActiveMQVersionCheck( ip:getIP(obj), name: obj.name.text(), runNo: idx + 1);
	pool.submit u;
	results << u;
*/

	SQLExecute r = new SQLExecute( drivers: drivers, obj: obj, runNo: idx + 1, sql: SQL);
	pool.submit r;
	results << r;
}

// Wait for the pool close when all threads are completed
pool.shutdown();
pool.awaitTermination( 100, TimeUnit.SECONDS );

// Print our results
println "RESULTS:"
results.each{
//	println "$it.name ($it.runNo) : $it.version"
//	if (it.rows.size() > 0)
	if (it.rows[0].dict < 7)
	println "$it.name ($it.runNo):\n ${it.rows.join('\n')}"
}
