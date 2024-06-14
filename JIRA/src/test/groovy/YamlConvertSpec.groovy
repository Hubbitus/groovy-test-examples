import data.GidMostBot
import data.RemoteDebugSend
import spock.lang.Specification
import sun.misc.Unsafe

import java.lang.reflect.Field
import java.lang.reflect.InaccessibleObjectException


class YamlConvertSpec extends Specification {
    def 'Base conversion check'(){
        given:
            Map m = [one: 1, two: 22, three: 333]
        when:
            String res = RemoteDebugSend.toYAML(m).trim()
        then:
            '{one: 1, two: 22, three: 333}' == res
    }

    def 'Convert File'(){
        given:
            File file = new File ('.')
        when:
            String res = RemoteDebugSend.toYAML(file).trim()
        then:
            '!!java.io.File {}' == res

        when:
            res = RemoteDebugSend.toYAML(file, true).trim()
        then:
            '''!!java.io.File
path: !!org.yaml.snakeyaml.introspector.FieldProperty$FieldUnreadable {}''' == res
    }

    def 'field.setAccessible(true)'(){
        when:
            Class<?> clazz = Class.forName("java.io.File");
            Field field = clazz.getDeclaredField("path");
            // this method can not be used in Java 9+ with java modules
            // Unable to make field private static java.util.IdentityHashMap java.lang.ApplicationShutdownHooks.hooks accessible: module java.base does not "opens java.lang" to unnamed module @452b3a41
            field.setAccessible(true);
        then:
            thrown(InaccessibleObjectException)
    }


    private static Unsafe reflectGetUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    def 'Research: setAccessible Unsafe'(){
        given:
            Class<?> clazz = Class.forName('java.io.File')
//            Field field = clazz.getDeclaredField("path")
            // this method can not be used in Java 9+ with java modules
            // Unable to make field private static java.util.IdentityHashMap java.lang.ApplicationShutdownHooks.hooks accessible: module java.base does not "opens java.lang" to unnamed module @452b3a41
//            field.setAccessible(true);
            Unsafe safe = reflectGetUnsafe()

        when: 'Static field:'
            Field field = clazz.getDeclaredField('fs')
            Object value = safe.getObject(clazz, safe.staticFieldOffset(field))
        then:
            noExceptionThrown()
            value instanceof UnixFileSystem
            value.toString().startsWith('java.io.UnixFileSystem@')

        when: 'Instance field:'
            field = clazz.getDeclaredField('path')
            value = safe.getObject(new File('.'), safe.objectFieldOffset(field))
        then:
            noExceptionThrown()
            value instanceof String
            '.' == value
    }
}
