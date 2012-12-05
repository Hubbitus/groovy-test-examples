#!/bin/env groovy

//import groovy.xml.MarkupBuilder

// http://groovy.codehaus.org/Groovy+Console
import groovy.ui.Console

//@ Grab(group='net.sourceforge.jtds', module='jtds', version='1.2')
////@ Grab(group='ojdbc', module='ojdbc', version='14')
//@ Grab(group='postgresql', module='postgresql', version='9.1-901-1.jdbc4')

import groovy.sql.Sql
import net.sourceforge.jtds.jdbc.Driver
import groovy.util.Node

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

class CommonRun{
	public static final boolean DEBUG = false;

	def res;
	Node obj;
	String error;
	Exception exception;
	int runNo;

	static synchronized out = System.out.&println;

	static class ExecuteResult{
		int code;
		List<String> out = [];
		String err;

		public String toString(){
			"""Execute process result: Code - $code;${err ? "\nError: ${err}" : ''}
Out:${out.size() > 1 ? '\n' : ''}${out.join('\n')}"""
		}
	}

	String getName(){
		obj.name.text();
	}

	String getIp(){
		String ip;
		try{ // MSSQL
			ip = (obj.url.text() =~ /sqlserver:\/\/([^:\/]+)/)[0][1];
		}
		catch(java.lang.IndexOutOfBoundsException ignore){ //Oracle
			ip = (obj.url.text() =~ /thin:@([^:\/]+)/)[0][1];
		}
		return ip;
	}

	// http://groovy.329449.n5.nabble.com/printing-the-output-from-quot-some-command-quot-execute-td330528.html
	static ExecuteResult executeProc(cmd) {
		List out = [];

		if (DEBUG){
			println "Will be executed next command: $cmd";
		}
		def proc;
		if (System.properties['os.name'].toLowerCase().contains('windows')) {// To allow run "internal" commands like dir
			proc = "cmd /c ${cmd}".execute();
		} else {
			proc = cmd.execute();
		}
		proc.in.eachLine {line ->
			if (DEBUG)
				println line;
			out << line;
		}
		proc.waitFor();

		String err = proc.err.text; // Fetch able only once!
		if (err)
			println ('Errors happened: ' + err);

		return new ExecuteResult([ code: proc.exitValue(), out: out, err: err ]);
	}
}

class ActiveMQVersionCheck extends CommonRun implements Runnable {
	void run() {
		try{
//			println "ip=$ip; URL: http://${ip}:8161/admin/";
			res = ( ("http://${ip}:8161/admin/".toURL().text =~ /<td>Version<\/td>\s+<td><b>([^<]+)<\/b><\/td>/ )[0][1] );
		}
		catch(java.net.ConnectException ne){
			res = 'Network exception happened: ' + ne
//			System.err.println(version); System.err.flush();
		}
		catch(java.lang.IndexOutOfBoundsException ignore){
			res = 'ActiveMQ version is too old'
		}

		out("DEBUG: ($runNo)$name: version: $res");
	}
}

class SQLExecute extends CommonRun implements Runnable {
	Node drivers;
	String sql;
	List rows;
	int runNo;

	static synchronized out = System.out.&println;

	static String getDriverStr(Node drivers, Node obj){
		drivers.Bean.find{ it.identifier.string.text() == obj.driverIdentifier.string.text() }.driverClassName.text()
	}

	void run() {
		out("Start $runNo");

		try{
			Sql sqlConnect = Sql.newInstance(obj.url.text(), obj.userName.text(), obj.password.text(), getDriverStr(drivers, obj));
			res = sqlConnect.rows(sql)
//			rows = sqlConnect.execute(sql) // DML
		}
		catch(java.sql.SQLException e){
			error = 'Exception happened during connect or query: ' + e;
		}
		catch(Exception all){
			error = 'Unknown Exception happened during connect or query: ' + all;
		}

		out("DEBUG: ($runNo)${name}: result: \n${res.join('\n')}");
	}
}

