package com.onresolve.scriptrunner.parameters.annotation

/**
* This annotation intended to be run in ScriptRunner scripts only. So, creating stub to compile project
* @author Pavel Alexeev <plalexeev@gid.ru>
**/
@interface ShortTextInput {
    String label() default "";
    String description() default "";
}