#!/bin/env groovy

import javax.swing.*

import groovy.ui.OutputTransforms

Map results = [ one: 1, two: 2, three: 3 ];

def c;

println 'RESULTS:'
// http://groovy.codehaus.org/Groovy+Console
groovy.ui.Console console = new groovy.ui.Console([res: results] as Binding);

//console.shell.context._outputTransforms = OutputTransforms.loadOutputTransforms();

console.visualizeScriptResults = true;
console.shell.context._outputTransforms << { result ->
    if (result instanceof Map) {
        def table = new JTable(
            result.collect{ k, v ->
                [k, v?.inspect()] as Object[]
            } as Object[][],
            ['Key', 'Value'] as Object[])
        table.preferredViewportSize = table.preferredSize
        return new JScrollPane(table)
    }
}
//console.binding.variables._outputTransforms = OutputTransforms.loadOutputTransforms()

console.run();

//println console.transform;
console.with{ // Set default content of console
	swing.edt {
		inputArea.editable = false
	}
	swing.doOutside {
		try {
			def consoleText ='''println res;''';
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