class RsyncFileGrep extends CommonRun implements Runnable {
	String remotePath;
	String grep;

	void run(){
		try{
			// http://gradle.1045684.n5.nabble.com/quot-su-USERNAME-bash-c-quot-from-Gradle-Groovy-execute-results-in-error-bash-c-line-0-unexpected-EO-td4544910.html
			res = executeProc(['bash', '-c', "TMP=\$(mktemp) ; rsync rsync://${ip}/${remotePath} \$TMP ; grep -a '${grep}' \$TMP; rm -f \$TMP"]);
		}
		catch (Exception e){
//			println e.stackTrace;
			error = 'Error happened: ' + e;
			exception = e;
		}
	}
}

// How many threads to kick off
// http://stackoverflow.com/questions/2720325/groovy-thread-for-urls
final int nThreads = 50
def pool = Executors.newFixedThreadPool( nThreads )
List results = [];

SQL = """
SELECT dict.*, 'dict_missing'
FROM free_queue fq
	RIGHT JOIN (
		SELECT 1 AS free_queue_id,1 AS grafik_num,1 AS free_queue_num,'Гр.№1 - 1 Очередь' AS comm FROM Dual
		UNION ALL
		SELECT 2 AS free_queue_id,1 AS grafik_num,2 AS free_queue_num,'Гр.№1 - 2 Очередь' AS comm FROM Dual
		UNION ALL
		SELECT 3 AS free_queue_id,1 AS grafik_num,3 AS free_queue_num,'Гр.№1 - 3 Очередь' AS comm FROM Dual
		UNION ALL
		SELECT 4 AS free_queue_id,1 AS grafik_num,4 AS free_queue_num,'Гр.№1 - 4 Очередь' AS comm FROM Dual
		UNION ALL
		SELECT 5 AS free_queue_id,2 AS grafik_num,1 AS free_queue_num,'Гр.№2 - 1 Очередь, 20%' AS comm FROM Dual
		UNION ALL
		SELECT 6 AS free_queue_id,2 AS grafik_num,2 AS free_queue_num,'Гр.№2 - 2 Очередь, 50%' AS comm FROM Dual
		UNION ALL
		SELECT 7 AS free_queue_id,2 AS grafik_num,3 AS free_queue_num,'Гр.№2 - 3 Очередь, 80%' AS comm FROM Dual
	) dict
	ON (dict.free_queue_id = fq.free_queue_id AND dict.grafik_num = fq.grafik_num AND dict.free_queue_num = fq.free_queue_num AND dict.comm = fq.comm)
WHERE fq.free_queue_id IS NULL
UNION ALL
SELECT fq.*, 'local items (NOT from dict)'
FROM free_queue fq
WHERE
	NOT (free_queue_id = 1 AND grafik_num = 1 AND free_queue_num = 1 AND comm = 'Гр.№1 - 1 Очередь')
	AND NOT (free_queue_id = 2 AND grafik_num = 1 AND free_queue_num = 2 AND comm = 'Гр.№1 - 2 Очередь')
	AND NOT (free_queue_id = 3 AND grafik_num = 1 AND free_queue_num = 3 AND comm = 'Гр.№1 - 3 Очередь')
	AND NOT (free_queue_id = 4 AND grafik_num = 1 AND free_queue_num = 4 AND comm = 'Гр.№1 - 4 Очередь')
	AND NOT (free_queue_id = 5 AND grafik_num = 2 AND free_queue_num = 1 AND comm = 'Гр.№2 - 1 Очередь, 20%')
	AND NOT (free_queue_id = 6 AND grafik_num = 2 AND free_queue_num = 2 AND comm = 'Гр.№2 - 2 Очередь, 50%')
	AND NOT (free_queue_id = 7 AND grafik_num = 2 AND free_queue_num = 3 AND comm = 'Гр.№2 - 3 Очередь, 80%')
""";

