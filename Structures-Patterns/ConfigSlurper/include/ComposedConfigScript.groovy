public abstract class ComposedConfigScript extends Script {
    def includeScript(scriptClass) {
        def scriptInstance = scriptClass.newInstance()
        scriptInstance.metaClass = this.metaClass
        scriptInstance.binding = new ConfigBinding(this.getBinding().callable)
        scriptInstance.&run.call()
    }
}
