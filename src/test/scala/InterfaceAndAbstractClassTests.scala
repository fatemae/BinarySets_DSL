import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ClassOperations.ClassOper.*
import ClassOperations.*
import SetOperations.SetOper.*
import SetOperations.*
import collection.mutable.*
import CONSTANTS.*

class InterfaceAndAbstractClassTests extends AnyFlatSpec with Matchers{
  behavior of "Abstract Classes and Interfaces"

  it should "should result in Exception if a non abstract class definition has abstract method" in {
    val op = ClassDef("newC", AbstractMethod("m1", Parameters(null)))
    assertThrows[Exception]("Only abstract class or Interface can have abstract methods",op.eval())
  }


  it should "should result in creating a abstract class definition with a field and abstract method" in {
    AbstractClassDef("NewAbstract",
      Field("abc"),
      AbstractMethod("AbsMethod", Parameters(collection.mutable.Map()))).eval()
    assert(bindingScope.getOrElse("NewAbstract", null)!=null)
  }

  it should "should result in creating a abstract class definition with constructor" in {
    AbstractClassDef("NewAbstract1",
      Field("abc"),
      Constructor(Parameters(null), null),
      AbstractMethod("AbsMethod", Parameters(collection.mutable.Map()))).eval()
    assert(bindingScope.getOrElse("NewAbstract1", null)!=null)
  }

  it should "should result in a Exception if class extends itself" in {
    val op = ClassDef("test", Field("NewField"), EXTENDS("test"))
    assertThrows[Exception]("Cannot inherit itself.",op.eval())
  }

  it should "should result in a Exception if class extending abstract class doesn't override abstract methods" in {
    val op = ClassDef("BaseClass", Field("NewField"), EXTENDS("NewAbstract"))
    assertThrows[Exception]("Doesnot Implement abstract methods of parent class",op.eval())
  }

  it should "should result in inheritance of abstract class , if child class overrides all its abstract methods" in {
    val op = ClassDef("BaseClass", Field("NewField"), Method("AbsMethod", Parameters(collection.mutable.Map()), null), Method("AbsMethod1", Parameters(collection.mutable.Map()), null) ,EXTENDS("NewAbstract"))
    op.eval()
    assert(bindingScope.getOrElse("BaseClass",null)!=null)
  }

  it should "should result in Exception if we create object of abstract class" in {
    AbstractClassDef("A", AbstractMethod("method1", Parameters(null))).eval()
    AbstractClassDef("B", AbstractMethod("method2", Parameters(null)), Method("method1", Parameters(null), Insert(Variable("y"), List(Value(10)))), EXTENDS("A")).eval()
    val op = NewObject("A", "B", Variable("obj"), Parameters(null))

    assertThrows[Exception]("cannot create object of abstract class or Interface",op.eval())
  }

  it should "should result in creation of object of the child class" in {
    AbstractClassDef("C", AbstractMethod("method3", Parameters(null)), Method("method2", Parameters(null), null), EXTENDS("B")).eval()
    ClassDef("D", Method("method3", Parameters(null), Insert(Variable("x"), List(Value(3)))), EXTENDS("C")).eval()
    val op = NewObject("D", "D", Variable("obj"), Parameters(null)).eval()
    assert(Object("obj").eval()!=null)
  }

  it should "should result in Exception if class extends more than one class" in {
    val op = AbstractClassDef("newB",  EXTENDS("A"), EXTENDS("B"))

    assertThrows[Exception]("Cannot extend more than one class/interface",op.eval())
  }

  it should "should result in Exception if dynamic and static type of object creation are not in hierarchy" in {
    val op = NewObject("A", "BaseClass", Variable("obj1"), Parameters(null))
    assertThrows[Exception]("static and dynamic type are not in inheritance hierarchy", op.eval())
  }

  it should "should result in object creation if dynamic and static Type are in inheritance hierarchy" in {
    val op = NewObject("A", "D", Variable("obj1"), Parameters(null)).eval()
    assert(Object("obj1").eval()!=null)
  }

  it should "will return null if methods other than that of objects static type are called" in {
    val op = InvokeMethod(Object("obj1"), "method2", Parameters(null))
    assert(null == op.eval())
  }

  it should "should invoke appropriate methods using dynamic dispatch" in {
    val op = InvokeMethod(Object("obj1"), "method1", Parameters(null))
    assert(() == op.eval())
  }

  it should "should result in Exception if interface extends an abstract class or normal class" in {
    val op = InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null)), EXTENDS("A"))
    assertThrows[Exception]("Interface cannot extend a class/ abstract class",op.eval())
  }

  it should "should result in Exception if interface definition has concrete methods" in {
    val op = InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null)), Method("newM", Parameters(null), null))
    assertThrows[Exception]("Interface cannot have Concrete Methods.",op.eval())
  }

  it should "should result in Exception if interface definition has constructor" in {
    val op = InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null)), Constructor(Parameters(null), null))
    assertThrows[Exception]("Interface cannot have Constructor.",op.eval())
  }



  it should "should result in Exception if abstract or non-abstract class extends an interface" in {
    val op = ClassDef("classC", EXTENDS("newIntr"))
    assertThrows[Exception]("Class cannot extend Interface",op.eval())
  }

  it should "should result in creation of interface with abstract method" in {
    val op = InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null))).eval()
    assert(bindingScope.getOrElse("newIntr", null)!=null)
  }

  it should "should result in Exception if we create an object of interface" in {
    val op = NewObject("newIntr", "newIntr", Variable("tempObj"), Parameters(null))
    assertThrows[Exception]("cannot create object of abstract class or Interface",op.eval())
  }

  it should "should result in Exception if a class implements an interface without implementing the abstract methods" in {
    InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null))).eval()
    InterfaceDef("newIntr1", AbstractMethod("m1", Parameters(null)), EXTENDS("newIntr")).eval()
    val op = ClassDef("X", Implements("newIntr1"))

    assertThrows[Exception]("Doesnot Implement abstract methods of parent class",op.eval())
  }

  it should "should result in creation of a class with with the implementation of parent interface methods" in {
    val op = ClassDef("X", Method("m1", Parameters(null), Insert(Variable("x"), List(Value(3)))), Implements("newIntr1")).eval()
    assert(bindingScope.getOrElse("X", null)!=null)
  }

  it should "should result in creation of an object of a class, with with the implementation of parent interface methods" in {
    val op = NewObject("newIntr", "X", Variable("intrObj"), Parameters(null)).eval()
    assert(Object("intrObj").eval()!=null)
  }


  it should "should result in Exception if an interface implements another interface" in {
    val op = InterfaceDef("X", Implements("newIntr1"))
    assertThrows[Exception]("Interface cannot implement an interface.", op.eval())
  }
}
