package CS474

import CS474.SetOperations.SetOper.Variable

object SetOperations :
  type BasicType= Any
  private var bindingScope: scala.collection.mutable.Map[String, BasicType] = collection.mutable.Map[String, BasicType]()
  enum SetOper:
    case Value(input: BasicType)
    case Variable(name: String)
    case Insert(setname: Variable, objectList: List[SetOper])
    case Delete(setName: Variable, obj: SetOper)
    case Check(setName: Variable, obj:SetOper)
    case Union(setA:SetOper, setB:SetOper)
//    private var setBindingScope: scala.collection.mutable.Map[Variable, BasicType] = collection.mutable.Map[Variable, BasicType]()
    def eval: BasicType = {
      this match {
        case Value(i) => i
        case Variable(name) => bindingScope.getOrElse(name,None)

        case Insert(setname, objectList) =>
          setname match{
            case Variable(name) => setname.eval match {
              case None =>
                var setObj: scala.collection.mutable.Set[BasicType] = scala.collection.mutable.Set[BasicType]()
                for (obj <- objectList) {
                  setObj += obj.eval
                }
                bindingScope(name) = setObj
              case setObj: scala.collection.mutable.HashSet[BasicType] =>
                for (obj <- objectList) {
                  setObj += obj.eval
                }
              case _ => println("Not a set type")
            }
            case _ => println("Setname not of variable type")
          }

        case Delete(setname: Variable, obj: SetOper) =>
          println("Deleting")
          setname match{
            case Variable(name) => setname.eval match {
              case None => "No Such set variable defined"
              case setObj: scala.collection.mutable.HashSet[BasicType] =>
                setObj -= obj.eval
                bindingScope(name) = setObj
              case _ => println("Not a set type")
            }
            case _ => println("Setname not of variable type")
          }

        case Check(setname, obj) => println("Check")
          setname match {
            case Variable(name) => setname.eval match {
              case None => "No Such set variable defined"
              case setObj: scala.collection.mutable.HashSet[BasicType] =>
                setObj.contains(obj.eval)
              case _ => println("Not a set type")
            }
            case _ => println("Setname not of variable type")
          }

        case Union(setA, setB) => println("Union")
          setA.eval match {
            case None => setB
            case setObjA: scala.collection.mutable.HashSet[BasicType] =>
              setB.eval match {
                case None => setA
                case setObjB: scala.collection.mutable.HashSet[BasicType] =>
                  setObjA.union(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

      }

    }
  @main def runSetExp: Unit =
    import SetOper.*
    Insert(Variable("A"),List(Value("set"),Value("1234"), Value(1234))).eval
    Insert(Variable("B"),List(Value("xyz"),Value("hello there"), Value(45))).eval
    Delete(Variable("A"),Value(1234)).eval
    val thirdExpr = Check(Variable("A"), Value(1234)).eval
//    var 4th = Union()
    println(thirdExpr)

