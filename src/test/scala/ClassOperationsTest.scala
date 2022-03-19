import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ClassOperations.ClassOper.*
import SetOperations.SetOper.*
import SetOperations.*

import scala.collection.mutable.*

class ClassOperationsTest extends AnyFlatSpec with Matchers{
  behavior of "Classes and Inheritance"
  it should "should result in creating a class definition with a field" in {
    ClassDef("ClassA", Field("a")).eval()
    assert(bindingScope.getOrElse("ClassA", null)!=null)
  }

  it should "should result in creating an object of ClassA" in {
    ClassDef("ClassA1", Field("setA"), Constructor(Parameters(null),Assign("setA", Value(collection.mutable.HashSet(2))))).eval()
    NewObject("ClassA1", "ClassA1", Variable("newObj1"), Parameters(null)).eval()
    val str = "HashMap(<method> -> HashMap(ClassA1+0 -> HashMap(<methodBody> -> ArraySeq(Assign(setA,Value(HashSet(2)))), <methodParams> -> HashMap())), <field> -> HashMap(setA -> HashSet(2)))"
    assert(str==Object("newObj1").eval().toString)
  }

  it should "should result in creating a method with no params in a class and invoking the method on the class object" in {
    ClassDef("ClassA2", Field("setA2"), Constructor(Parameters(null),Assign("setA2", Value(collection.mutable.HashSet(2)))),
      Method("newMethodNoParam", Parameters(null),Union(Variable("setA2"), Value(collection.mutable.HashSet("hi", "mario"))))).eval()
    NewObject("ClassA2", "ClassA2" ,Variable("newObj2"), Parameters(null)).eval()
    val op = InvokeMethod(Object("newObj2"), "newMethodNoParam", Parameters(null))
    assert(op.eval() == collection.mutable.HashSet(2, "hi", "mario"))
  }

  it should "should result in creating a parameterized method and constructor in a class and invoking the method on the class object" in {
    ClassDef("ClassA3", Field("a"), Field("setA3"),
      Constructor(Parameters(collection.mutable.Map("v"->null, "v1"->null)),
        Assign("a", Variable("v")), Assign("setA3", Variable("v1"))),
      Method("newMethodWithParam", Parameters(collection.mutable.Map("x"->None, "y"->None)),
        Insert(Variable("setZ3"), List(Variable("x"), Variable("y"))),Union(Variable("setA3"), Variable("setZ3")))).eval()
    NewObject("ClassA3", "ClassA3", Variable("newObj3"), Parameters(collection.mutable.Map("v"->2,
      "v1"->collection.mutable.HashSet("set", "34", 1234)))).eval()
    val op = InvokeMethod(Object("newObj3"), "newMethodWithParam", Parameters(collection.mutable.Map("x"->1, "y"->2)))
    assert(op.eval() == collection.mutable.HashSet(1,"34", 2, 1234,"set"))
  }

  it should "result in inheritence of ClassB to ClassC" in {
    ClassDef("ClassC",
      Field("a"),
      Field("setx"),
      Method("newMethod", Parameters(collection.mutable.Map("x"->None, "y"->None)),
        Insert(Variable("setz"), List(Variable("x"), Variable("y"))),
        Union(Variable("setz"), Value(collection.mutable.HashSet(123,"xyz"))))).eval()
    ClassDef("ClassB", Field("b"), Constructor(Parameters(null), Assign("b", Value(5))), EXTENDS("ClassC")).eval()
    NewObject("ClassC", "ClassB", Variable("newObjB"), Parameters(null)).eval()
    val op = InvokeMethod(Object("newObjB"), "newMethod" , Parameters(collection.mutable.Map("x"->1, "y"->2)))
    assert(op.eval() == collection.mutable.HashSet("xyz", 1, 2, 123))
  }

}
