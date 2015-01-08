import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import groovy.swing.SwingBuilder

/*
Base:
Swing tree:
http://www.java2s.com/Tutorial/Java/0240__Swing/JTreenodemouseclickevent.htm
Groovy JTree
http://osdir.com/ml/lang.groovy.user/2004-07/msg00046.html

https://docs.oracle.com/javase/7/docs/api/javax/swing/tree/DefaultMutableTreeNode.html
*/

def topNode = new DefaultMutableTreeNode('Projects')
def project1Node = new DefaultMutableTreeNode('Project1')
topNode.add(project1Node)
topNode.add(new DefaultMutableTreeNode('Project2'))
project1Node.add(new DefaultMutableTreeNode('Project1-1'))
projectTree = new JTree(topNode)

aFrame = new SwingBuilder().frame(title:"Hello World",size:[200,200]){
    panel(layout: new FlowLayout()) {
      scrollPane(preferredSize:[200,130]) {
        tree(projectTree)
      }
      panel(layout:new GridLayout(1,2,15,15)){
         button(
             text:"Ok"
             ,actionPerformed: {
                 // Dialog http://stackoverflow.com/questions/6270354/how-to-open-warning-information-error-dialog-in-swing, https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html
                 JOptionPane.showMessageDialog(aFrame, "test OK");
             }
         )
         button(text:"Cancel"
             ,actionPerformed: {
                 aFrame.hide();
             }
         )
      }
    }
}
aFrame.show()