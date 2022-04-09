import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ClassOperations.ClassOper.*
import SetOperations.SetOper.*
import SetOperations.*
import CONSTANTS.*

import scala.collection.mutable
import scala.collection.mutable.*
class ControlStructuresTest extends AnyFlatSpec with Matchers{

  private def getFields(scopeName: String): mutable.Map[String, Any] = {
    bindingScope.getOrElse(scopeName, null) match {
      case s : mutable.Map[String, Any] => s.getOrElse(field, null) match {
        case m: mutable.Map[String, Any] =>
          m
        case _ => null
      }
      case _ => null
    }
  }

  behavior of "ControlStructures and Exceptions"

  it should "result in execution of else case" in {
    Scope("IfElseTestScope", Insert(Variable("someSetElse"), List(Value("var"), Value(1), Value("somestring")))).eval()
    val op = Scope("IfElseTestScope", IfThenElse(Check(Variable("someSetElse"), Value("var1")),
      List(Insert(Variable("someSetElse"), List(Value(10)))), List(Insert(Variable("someSetElse"), List(Value(15)))))).eval()
    getFields("IfElseTestScope") match {
      case m: mutable.Map[String, Any] =>
        assert(m.getOrElse("someSetElse", null) == collection.mutable.HashSet(1, "var", "somestring", 15))
      case _ =>
    }
  }

  it should "result in execution of if case" in {
    Scope("IfElseTestScope", Insert(Variable("someSetIf"), List(Value("var"), Value(1), Value("somestring")))).eval()
    val op = Scope("IfElseTestScope", IfThenElse(Check(Variable("someSetIf"), Value("var")),
      List(Insert(Variable("someSetIf"), List(Value(10)))), List(Insert(Variable("someSetIf"), List(Value(15)))))).eval()
    getFields("IfElseTestScope") match {
      case m: mutable.Map[String, Any] =>
        assert(m.getOrElse("someSetIf", null) == collection.mutable.HashSet(1, "var", "somestring", 10))
      case _ =>
    }
  }

  it should "try - catch block without a exception thrown results in normal flow of execution" in {
    ExceptionClassDef("someExceptonClassName", Field("Reason"),
      Constructor(Parameters(collection.mutable.Map("r"->null)), Assign("Reason", Variable("r"))),
      Method("printReason", Parameters(null), PrintField("Reason"))).eval()
    Scope("scopename", TryCatch(
      List(
        Insert(Variable("someSet"), List(Value("var"), Value(1), Value("somestring"))),
        IfThenElse(Check(Variable("someSet"), Value(1)),
          List(
            NewObject("someExceptonClassName", "someExceptonClassName", Variable("e"),
              Parameters(collection.mutable.Map("r"->"No value found"))),
            Insert(Variable("var1"), List(Value(1)))),
          List(Insert(Variable("var2"), List(Value(3))))),
        Insert(Variable("var3"), List(Value(3)))),
      List(CatchException(ObjectType("e"), InvokeMethod(ObjectType("e"), "printReason", Parameters(null)))))).eval()
    getFields("scopename") match {
      case m : mutable.Map[String, Any] => assert(m.getOrElse("var3", null) != null)
      case _ =>
    }
  }

  it should "try - catch block with a exception thrown with no corresponding catch block result in error to be thrown" in {
    ExceptionClassDef("someExceptonClassName", Field("Reason"),
      Constructor(Parameters(collection.mutable.Map("r"->null)), Assign("Reason", Variable("r"))),
      Method("printReason", Parameters(null), PrintField("Reason"))).eval()
    val op = Scope("scopenameErr", TryCatch(
      List(
        Insert(Variable("someSet"), List(Value("var"), Value(1), Value("somestring"))),
        IfThenElse(Check(Variable("someSet"), Value(1)),
          List(
            NewObject("someExceptonClassName", "someExceptonClassName", Variable("e"),
              Parameters(collection.mutable.Map("r"->"No value found"))),
              ThrowException("someExceptonClassName", ObjectType("e")),
            Insert(Variable("var1"), List(Value(1)))),
          List(Insert(Variable("var2"), List(Value(3))))),
        Insert(Variable("var3"), List(Value(3)))),
      List(CatchException(ObjectType("a"), InvokeMethod(ObjectType("e"), "printReason", Parameters(null))))))
    assertThrows[Error]("Uncaught Exception",op.eval())
  }

  it should "try - catch block with a exception thrown results in abrupt jump of execution flow to catchBlock" in {
    ExceptionClassDef("someExceptonClassName", Field("Reason"),
      Constructor(Parameters(collection.mutable.Map("r"->null)), Assign("Reason", Variable("r"))),
      Method("printReason", Parameters(null), PrintField("Reason"))).eval()
    Scope("scopenameExc", TryCatch(
      List(
        Insert(Variable("someSet"), List(Value("var"), Value(1), Value("somestring"))),
        IfThenElse(Check(Variable("someSet"), Value(1)),
          List(
            NewObject("someExceptonClassName", "someExceptonClassName", Variable("e"),
              Parameters(collection.mutable.Map("r"->"No value found"))),
              ThrowException("someExceptonClassName", ObjectType("e")),
            Insert(Variable("var1"), List(Value(1)))),
          List(Insert(Variable("var2"), List(Value(3))))),
        Insert(Variable("var3"), List(Value(3)))),
      List(CatchException(ObjectType("e"), InvokeMethod(ObjectType("e"), "printReason", Parameters(null)))))).eval()
    getFields("scopenameExc") match {
      case m : mutable.Map[String, Any] => assert(m.getOrElse("var3", null) == null)
      case _ =>
    }
  }

  it should "try - multi catch block with a exception thrown results in abrupt jump of " +
    "execution flow to corresponding catch block" in {
    ExceptionClassDef("someExceptonClassName", Field("Reason"),
      Constructor(Parameters(collection.mutable.Map("r"->null)), Assign("Reason", Variable("r"))),
      Method("printReason", Parameters(null), PrintField("Reason"))).eval()
    ExceptionClassDef("someExceptonClassName1", Field("Reason"),
      Constructor(Parameters(collection.mutable.Map("r"->null)), Assign("Reason", Variable("r"))),
      Method("printReason", Parameters(null), PrintField("Reason"))).eval()
    //this example shows how users use branching and exception constructs
    Scope("scopenamemulticatch", TryCatch(
      List(
        Insert(Variable("someSet"), List(Value("var"), Value(1), Value("somestring"))),
        IfThenElse(Check(Variable("someSet"), Value(1)),
          List(
            NewObject("someExceptonClassName", "someExceptonClassName", Variable("e"),
              Parameters(collection.mutable.Map("r"->"No value found"))),
            NewObject("someExceptonClassName1", "someExceptonClassName1", Variable("a"),
              Parameters(collection.mutable.Map("r"->"No value found"))),
            ThrowException("someExceptonClassName", ObjectType("e")),
            Insert(Variable("var1"), List(Value(1)))),
          List(Insert(Variable("var2"), List(Value(3))))),
        Insert(Variable("var3"), List(Value(3)))),
      List(CatchException(ObjectType("a"), Insert(Variable("var4"), List(Value(3)))),
        CatchException(ObjectType("e"), Insert(Variable("var5"), List(Value(3))))))).eval()

    getFields("scopenamemulticatch") match {
      case m : mutable.Map[String, Any] =>
        assert(m.getOrElse("var4", null) == null)
        assert(m.getOrElse("var5", null) != null)
      case _ =>
    }

  }


}
