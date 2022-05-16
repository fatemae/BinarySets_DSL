import SetOperations.SetOper.*


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SetOperationsTest extends AnyFlatSpec with Matchers {
  behavior of "DSL for Set Operation "
  it should "result in creating a set if set not already present" in {
    assert(Variable("A").eval() == None)
    Insert(Variable("A") , List(Value("set"), Value("34"), Value(1234))).eval()
    val b = collection.mutable.HashSet("set", "34", 1234)
    assert(Variable("A").eval() == b)
  }

  it should "result in deleting an object from the set" in {
    Delete(Variable("A"),Variable("var")).eval()
    Delete(Variable("A"),Value(1234)).eval()
    assert(Variable("A").eval() == collection.mutable.HashSet("set", "34"))
  }

  it should "result in Union of sets" in {
    Insert(Variable("B") , List(Value("new set"), Value(true), Value(100))).eval()
    val op = Union(Variable("A"),Variable("B"))
    assert(op.eval() == collection.mutable.HashSet("set", "34", "new set", true, 100))
  }

  it should "result in Intersection of sets" in {
    Insert(Variable("B") , List(Value("set"))).eval()
    val op = Intersection(Variable("A"),Variable("B"))
    assert(op.eval() == collection.mutable.HashSet("set"))
  }

  it should "result in Difference of sets" in {
    val op = Difference(Variable("A"),Variable("B"))
    assert(op.eval() == collection.mutable.HashSet("34"))
  }

  it should "result in Symmetric Difference of sets" in {
    val op = Symm_Difference(Variable("A"),Variable("B"))
    assert(op.eval() == collection.mutable.HashSet("34", true, 100, "new set"))
  }

  it should "result in Cartesian Product of sets" in {
    val op = Cartesian_Product(Variable("A"),Variable("B"))
    assert(op.eval() == collection.mutable.HashSet(("34",100), ("34","set"), ("set","new set"), ("set","set"), ("set",true), ("34","new set"), ("34",true), ("set",100)))
  }

  it should "result in creating a Macro and evaluation it" in {
    val op = Macro("someName", Seq(Delete(Variable("B"), Value("set"))))
    op.eval()
    MacroEval("someName").eval()
    assert(Variable("B").eval() == collection.mutable.HashSet("new set", true, 100))
  }

  it should "result in creating a Scope" in {
    Scope("scopename", Scope("othername", Insert(Variable("someSetName"), List(Value("var"), Value(1), Value("somestring"))))).eval()
    val op = Scope("scopename", Scope("othername", Check(Variable("someSetName"),Value(1)))).eval()
    assert(op == true)
  }
}
