package TEST_examples.GUI.FX

//@GrabConfig(systemClassLoader=true, initContextClassLoader=true)
// Until BUG https://jira.codehaus.org/browse/GFX-41 resolved not 0.4.0 version
@Grab(group = 'org.codehaus.groovyfx', module = 'groovyfx', version = '0.3.1')
import groovy.transform.TupleConstructor
import groovyx.javafx.beans.FXBindable

/**
 * From tutorial:
 * https://wikis.oracle.com/display/OpenJDK/TreeTableView+User+Experience+Documentation
 * https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/tree-table-view.htm
 * EXTENDED
 * http://www.slideshare.net/steveonjava/hacking-java-fx-with-groovy-clojure-scala-and-visage-oscon
 * GROOVY version of TreeTableViewJava.groovy
 */

import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.ReadOnlyLongWrapper
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableColumn
import javafx.scene.control.TreeTableView

import static groovyx.javafx.GroovyFX.start

@TupleConstructor
class Employee {
	@FXBindable String name;
	@FXBindable String email;
	@FXBindable Long salary;
}

start {
	stage(title: 'Tree Table View Sample in GROOVY', visible: true) {
		scene(fill: BLACK, width: 600, height: 250) {
			final TreeItem<Employee> rootFolder = new TreeItem<>(new Employee("Full organization", "", null));

			List<Employee> employees =[
				new Employee("Ethan Williams", "ethan.williams@example.com", 0),
				new Employee("Emma Jones", "emma.jones@example.com", 1),
				new Employee("Michael Brown", "michael.brown@example.com", 2),
				new Employee("Anna Black", "anna.black@example.com", 3),
				new Employee("Rodger York", "roger.york@example.com", 4),
				new Employee("Susan Collins", "susan.collins@example.com", 5)
			]

			rootFolder.setExpanded(true);
			TreeItem<Employee> salesDepartment = new TreeItem<Employee>(new Employee("Sales Department", "", 77))
			rootFolder.children.add(salesDepartment);
			employees.each{employee->
				salesDepartment.getChildren().add(new TreeItem<>(employee));
			};

			TreeItem<Employee> anotherFolder = new TreeItem<>(new Employee("Another Department", "", 777));
			rootFolder.getChildren().add(anotherFolder)
			anotherFolder.children.add(new TreeItem<Employee>(new Employee('Pavel Alexeev', 'pahan@hubbitus.info', 3)))

			// Unfortunately GroovyFX have no TreeTableView builder (yet?), so build it manually and insert as node
			node new TreeTableView<>(rootFolder).with{
				it.setTableMenuButtonVisible(true);
				it.setShowRoot(false);
				it.columns.setAll(
					new TreeTableColumn<>("Employee").with{
						it.setPrefWidth(150);
						it.cellValueFactory = {TreeTableColumn.CellDataFeatures<Employee, String> param ->
							new ReadOnlyStringWrapper(param.value.value.name)
						}
						it
					}
					,new TreeTableColumn<>("Email").with{
						it.setPrefWidth(150);
						it.cellValueFactory = {TreeTableColumn.CellDataFeatures<Employee, String> param ->
							new ReadOnlyStringWrapper(param.value.value.email)
						}
						it
					}
					,new TreeTableColumn<>("Salary").with{
						it.setPrefWidth(80);
						it.cellValueFactory = {TreeTableColumn.CellDataFeatures<Employee, String> param ->
							new ReadOnlyLongWrapper(param.value.value.salary)
						}
						it
					}
				);
				it
			}
		}
	}
}
