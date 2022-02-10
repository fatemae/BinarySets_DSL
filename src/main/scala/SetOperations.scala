package scala

import collection.mutable

object SetOperations:
  type BasicType = Any
  val scopeVariable = "<scope>"
//  For the outermost scope
  private val bindingScope: mutable.Map[String, BasicType] = collection.mutable.Map[String, BasicType](scopeVariable->None, "var"->"var")
  private val macroMap: mutable.Map[String, SetOper] = collection.mutable.Map[String, SetOper]()
//  for other scopes
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

    // To deepcopy Map
    private def clone(bindingScope: mutable.Map[String, BasicType]): mutable.Map[String, BasicType]  ={
      val newBindingScope = mutable.Map[String, BasicType]()
      for((k,v)<- bindingScope){
        v match{
          case map:mutable.Map[String, BasicType] => newBindingScope(k) = clone(map)
          case set:mutable.Set[BasicType] => newBindingScope(k) = set.clone();
          case _ => newBindingScope(k)=v
        }
      }
      newBindingScope
    }

//    To store variable in respective maps
    private def storeVariable(name: String, obj: BasicType) :Unit = {
      bindingScope.getOrElse(scopeVariable, None) match {
        case None => bindingScope(name) = obj
        case scope:String => scopeMap.getOrElse(scope, None) match{
          case None =>scopeMap(scope)=mutable.Map(name->obj)
          case map:mutable.Map[String,BasicType] => map(name)=obj
        }
      }
    }

    def eval: BasicType = {
      bindingScope(scopeVariable)=None
      this match {
//        To get any basic value types
        case Value(i) => i

//        To return variables value
        case Variable(name) => bindingScope.getOrElse(scopeVariable, None) match {
          case None => bindingScope.getOrElse(name, None)
          case scope:String => scopeMap.getOrElse(scope, None) match {
            case None=> None
            case map: mutable.Map[String, BasicType] => map.getOrElse(name,None)
          }
        }

//        Insert a list of values in a set, creates a set if not already present
        case Insert(setname, objectList) => println("Inserting")
          setname match {
            case Variable(name) => setname.eval match {
              case None =>
                val setObj: mutable.Set[BasicType] = mutable.Set[BasicType]()
                for (obj <- objectList) {
                  setObj += obj.eval
                }
                storeVariable(name,setObj)
              case setObj: mutable.Set[BasicType] =>
                for (obj <- objectList) {
                  setObj += obj.eval
                }
              case null => println("Not a set type")
            }
            case null => println("Setname not of variable type")
          }

//        Deletes an object from a set
        case Delete(setname: Variable, obj: SetOper) =>
          println("Deleting")
          setname match {
            case Variable(name) => setname.eval match {
              case None => "No Such set variable defined"
              case setObj: mutable.Set[BasicType] =>
                setObj -= obj.eval
                storeVariable(name,setObj)
              case null => println("Not a set type")
            }
            case null => println("Setname not of variable type")
          }

//          Checks if a object is present in a set
        case Check(setname, obj) => println("Check")
          setname match {
            case Variable(name) => setname.eval match {
              case None => "No Such set variable defined"
              case setObj: mutable.Set[BasicType] =>
                println("Check returns :"+setObj.contains(obj.eval))
                setObj.contains(obj.eval)
              case null => println("Not a set type")
            }
            case null => println("Setname not of variable type")
          }

//        returns a union of two sets
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

//        returns intersection of two sets
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

//        returns a difference of two sets
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

//        returns symmetric difference of two sets
        case Symm_Difference(setA, setB) => println("Symmetric Difference")
          val diff1 = Difference(setA, setB)
          val diff2 = Difference(setB, setA)
          Union(diff1, diff2).eval

//        returns Cartesian Product of two sets
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

//        creates a named operation
        case Macro(name, operation) => println("Macro")
          macroMap(name) = operation

//        Evaluates a Macro
        case MacroEval(name) => println("Substituting Macro")
          val x: SetOper = macroMap.getOrElse(name, null)
          if x != null then
            x.eval
          else x

//        Case to run scope
        case Scope(sName, oper) => println("Scope:"+sName)
          bindingScope.getOrElse(scopeVariable, None) match {
            case None => scopeMap(sName)=clone(bindingScope)
            case scope:String => scopeMap.getOrElse(scope, None) match{
              case None =>
              case map: mutable.Map[String, BasicType] => scopeMap(sName)= clone(map)
            }
          }
          bindingScope(scopeVariable)=sName
          oper.eval

      }

    }
