@Grab(group='com.google.guava', module='guava', version='14.0.1')
import com.google.common.collect.*

// Groovy way
ArrayListMultimap.metaClass.leftShift = {Map m->
 m.each{
  delegate.put(it.key, it.value)
 }
}

ArrayListMultimap.metaClass.getProperty = {prop->
  delegate.get(prop)
}

//public class MutliMapTest {
//    public static void main(String... args) {
  Multimap<String, String> mm = ArrayListMultimap.create();
   
  // Adding some key/value
  mm.put("Fruits", "Bannana");
  mm.put("Fruits", "Apple");
  mm.put("Fruits", "Pear");
  mm.put("Vegetables", "Carrot");
  mm << [Test: 'qwerty'];
   
  println '1 (size)'
  println "Initial size: ${mm.size()}"

  println '2 (multiple)'
  // Getting values
  Collection<String> fruits = mm.get("Fruits");
  println(fruits); // [Bannana, Apple, Pear]

  println '3 (multiple Groovy-way)'
  println mm.Fruits

  println '4 (sinple)'
  Collection<String> vegetables = mm.get("Vegetables");
  System.out.println(vegetables); // [Carrot]

  println '5 (sinple Groovy-way)'
  println mm.Vegetables
  println mm.Test

  println '6 (iterate all values)'
  // Iterating over entire Mutlimap
  for(String value : mm.values()) {
    println(value);
  }

  // Removing a single value
//  mm.remove("Fruits","Pear");
//  System.out.println(mm.get("Fruits")); // [Bannana, Pear]
   
  // Remove all values for a key
//  mm.removeAll("Fruits");
//  System.out.println(mm.get("Fruits")); // [] (Empty Collection!)
// }
//}