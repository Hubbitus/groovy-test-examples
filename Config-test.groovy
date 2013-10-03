#!/usr/bin/env groovy

package imus.ais.utils

@Grab(group='log4j', module='log4j', version='1.2.16')
@Grab(group='org.slf4j', module='slf4j-api', version='1.7.5')
@Grab(group='ch.qos.logback', module='logback-core', version='1.0.13')
@Grab(group='ch.qos.logback', module='logback-classic', version='1.0.13')
import groovy.util.logging.Slf4j

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * Simple config holder for sharing
 *
 * As it treated as global, it will be bean managed by imus.ais.spring and assigned for adapter, but most fields static with
 *	non-static setters to act as Singleton pattern.
 *
 * It should be available globally just as Config.field
 * Constructed by Spring
 *
 * Written as backward holder to read old ais.properties. For that have such quirks as transformations because not lists
 *	available and other complex types
 *
 * @author Pavel Alexeev <Pahan@Hubbitus.info>
 * @created 15.07.2013 22:27
 */
@Slf4j
@Singleton
class Config{
	public static final String PROPERTIES_PREFIX_STRIP = 'ais.ws.';

	ConfigObject _conf;

	Thread watcher;

	public static final def stringToSet(String val){
		(val.split(',') as Set).collect{ it.trim() }
	}

	public static final def stringToBoolean(String val){
		if ('true' == val.trim().toLowerCase()) return true;
		if ('false' == val.trim().toLowerCase()) return false;
		return val;
	}

	public static final def stringToInteger(String val){
		try{
			return Integer.valueOf(val);
		}
		catch(NumberFormatException ignore){
			return val; // as is
		}
	}

	public static Map<String, Closure> TRANSFORMATIONS = [
		muteErrors: this.&stringToSet
		,forbiddenTasks: this.&stringToSet
		,dvisAbsent: this.&stringToSet
		,replaceGenerated: this.&stringToSet

		,attachPcToGcoConsists: {String val-> val.trim() }
	];

	static ConfigObject parseProperties(String filename){
		ConfigObject conf = new ConfigObject();

		Properties prop = new Properties();
		prop.load(new InputStreamReader(this.class.getResource(filename).openStream(), 'UTF8'));

		prop.each{String key, String value->
			String k = key.replace(PROPERTIES_PREFIX_STRIP, '');
			if (TRANSFORMATIONS[k]){
				setFromPropertyLikeKey(conf, k, TRANSFORMATIONS[k](value));
			}
			else{
				setFromPropertyLikeKey(conf, k, value);
				// Try some basic type transformations by default.
				[ this.&stringToBoolean, this.&stringToInteger ].find{
					def newValue = it(value);
					if (newValue != value){
						setFromPropertyLikeKey(conf, k, newValue);
						return true; // stop loop
					}
				}
			}
		}

		conf;
	}

	/**
	 * It is not work set it from doted string, but read in groovy syntax like:
	 * ConfigObject conf = new ConfigObject();
	 * conf.'some.key' = 77;
	 * assert conf.some.key == [:]
	 *
	 * This method it workaround.
	 * It is also safe for keys without dots.
	 *
	 * @param co
	 * @param propertyLikeKey
	 * @param value
	 */
	private static void setFromPropertyLikeKey(ConfigObject conf, String propertyLikeKey, value){
		conf.merge(new ConfigSlurper().parse( [(propertyLikeKey): value] as Properties ))
	}

	/**
	 * (Re)initialization_
	 */
	public void init(){
		log.info 'init()'

		_conf = (ConfigObject)parseProperties('/imus/ais/ais-ws-default.properties');
		merge(parseProperties('/ais.properties'));

		// Start watching for ais.properties change. Unfortunately only watch dir implemented in Java 7, not just file change
		URI uri = this.class.getResource('/ais.properties').toURI();
		Path watchDir = Paths.get(this.class.getResource('/ais.properties').toURI().resolve('.')); // https://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html , http://stackoverflow.com/questions/10159186/how-to-get-parent-url-in-java

		WatchService watchService = FileSystems.getDefault().newWatchService();
		watchDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

		// Processing will be by done watch service in another thread to in sane way handle also manual placed there files
		watcher = Thread.start {
			while(true){
				WatchKey key = watchService.take()
				if (key) {
					key.pollEvents().each{
						if (it.context().toString() == 'ais.properties'){
							onConfigChange();
						}
					}
					key.reset()
				}
				sleep(100); // 100 ms
			}
		}
	}

	/**
	 * Base implementation borrowed from ConfigObject.merge and added possibility log differences
	 * Merges the given map with this ConfigObject overriding any matching configuration entries in this ConfigObject
	 *
	 * @param other The ConfigObject to merge with
	 * @return The result of the merge
	 */
	Map merge(ConfigObject other, boolean logDiff = false){
		return doMerge(this._conf, other, logDiff);
	}

	/**
	 * Base implementation borrowed from ConfigObject.doMerge and added possibility log differences
	 *
	 * @param config
	 * @param other
	 * @return
	 */
	private doMerge(Map config, Map other, boolean logDiff = false) {
		def logChange = {entry->
			if (logDiff && config[entry.key] != entry.value){
				log.info("Config entry [${entry.key}] changed: [${config[entry.key]}] => [${entry.value}]");
			}
		}

		for(entry in other) {
			def configEntry = config[entry.key]
			if(configEntry == null){
				logChange(entry);
				config[entry.key] = entry.value;
			}
			else {
				if(configEntry instanceof Map && configEntry.size() > 0 && entry.value instanceof Map) {
					doMerge(configEntry, entry.value, logDiff); // recur
				}
				else {
					logChange(entry);
					config[entry.key] = entry.value;
				}
			}
		}
		return config
	}

	synchronized private void onConfigChange(){
		println 'onConfigChange()'
		log.error('Config changed');
		merge(parseProperties('/ais.properties'), true);
	}

	/**
	 * {@see http://jira.codehaus.org/browse/GROOVY-6264#comment-328878}
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	public static void set(String name, value){
		if (!Config.instance._conf) Config.instance.init();
		Config.instance._conf."$name" = value;
	}

	static{
		Config.metaClass.static.propertyMissing << {prop->
			if (!Config.instance._conf) Config.instance.init();
			instance._conf."$prop";
		}
	}
}

Config c = Config.instance;
println Config.user

while (true){
}