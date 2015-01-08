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

// http://sandarenu.blogspot.in/2008/02/treetable-in-java-using-swingx.html
@Grab(group='org.swinglabs.swingx', module='swingx-all', version='1.6.5-1')
import org.jdesktop.swingx.JXTreeTable

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new TableRowData("CF","","","",true));
        
        DefaultMutableTreeNode incomeNode = new DefaultMutableTreeNode(new TableRowData("Income","25000","5000","300000",true));
        incomeNode.add(new DefaultMutableTreeNode(new TableRowData("Salary1","250001","50001","3000001",false)));
        incomeNode.add(new DefaultMutableTreeNode(new TableRowData("Salary2","250002","50002","3000002",false)));
        incomeNode.add(new DefaultMutableTreeNode(new TableRowData("Salary3","250003","50003","3000003",false)));
        incomeNode.add(new DefaultMutableTreeNode(new TableRowData("Salary4","250004","50004","3000004",false)));
        incomeNode.add(new DefaultMutableTreeNode(new TableRowData("Salary5","250005","50005","3000005",false)));
        
        rootNode.add(incomeNode);
        rootNode.add(new DefaultMutableTreeNode());
        
        DefaultMutableTreeNode expenseNode = new DefaultMutableTreeNode(new TableRowData("Expenses","25000","5000","300000",true));
        expenseNode.add(new DefaultMutableTreeNode(new TableRowData("Salary1","250001","50001","3000001",false)));
        expenseNode.add(new DefaultMutableTreeNode(new TableRowData("Salary2","250002","50002","3000002",false)));
        expenseNode.add(new DefaultMutableTreeNode(new TableRowData("Salary3","250003","50003","3000003",false)));
        expenseNode.add(new DefaultMutableTreeNode(new TableRowData("Salary4","250004","50004","3000004",false)));
        expenseNode.add(new DefaultMutableTreeNode(new TableRowData("Salary5","250005","50005","3000005",false)));
        
        rootNode.add(expenseNode);
    
        JXTreeTable binTree = new JXTreeTable(new MyTreeModel(rootNode));

aFrame = new SwingBuilder().frame(title:"Hello World",size:[200,200]){
    panel(layout: new FlowLayout()) {
      scrollPane(preferredSize:[400,300]) {
        widget(binTree)
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


//////////////////////////////
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class MyTreeModel extends AbstractTreeTableModel
{
    private String[] titles = ["CF Source","Client","Spouse","Family"];

    
    public MyTreeModel(DefaultMutableTreeNode root)
    {
        super(root);
    }
     
    /**
     * Table Columns
     */
    public String getColumnName(int column) {
        if (column < titles.length)
            return (String) titles[column];
        else
            return "";
    }

    public int getColumnCount()
    {
        return titles.length;
    }
    
    public Class getColumnClass(int column)
    {
        return String.class;
    }

    public Object getValueAt(Object arg0, int arg1)
    {
        if(arg0 instanceof TableRowData)
        {
            TableRowData data = (TableRowData)arg0;
            if(data != null)
            {
                switch(arg1)
                {
                case 0: return data.getSource();
                case 1: return data.getClient();
                case 2: return data.getSpouse();
                case 3: return data.getFamily();
                }
            }
            
        }
        
        if(arg0 instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode)arg0;
            TableRowData data = (TableRowData)dataNode.getUserObject();
            if(data != null)
            {
                switch(arg1)
                {
                case 0: return data.getSource();
                case 1: return data.getClient();
                case 2: return data.getSpouse();
                case 3: return data.getFamily();
                }
            }
            
        }
        return null;
    }

    public Object getChild(Object arg0, int arg1)
    {
        
        if(arg0 instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode nodes = (DefaultMutableTreeNode)arg0;
            return nodes.getChildAt(arg1);
        }
        return null;
    }

    public int getChildCount(Object arg0)
    {
        
        if(arg0 instanceof DefaultMutableTreeNode)
        {
            DefaultMutableTreeNode nodes = (DefaultMutableTreeNode)arg0;
            return nodes.getChildCount();
        }
        return 0;
    }

    public int getIndexOfChild(Object arg0, Object arg1)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
     public boolean isLeaf(Object node) 
     {
            return getChildCount(node) == 0;
     }

}

public class TableRowData
{
    private String source = null;
    private String client;
    private String spouse;
    private String family;
    private boolean isRoot;
    /**
     * Created on: Feb 23, 2008
     * @Author: sandarenu
     * @param source
     * @param client
     * @param spouse
     * @param family
     * @param isRoot
     */
    public TableRowData(String source, String client, String spouse, String family, boolean isLeaf)
    {
        this.source = source;
        this.client = client;
        this.spouse = spouse;
        this.family = family;
        this.isRoot = isLeaf;
    }
    /**
     * @return the client
     */
    public String getClient()
    {
        return client;
    }
    /**
     * @param client the client to set
     */
    public void setClient(String client)
    {
        this.client = client;
    }
    /**
     * @return the family
     */
    public String getFamily()
    {
        return family;
    }
    /**
     * @param family the family to set
     */
    public void setFamily(String family)
    {
        this.family = family;
    }
    /**
     * @return the isRoot
     */
    public boolean isRoot()
    {
        return isRoot;
    }
    /**
     * @param isRoot the isRoot to set
     */
    public void setRoot(boolean isLeaf)
    {
        this.isRoot = isLeaf;
    }
    /**
     * @return the source
     */
    public String getSource()
    {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource(String source)
    {
        this.source = source;
    }
    /**
     * @return the spouse
     */
    public String getSpouse()
    {
        return spouse;
    }
    /**
     * @param spouse the spouse to set
     */
    public void setSpouse(String spouse)
    {
        this.spouse = spouse;
    }
    
}
