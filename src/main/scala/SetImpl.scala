

object SetImpl:
  @main def runSetExp(): Unit =
    import SetOperations.SetOper.*
    import ClassOperations.ClassOper.*
    val A:collection.mutable.Map[String,Any] = collection.mutable.Map("123"->1233,"xyz"->collection.mutable.Set(123,567))
    val B:collection.mutable.Map[String,Any] = collection.mutable.Map[String, Any]()

//    ClassDef("ClassC",
//      Field("a"),
//      Field("setx"),
//      Method("newMethod", Parameters(collection.mutable.Map("x"->None, "y"->None)),
//        Insert(Variable("setz"), List(Variable("x"), Variable("y"))),
//        Union(Variable("setz"), Value(collection.mutable.HashSet(123,"xyz"))))).eval()
//    ClassDef("ClassB", Field("b"), Constructor(Parameters(null), Assign("b", Value(5))), EXTENDS("ClassC")).eval()
//    NewObject("ClassC", "ClassB", Variable("newObjB"), Parameters(null)).eval()
//    val op = InvokeMethod(Object("newObjB"), "newMethod" , Parameters(collection.mutable.Map("x"->1, "y"->2)))
//    println(op.eval())
//    println(ClassOperations.objectMap)

//    Scope("scopename", Scope("othername",
    //    Insert(Variable("someSetName"), List(Value("var"), Value(1), Value("somestring"))))).eval()
//    Scope("scopename", Scope("othername", Check(Variable("someSetName"),Value(1)))).eval()

//      ClassDef("B", Field("field1")).eval()
//      ClassDef("A", Field("newField"),
//        Constructor(Parameters(collection.mutable.Map[String, Any]("a"->null)), Assign("newField", Value(10))) ,
//        Method("newMethod",
//        Parameters(collection.mutable.Map("a"->1)),
//        Insert(Variable("someSetName"), List(Variable("a"), Value(1), Value("somestring")))),
//        EXTENDS("B")).eval()
//      NewObject("B", "A", Variable("newObj"), Parameters(collection.mutable.Map("a"->1))).eval()
//      InvokeMethod(Object("newObj"), "newMethod", Parameters(collection.mutable.Map("a"->10))).eval()



//        AbstractClassDef("NewAbstract", Field("abc"), AbstractMethod("AbsMethod", Parameters(collection.mutable.Map()))).eval()
//        ClassDef("BaseClass", Field("NewField"), EXTENDS("NewAbstract")).eval()

//        AbstractClassDef("NewAbstract", Field("abc"), AbstractMethod("AbsMethod", Parameters(collection.mutable.Map())),Method("Method", Parameters(collection.mutable.Map()), null) , AbstractMethod("AbsMethod1", Parameters(collection.mutable.Map()))).eval()
//        ClassDef("BaseClass", Field("NewField"), Method("AbsMethod", Parameters(collection.mutable.Map()), null), Method("AbsMethod1", Parameters(collection.mutable.Map()), null) ,EXTENDS("NewAbstract")).eval()

//        AbstractClassDef("NewAbstract", Field("abc")).eval()

//        ClassDef("A").eval()
//        ClassDef("B", EXTENDS("A")).eval()
//        ClassDef("C", EXTENDS("B")).eval()
//        ClassDef("A", EXTENDS("C")).eval()

//        AbstractClassDef("A", AbstractMethod("method1", Parameters(null))).eval()
//        ClassDef("Classs", EXTENDS("A")).eval()

//        AbstractClassDef("B", AbstractMethod("method2", Parameters(null)), Method("method1", Parameters(null), Insert(Variable("y"), List(Value(10)))), EXTENDS("A")).eval()
//        AbstractClassDef("C", AbstractMethod("method3", Parameters(null)), Method("method2", Parameters(null), null), EXTENDS("B")).eval()
//        ClassDef("D", Method("method3", Parameters(null), Insert(Variable("x"), List(Value(3)))), EXTENDS("C")).eval()
////        NewObject("A", "B", Variable("obj"), Parameters(null)).eval()
//        NewObject("C", "D", Variable("obj"), Parameters(null)).eval()
//        InvokeMethod(Object("obj"), "method1", Parameters(null)).eval()

//        AbstractClassDef("A", AbstractMethod("method1", Parameters(null))).eval()
//        InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null)), EXTENDS("A")).eval()

//        InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null))).eval()
//        AbstractClassDef("A", AbstractMethod("method1", Parameters(null)), EXTENDS("newIntr")).eval()

//        InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null))).eval()
//        InterfaceDef("newIntr1", AbstractMethod("m1", Parameters(null)), EXTENDS("newIntr")).eval()
//        ClassDef("X", Implements("newIntr1")).eval()

        InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null))).eval()
        InterfaceDef("newIntr1", AbstractMethod("m1", Parameters(null)), EXTENDS("newIntr")).eval()
        ClassDef("X", Method("m1", Parameters(null), Insert(Variable("x"), List(Value(3)))), Implements("newIntr1")).eval()
        NewObject("newIntr", "X", Variable("var"), Parameters(null)).eval()
        println(Object("var").eval())

