package CS474

import CS474.SetOperations.SetOper.{Difference, Intersection, Variable}

import scala.collection.mutable

object SetOperations :
  type BasicType= Any
  private var bindingScope: mutable.Map[String, BasicType] = collection.mutable.Map[String, BasicType]()
  enum SetOper:
    case Value(input: BasicType)
    case Variable(name: String)
    case Insert(setname: Variable, objectList: List[SetOper])
    case Delete(setName: Variable, obj: SetOper)
    case Check(setName: Variable, obj:SetOper)
    case Union(setA:SetOper, setB:SetOper)
    case Intersection(setA:SetOper, setB:SetOper)
    case Difference(setA:SetOper, setB:SetOper)
    case Symm_Difference(setA:SetOper, setB:SetOper)
    case Cartesian_Product(setA:SetOper, setB:SetOper)
//    private var setBindingScope: mutable.Map[Variable, BasicType] = collection.mutable.Map[Variable, BasicType]()
    def eval: BasicType = {
      this match {
        case Value(i) => i
        case Variable(name) => bindingScope.getOrElse(name,None)

        case Insert(setname, objectList) => println("Inserting")
          setname match{
            case Variable(name) => setname.eval match {
              case None =>
                var setObj: mutable.Set[BasicType] = mutable.Set[BasicType]()
                for (obj <- objectList) {
                  setObj += obj.eval
                }
                bindingScope(name) = setObj
              case setObj: mutable.HashSet[BasicType] =>
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
              case setObj: mutable.HashSet[BasicType] =>
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
              case setObj: mutable.HashSet[BasicType] =>
                setObj.contains(obj.eval)
              case _ => println("Not a set type")
            }
            case _ => println("Setname not of variable type")
          }

        case Union(setA, setB) => println("Union")
          setA.eval match {
            case None => setB.eval
            case setObjA: mutable.HashSet[BasicType] =>
              setB.eval match {
                case None => setA.eval
                case setObjB: mutable.HashSet[BasicType] =>
                  setObjA.union(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

        case Intersection(setA, setB) => println("Intersection")
          setA.eval match {
            case None => Set.empty
            case setObjA: mutable.HashSet[BasicType] =>
              setB.eval match {
                case None => Set.empty
                case setObjB: mutable.HashSet[BasicType] =>
                  setObjA.intersect(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

        case Difference(setA, setB) => println("Difference")
          setA.eval match {
            case None => setB.eval
            case setObjA: mutable.HashSet[BasicType] =>
              setB.eval match {
                case None => setA.eval
                case setObjB: mutable.HashSet[BasicType] =>
                  setObjA.diff(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

        case Symm_Difference(setA, setB) => println("Symmetric Difference")
         val diff1 = Difference(setA, setB)
         val diff2 = Difference(setB, setA)
         Union(diff1,diff2).eval

        case Cartesian_Product(setA, setB) => println("Cartesian Product")
          setA.eval match {
            case None => setB.eval
            case setObjA: mutable.HashSet[BasicType] =>
              setB.eval match {
                case None => setA.eval
                case setObjB: mutable.HashSet[BasicType] =>
                  val cross = for {
                    x <- setObjA; y <- setObjB
                  } yield (x, y)
                  cross
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }
      }

    }
  @main def runSetExp: Unit =
    import SetOper.*
    Insert(Variable("A"),List(Value("set"),Value("34"), Value(1234))).eval
    Insert(Variable("B"),List(Value("xyz"),Value("hello there"), Value("34"))).eval
//    Delete(Variable("A"),Value(1234)).eval
    val thirdExpr = Check(Variable("A"), Value(1234)).eval
    val D = Cartesian_Product(Variable("A"),Variable("C"))
    println(D.eval)