//println objects.dump()
objects.Bean.eachWithIndex{obj, idx->
//objects.Bean[0..0].eachWithIndex{obj, idx->
//	println "${idx + 1}/${objects.Bean.size()} :: ${obj.name.text()} : ${obj.url.text()} : Driver(${obj.driverIdentifier.string.text()}) - ${ SQLExecute.getDriverStr(drivers, obj) }"

/*
	try{
		Sql sql = Sql.newInstance(obj.url.text(), obj.userName.text(), obj.password.text(), getDriverStr(drivers, obj));
	//	println sql.rows("SELECT * FROM ext_system WHERE system_code LIKE '%.imus' AND system_code NOT IN ('mrgmain.imus', 'antmain.imus')").join('->\n')
		println sql.rows("SELECT run_date, status, msg FROM up_db_script WHERE change_id = 598").join('->\n')
	}
	catch(java.sql.SQLException e){
		System.err.println('Exception happened during connect or query: ' + e);
		System.err.flush();
	}
*/

//	ActiveMQVersionCheck u = new ActiveMQVersionCheck( ip:getIP(obj), name: obj.name.text(), runNo: idx + 1);
//	pool.submit u;
//	results << u;

//	SQLExecute r = new SQLExecute( drivers: drivers, obj: obj, runNo: idx + 1, sql: SQL);
//	pool.submit r;
//	results << r;

	// res.findAll{ !it?.res?.out }.collect{ "${it.name}: ${it.res}" }
	RsyncFileGrep u = new RsyncFileGrep(obj: obj, runNo: idx + 1, remotePath: 'imus/esb/conf/ais.properties', grep: 'TransDic');
	pool.submit u;
	results << u;
}

// Wait for the pool close when all threads are completed
pool.shutdown();
pool.awaitTermination( 100, TimeUnit.SECONDS );

// Print our results
println "RESULTS:"
/*
results.each{
//	println "$it.name ($it.runNo) : $it.version"
//	if (it.rows[0].dict < 7)
	if (it.rows?.size() > 0 || it.error){
//	if (it.rows?.getAt(0)?.gco_gas_free_COUNT > 0 || it.rows?.getAt(0)?.gas_free_reg_fact_COUNT > 0 || it.error){
		if (it.error){
			System.err.println(it.error);System.err.flush();
		}
		else{
			println "$it.name ($it.runNo):\n${it.rows.join('\n')}"
		}
	}
}
*/

// http://groovy.codehaus.org/Groovy+Console
Console console = new Console([res: results, pool: pool] as Binding);
//Console console = new Console(this.getClass().getClassLoader().getRootLoader(), [res: results, pool: pool] as Binding);
//Console console = new Console(this.getClass().getClassLoader(), [res: results, pool: pool] as Binding);

//console.sour.setText('res.each{ it }');

console.run();
console.with{ // Set default content of console
	swing.edt {
		inputArea.editable = false
	}
	swing.doOutside {
		try {
			def consoleText ='''// Typical usage this console to analyse queried results:
res.each{
	if (it.res || it.error){
		if (it.error){
			System.err.println("$it.name ($it.runNo): ${it.error}");System.err.flush();
		}
		else{
//			println "$it.name ($it.runNo):\\n${it.res.join('\\n')}" // SQL
			println "$it.name ($it.runNo):${it.res}"
		}
	}
}
''';
			swing.edt {
				updateTitle()
				inputArea.document.remove 0, inputArea.document.length
				inputArea.document.insertString 0, consoleText, null
				setDirty(false)
				inputArea.caretPosition = 0
			}
		} finally {
			swing.edt { inputArea.editable = true }
			// GROOVY-3684: focus away and then back to inputArea ensures caret blinks
			swing.doLater outputArea.&requestFocusInWindow
			swing.doLater inputArea.&requestFocusInWindow
		}
	}
}
println "RUN";