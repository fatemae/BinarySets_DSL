package scala

object SetImpl:
  @main def runSetExp(): Unit =
    import SetOperations.SetOper.*
    val A:collection.mutable.Map[String,Any] = collection.mutable.Map("123"->1233,"xyz"->collection.mutable.Set(123,567))
    val B:collection.mutable.Map[String,Any] = collection.mutable.Map[String, Any]()
    Scope("scopename", Scope("othername", Insert(Variable("someSetName"), List(Value("var"), Value(1), Value("somestring"))))).eval
    Scope("scopename", Scope("othername", Check(Variable("someSetName"),Value(1)))).eval

