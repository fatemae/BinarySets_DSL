import SetOperations.SetOper.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PartialEvalOptimizationTest extends AnyFlatSpec with Matchers {
  behavior of "DSL with Partial Evaluation and Optimization "
  Insert(Variable("PevalA") , List(Value("setA"), Value("100"))).eval()

  it should "result in partial Evaluation of Union Operation" in {
    val op = Union(Variable("PevalA"),Variable("PevalB")).eval()
    assert(op == Union(Value(Set("setA", "100")), Variable("PevalB")))
  }

  it should "result in partial Evaluation of Intersection Operation" in {
    val op = Intersection(Variable("PevalA"),Variable("PevalB")).eval()
    assert(op == Intersection(Value(Set("setA", "100")), Variable("PevalB")))
  }

  it should "result in partial Evaluation of Other Set Operations" in {
    val op = Difference(Variable("PevalA"),Variable("PevalB")).eval()
    assert(op == Difference(Value(Set("setA", "100")), Variable("PevalB")))
    val op1 = Symm_Difference(Variable("PevalA"),Variable("PevalB")).eval()
    assert(op1 == Union(Difference(Value(Set("100", "setA")),Variable("PevalB")),Difference(Variable("PevalB"),Value(Set("100", "setA")))))
    val op2 = Cartesian_Product(Variable("PevalA"),Variable("PevalB")).eval()
    assert(op2 == Cartesian_Product(Value(Set("setA", "100")), Variable("PevalB")))

  }

  it should "result in applying OptimizedUnion Function on a SetExpression" in {
    val x = OptimizedUnion(Union(Variable("varA"), Variable("varA"))).eval()
    assert(x == Variable("varA"))
  }

  it should "result in applying OptimizedIntersection Function on a SetExpression" in {
    val x = OptimizedIntersection(Intersection(Variable("varA"), Variable("varA"))).eval()
    assert(x == Variable("varA"))
  }

  it should "result in applying OptimizedDifference Function on a SetExpression" in {
    val x = OptimizedDifference(Difference(Variable("varA"), Variable("varA"))).eval()
    assert(x == Set())
  }

  it should "result in applying OptimizedCProduct Function on a SetExpression" in {
    val x = OptimizedCProduct(Cartesian_Product(Variable("varA"), Value(Set()))).eval()
    assert(x == Set())
  }


  it should "result in applying OptimizedUnion Function on a container of SetExpression" in {
    val x = ExpressionContainer(List(Union(Variable("varA"), Variable("varA")),
      Union(Variable("varA"), Value(Set())),
      Union(Value(Set()), Variable("varA")),
      Union(Variable("varA"), Variable("varB")))).map(OptimizedUnion)
    assert(x ==
      ExpressionContainer(List(Variable("varA"), Variable("varA"), Variable("varA"),
        Union(Variable("varA"),Variable("varB")))))
  }

  it should "result in applying OptimizedIntersection Function on a container of SetExpression using map" in {
    val x = ExpressionContainer(List(Intersection(Variable("varA"), Variable("varA")),
      Intersection(Variable("varA"), Value(Set())),
      Intersection(Value(Set()), Variable("varA")),
      Intersection(Variable("varA"), Variable("varB")))).map(OptimizedIntersection)
    assert(x ==
      ExpressionContainer(List(Variable("varA"), Value(Set()), Value(Set()),
        Intersection(Variable("varA"),Variable("varB")))))
  }

  it should "result in applying OptimizedDifference Function on a container of SetExpression using map" in {
    val x = ExpressionContainer(List(Difference(Variable("varA"), Variable("varA")),
      Difference(Variable("varA"), Value(Set())),
      Difference(Value(Set()), Variable("varA")),
      Difference(Variable("varA"), Variable("varB")))).map(OptimizedDifference)
    assert(x ==
      ExpressionContainer(List(Value(Set()), Variable("varA"), Variable("varA"),
        Difference(Variable("varA"),Variable("varB")))))
  }

  it should "result in applying Optimized Cartesian Product Function on a container of SetExpression using map" in {
    val x = ExpressionContainer(List(Cartesian_Product(Variable("varA"), Variable("varA")),
      Cartesian_Product(Variable("varA"), Value(Set())),
      Cartesian_Product(Value(Set()), Variable("varA")),
      Cartesian_Product(Variable("varA"), Variable("varB")))).map(OptimizedCProduct)
    assert(x ==
      ExpressionContainer(List(Cartesian_Product(Variable("varA"),Variable("varA")), Value(Set()), Value(Set()),
        Cartesian_Product(Variable("varA"),Variable("varB")))))
  }

}
