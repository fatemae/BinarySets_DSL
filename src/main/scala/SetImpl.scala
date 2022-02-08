package scala

object SetImpl:
  @main def runSetExp(): Unit =
    import SetOperations.SetOper.*
    Insert(Variable("A"), List(Value("set"), Value("34"), Value(1234))).eval
    println(Variable("A").eval)

    val a = collection.mutable.HashSet("123", "ABC", false)
    val b = collection.mutable.HashSet("set", "34", 1234)
    println(Variable("A").eval==b)


