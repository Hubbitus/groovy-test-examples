#!/bin/env groovy

//package imus.ais.i18n

/**
 * Created: Pavel Alexeev <Pahan@Hubbitus.info>
 * Created: 02.08.12 20:44
 * Localisation
 */
class L {
    private static groovy.util.NodeList messages;

    private List<String> localesPriority;

    private static L instance = new L();

    public static getInstance(){
        return instance;
    }

    /**
     * Priority language message search (e.g. ['ru', 'ru_RU', 'en']). First found <msg lang="ru" key="...">...</msg>
     * value will be returned. Null mean untranslated, key returned itself.
     *
     * Private, should be {@see getInstance()} used instead as singleton.
     *
     * @param localesPriority By default current locale language
     */
    private L(List<String> localesPriority = [Locale.getDefault().getLanguage()]){
        this.localesPriority = localesPriority;
    }

    public String _(String key, ...params){
        return '_!!' + key;
    }

    static{
	Class.metaClass.static.i18n = {L.getInstance()};// Static i18n() call
	Object.metaClass.static.i18n = {L.getInstance()};// Instance i18n() call. It may also used as just i18n if defined as: "Object.metaClass.static.i18n = L.getInstance();". But similar static call is not allowed. So, leaf single approach
	Class.metaClass.static._ = {String key, ...params-> i18n()._(key, params)}; // Static call _(...)
	Object.metaClass._ = {String key, ...params-> Class.i18n()._(key, params)}; // Instance call _(...)
    }
}



class T{
	public static staticGet(){
//		println 'static: ' + i18n;
		println 'static: ' + i18n(); // Class.metaClass.static.i18n = {L.getInstance()};
//		println 'static: ' + getI18n();
		println _('STATIC');
	}

	public instanceGet(){
//		println 'instance:' + getI18n();
//		println 'instance:' + i18n;
		println 'instance:' + i18n();
		println _('INSTANCE');
	}
}

/* WORKS THERE
Class.metaClass.static.i18n = {L.getInstance()};// Static i18n() call
Object.metaClass.static.i18n = {L.getInstance()};// Instance i18n() call. It may also used as just i18n if defined as: "Object.metaClass.static.i18n = L.getInstance();". But similar static call is not allowed. So, leaf single approach
Class.metaClass.static._ = {String key, ...params-> i18n()._(key, params)}; // Static call _(...)
Object.metaClass._ = {String key, ...params-> Class.i18n()._(key, params)}; // Instance call _(...)
*/

//L.getInstance();

//println _('ttt', 1);
T.staticGet();
t = new T();
t.instanceGet();
