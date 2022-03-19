
object CONSTANTS {
  val typeModifier = "<type>"
  val abstractType = "<abstract>"
  val interfaceType = "<interface>"
  val overriddenType = "<overriden>"
  val parentClass = "<parentClass>"
  val implementsClass = "<implements>"
  val method = "<method>"
  val field = "<field>"
  val methodBody = "<methodBody>"
  val methodParams = "<methodParams>"
  val staticType = "<staticType>"
  val dynamicType = "<dynamicType>"
  val scopeVariable = "<parentScope>"
  val connector = "+"
}

//Can a class/interface inherit from itself? No
//Can an interface inherit from an abstract class with all pure methods? No
// Can an interface implement another interface? No
//Can a class implement two or more different interfaces that declare methods with exactly the same signatures? yes
//Can an abstract class inherit from another abstract class and implement interfaces
// where all interfaces and the abstract class have methods with the same signatures? yes
//  Can an abstract class implement interfaces? yes
//  Can a class implement two or more interfaces that have methods whose signatures differ only in return types? No
//  Can an abstract class inherit from a concrete class? yes
//Can an abstract class/interface be instantiated as anonymous concrete classes?


//bindingscope - Map(className -> Map(
//  fieldName->Set(), <method> -> Map(
//    methodName+paramSize->Map(
//      <methodBody>-> Seq(), <methodParams>->Map(), <parent>->parentClassName
//    )
//  )
//))

//objectMap - Map(scopeName -> Map(
//  objectName -> Map(
//    fieldName->Set(), <method> -> Map(
//      methodName+paramSize->Map(
//        <methodBody>-> Seq(), <methodParams>->Map()
//      )
//    )
//  )
//))

//binding:HashMap(
//  A -> HashMap(
//    <method> -> HashMap(
//      newMethod+1 -> HashMap(
//        <methodBody> -> ArraySeq(Insert(Variable(someSetName),List(Variable(a), Value(1), Value(somestring)))),
//        <methodParams> -> HashMap(a -> 1)),
//      A+1 -> HashMap(
//        <methodBody> -> ArraySeq(Assign(newField,Value(10))),
//        <methodParams> -> HashMap(a -> null)
//      )
//    ),
//    <field> -> HashMap(newField -> HashSet()),
//    <parent> -> B),
//  B -> HashMap(
//    <field> -> HashMap(field1 -> HashSet()),
//    <parent> -> null),
//  <parentScope> -> HashMap(<method> -> HashMap(), var -> var))
