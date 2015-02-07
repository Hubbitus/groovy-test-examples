
// https://gist.github.com/kiy0taka/2478499
tree = new ConfigObject()

tree.one.one = 11
tree.one.two = 12
tree.one.three = 13
tree.two.one = 21
tree.two.two = 22
tree.two.three = 23
tree.tree = 3
//tree.two.three.one = 231

tree

/*
// http://rosettacode.org/wiki/Tree_traversal#Groovy
// http://groovy.codehaus.org/GroovyMarkup
def tree1 = new NodeBuilder().
'1'(one: 1, two: 2) {
    '2' {
        '4' { '7' {} }
        '5' {}
    }
    '3' {
        '6' { '8' {}; '9' {} }
    }
}
println tree1
*/

/* @TODO report
Node root = new Node(null, '-root-', [root: true], 'root node value')
Node childNode = new Node(root, 'child node', 'child node value')
assert childNode instanceof Node
assert childNode.parent() instanceof Node
//assert childNode.parent().value() instanceof String // Fails! BUG???
assert childNode.parent().value()[0] instanceof String

root.value()[0]
*/


Node root = new Node(null, '-root-', [root: true], 'root node value')
root.attributes().attr1 = 'attr 1'
Node n1 = new Node(root, 'n1', 'n1 value')
n1.value = 777 // not n1.value() = 777 !
Node n2 = new Node(root, 'n2', 'n2 value')
Node n3 = new Node(root, 'n3', 'n3 value')
Node n31 = new Node(n3, 'n31', 'n31 value')

//root
//[ n31.parent().value()[0], n31.parent().value()[0].getClass() ]
n31.parent().parent().value()[0]