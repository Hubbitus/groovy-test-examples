#!/usr/bin/env groovy

// http://www.kellyrob99.com/blog/2009/04/19/groovy-and-glazed-lists-with-grape/

import ca.odell.glazedlists.*
import ca.odell.glazedlists.gui.*
import ca.odell.glazedlists.swing.*
import groovy.swing.*
import javax.swing.*
 
@Grab (group = 'net.java.dev.glazedlists', module = 'glazedlists_java15', version = '1.8.0')
public class SVNGlazedListExample
{
    public static void main(args)
    {
        def columnNames = ['Code', 'Revision', 'Filename']
        def sortedRevisions
 
        //execute on the command line and populate buffers with the output
        def sout = new StringBuffer()
        def serr = new StringBuffer()
        def Process process = "svn stat -u".execute([], new File('/home/pasha/imus/IdeaProjects/imus/ascug-maket/ascug-esb-modules/imus-esb-core/'))
        process.consumeProcessOutput(sout, serr)
        process.waitFor()
 
        SwingBuilder builder = new SwingBuilder();
 
        /**
         * closure returns a JComponent showing the buffered output
         */
        def getTableModel = {out, err ->
            if (err)      //Groovy truth FTW
            {
                //create error output table
                return builder.textPane(size: [500, 500], text: err)
            }
            else
            {
                //create three column table
                def lines = out.toString().split('\n')
                def data = []
                lines.each() {
                    def tokens = it.tokenize()  //Groovy add which abstract away spaces vs tabs
                    //a hack to be sure, but let's just get trebles for now
                    if (tokens.size() == 3){
                        //rows in the data are just a map
                        data << [revision: tokens[1], filename: tokens[2]]
                    }
                }
                //passing in null gives us default String comparator
                sortedRevisions = new SortedList(new BasicEventList(data),null)
 
                //this little trick courtesy of Andres Almiray - Thanks!
                final EventTableModel model = new EventTableModel(sortedRevisions, [
                        getColumnCount: {columnNames.size()},
                        getColumnName: {index -> columnNames[index]},
                        getColumnValue: {object, index ->
                            object."${columnNames[index].toLowerCase()}"
                        }] as TableFormat)
 
                return builder.table(id: 'revisions', model: model)
            }
        }
 
        //the 'show it' code
        builder.frame(title: 'SVN Change List', size: [500, 500], pack: true, show: true,
                defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE) {
            scrollPane {
                def component = getTableModel(sout, serr)
                //I'm sure this could be more elegant
                if(component instanceof JTable){
                    def tableSorter = new TableComparatorChooser(revisions,
                        sortedRevisions, AbstractTableComparatorChooser.MULTIPLE_COLUMN_MOUSE)
                }
            }
        }
    }
}