package scala

import collection.mutable

object SetOperations:
  type BasicType = Any
  private val bindingScope: mutable.Map[String, BasicType] = collection.mutable.Map[String, BasicType]()
  private val macroMap: mutable.Map[String, SetOper] = collection.mutable.Map[String, SetOper]()
  private val scopeMap: mutable.Map[String, mutable.Map[String, BasicType]] = collection.mutable.Map[String, mutable.Map[String, BasicType]]()

  enum SetOper:
    case Value(input: BasicType)
    case Variable(name: String)
    case Insert(setname: Variable, objectList: List[SetOper])
    case Delete(setName: Variable, obj: SetOper)
    case Check(setName: Variable, obj: SetOper)
    case Union(setA: SetOper, setB: SetOper)
    case Intersection(setA: SetOper, setB: SetOper)
    case Difference(setA: SetOper, setB: SetOper)
    case Symm_Difference(setA: SetOper, setB: SetOper)
    case Cartesian_Product(setA: SetOper, setB: SetOper)
    case Macro(mName: String, operation: SetOper)
    case MacroEval(mName: String)
    case Scope(sName: String, oper: SetOper)


    def eval: BasicType = {
      this match {
        case Value(i) => i
        case Variable(name) => bindingScope.getOrElse(name, None)

        case Insert(setname, objectList) => println("Inserting")
          setname match {
            case Variable(name) => setname.eval match {
              case None =>
                val setObj: mutable.Set[BasicType] = mutable.Set[BasicType]()
                for (obj <- objectList) {
                  setObj += obj.eval
                }
                bindingScope(name) = setObj
              case setObj: mutable.Set[BasicType] =>
                for (obj <- objectList) {
                  setObj += obj.eval
                }
              case null => println("Not a set type")
            }
            case null => println("Setname not of variable type")
          }

        case Delete(setname: Variable, obj: SetOper) =>
          println("Deleting")
          setname match {
            case Variable(name) => setname.eval match {
              case None => "No Such set variable defined"
              case setObj: mutable.Set[BasicType] =>
                setObj -= obj.eval
                bindingScope(name) = setObj
              case null => println("Not a set type")
            }
            case null => println("Setname not of variable type")
          }

        case Check(setname, obj) => println("Check")
          setname match {
            case Variable(name) => setname.eval match {
              case None => "No Such set variable defined"
              case setObj: mutable.Set[BasicType] =>
                setObj.contains(obj.eval)
              case null => println("Not a set type")
            }
            case null => println("Setname not of variable type")
          }

        case Union(setA, setB) => println("Union")
          setA.eval match {
            case None => setB.eval
            case setObjA: mutable.Set[BasicType] =>
              setB.eval match {
                case None => setA.eval
                case setObjB: mutable.Set[BasicType] =>
                  setObjA.union(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

        case Intersection(setA, setB) => println("Intersection")
          setA.eval match {
            case None => Set.empty
            case setObjA: mutable.Set[BasicType] =>
              setB.eval match {
                case None => Set.empty
                case setObjB: mutable.Set[BasicType] =>
                  setObjA.intersect(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

        case Difference(setA, setB) => println("Difference")
          setA.eval match {
            case None => setB.eval
            case setObjA: mutable.Set[BasicType] =>
              setB.eval match {
                case None => setA.eval
                case setObjB: mutable.Set[BasicType] =>
                  setObjA.diff(setObjB)
                case _ => println("B is not of Set type.")
              }
            case _ => println("A is not of Set type.")
          }

        case Symm_Difference(setA, setB) => println("Symmetric Difference")
          val diff1 = Difference(setA, setB)
          val diff2 = Difference(setB, setA)
          Union(diff1, diff2).eval

        case Cartesian_Product(setA, setB) => println("Cartesian Product")
          setA.eval match {
            case None => setB.eval
            case setObjA: mutable.Set[BasicType] =>
              setB.eval match {
                case None => setA.eval
                case setObjB: mutable.Set[BasicType] =>
                  val cross = for {
                    x <- setObjA; y <- setObjB
                  } yield (x, y)
                  cross
                case null => println("B is not of Set type.")
              }
            case null => println("A is not of Set type.")
          }

        case Macro(name, operation) => println("Macro")
          macroMap(name) = operation

        case MacroEval(name) => println("Substituting Macro")
          val x: SetOper = macroMap.getOrElse(name, null)
          if x != null then
            x.eval
          else x

        case Scope(sName, oper) => println("Scope:"+sName)
          scopeMap(sName)=bindingScope

      }

    }
