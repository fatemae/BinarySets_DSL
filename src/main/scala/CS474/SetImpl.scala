package scala.CS474

import collection.mutable

object SetImpl :
  @main def runSetExp() : Unit =
    import SetOperations.SetOper.*
    Insert(Variable("A"),List(Value("set"),Value("34"), Value(1234))).eval
    Insert(Variable("B"),List(Value("xyz"),Value("hello there"), Value("34"))).eval
    //    Delete(Variable("A"),Value(1234)).eval
    val thirdExpr = Check(Variable("A"), Value(1234)).eval
    Macro("union",Union(Variable("A"),Variable("B"))).eval
    val D = Union(MacroEval("union"),Value(mutable.Set(1,2,3)))
    println(D.eval)
//    Scope("scopename",
//      Scope("othername", Assign(Variable("someSetName"), Insert(Variable("var"), Value(1)), Value("somestring"))))
//    Assign(Scope("scopename", Scope("othername", Variable("someSetName"))), Insert(Value("x")))

