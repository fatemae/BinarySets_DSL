

import CONSTANTS.*
import ClassOperations.objectMap

import javax.management.ObjectName
import collection.mutable
import scala.util.control

object SetOperations:
  val bindingScope: mutable.Map[String, mutable.Map[String, Any]]
    = collection.mutable.Map[String, mutable.Map[String, Any]](scopeVariable->mutable.Map[String, Any](method->mutable.Map[String, Seq[SetOperations.SetOper]](),"var"->"var"))

  enum SetOper:
    case Value(input: Any)
    case Variable(name: String)
    case Assign(variable: String, value: SetOper)
    case Insert(setname: Variable, objectList: List[SetOper])
    case Delete(setName: Variable, obj: SetOper)
    case Check(setName: Variable, obj: SetOper)
    case Union(setA: SetOper, setB: SetOper)
    case Intersection(setA: SetOper, setB: SetOper)
    case Difference(setA: SetOper, setB: SetOper)
    case Symm_Difference(setA: SetOper, setB: SetOper)
    case Cartesian_Product(setA: SetOper, setB: SetOper)
    case Macro(mName: String, operation: Seq[SetOper], isAbstract:String = null)
    case MacroEval(mName: String)
    case Scope(sName: String, oper: SetOper)

    private def findInScope(scopeName: String, objectName: String, methodName: String, variableName: String): Any = {
      val scopeList = scopeName.split('+').toList
      if(objectName==null) {
        for(l<-scopeList){
          bindingScope.getOrElse(l, None) match{
            case None =>
            case map: mutable.Map[String, Any] => map.getOrElse(field, None) match {
              case None =>
              case fields: mutable.Map[String, Any] => if(fields.getOrElse(variableName, None) !=None)
                return fields.getOrElse(variableName, None)
            }
          }
        }
      }else{
        //objectMap - Map(scopeName -> Map(
        //  objectName -> Map(
        //    fieldName->Set(), <method> -> Map(
        //      methodName+paramSize->Map(
        //        <methodBody>-> Seq(), <methodParams>->Map()
        //      )
        //    )
        val l = scopeList.last
        if(methodName!=null){
          objectMap.getOrElse(l, None) match{
            case None =>
            case map: mutable.Map[String, Any] => map.getOrElse(objectName, None) match {
              case objectDetails: mutable.Map[String, Any] => objectDetails.getOrElse(method, None) match {
                case methods: mutable.Map[String, Any] => methods.getOrElse(methodName, None) match {
                  case methodDetails: mutable.Map[String, Any] => methodDetails.getOrElse(methodParams, None) match {
                    case params: mutable.Map[String, Any] => if(params.getOrElse(variableName, None) !=None)
                      return params.getOrElse(variableName, None)
                    case None =>
                  }
                  case None =>
                }
                case None =>
              }
              case None =>
            }
          }
        }
        objectMap.getOrElse(l, None) match{
          case None =>
          case map: mutable.Map[String, Any] => map.getOrElse(objectName, None) match {
            case objectDetails: mutable.Map[String, Any] => objectDetails.getOrElse(field, None) match {
              case fields: mutable.Map[String, Any] => if(fields.getOrElse(variableName, None) !=None)
                return fields.getOrElse(variableName, None)
              case None =>
            }
            case None =>
          }
        }

      }
      None
    }

    private def findMacroInScope(scopeName: String, macroName: String): Seq[SetOperations.SetOper] = {
      val scopeList = scopeName.split('+').toList
      for(l<-scopeList){
        bindingScope.getOrElse(l, None) match{
          case None =>
          case map: mutable.Map[String, Any] =>
            map.getOrElse(method, None) match{
              case None =>
              case macros: mutable.Map[String, Seq[SetOperations.SetOper]] =>
                macros.getOrElse(macroName, None) match {
                  case macroMp: mutable.Map[String, Any] => macroMp.getOrElse(methodBody, null) match {
                    case seq: Seq[SetOper] => return seq
                    case _ => return null
                  }
                  case None =>
                }
            }
        }
      }
      null
    }

    def checkVariableInObjectFields(scopeName: String, objectName: String, methodName: String, variableName: String, obj: Any, createNew: Boolean):Boolean = {
      val scopeList = scopeName.split('+').toList
      objectMap.getOrElse(scopeList.last, None) match{
        case None => false
        case objects: mutable.Map[String, Any] => objects.getOrElse(objectName, None) match {
          case objectDetails: mutable.Map[String, Any] => objectDetails.getOrElse(field, None) match {
            case fields: mutable.Map[String, Any] =>

                fields.getOrElse(variableName, None) match {
                  case value => if(createNew) { fields(variableName)= obj}
                    true
                  case None => false
                }

            case None => false
          }
          case None => false
        }
      }
    }

//    To store variable in respective maps
    private def storeVariable(scopeName: String, objectName: String, methodName: String, variableName: String, obj: Any) :Unit = {
      val scopeList = scopeName.split('+').toList
      var inObjectScope = false
      if(objectName==null){
        bindingScope.getOrElse(scopeList.last, None) match {
          case None => bindingScope(scopeList.last) = mutable.Map[String, Any](field-> mutable.Map[String, Any](variableName->obj))
          case map:mutable.Map[String, Any] => map.getOrElse(field, None) match {
            case None => map(field)=mutable.Map(variableName->obj)
            case fields:mutable.Map[String, Any] => fields(variableName)=obj
          }
        }
      }else{
        if(methodName!=null){
          objectMap.getOrElse(scopeList.last, None) match{
            case None =>
            case objects: mutable.Map[String, Any] => objects.getOrElse(objectName, None) match {
              case objectDetails: mutable.Map[String, Any] => objectDetails.getOrElse(method, None) match {
                case methods: mutable.Map[String, Any] => methods.getOrElse(methodName, None) match {
                  case methodDetails: mutable.Map[String, Any] => methodDetails.getOrElse(methodParams, None) match {
                    case params: mutable.Map[String, Any] =>
                      if(params.getOrElse(variableName, None)==None)
                        inObjectScope = checkVariableInObjectFields(scopeName, objectName, methodName, variableName, obj, false)
                        println("inObjectScope:"+inObjectScope)
                        if(!inObjectScope) {
                          params(variableName) = obj
                          return
                        }
                    case None =>
                  }
                  case None =>
                }
                case None =>
              }
              case None =>
            }
          }
        }
        checkVariableInObjectFields(scopeName, objectName, methodName, variableName, obj, true)
      }
    }

    private def storeMacro(scopeName: String, macroName: String, isAbstract:String, macroObj: Seq[SetOper]) :Unit = {
      val classNm = scopeName.split('+').toList.last
      bindingScope.getOrElse(classNm, None) match {
        case None => bindingScope(classNm) = mutable.Map[String, Any](method->mutable.Map[String, Any](macroName->mutable.Map[String, Any](typeModifier->isAbstract ,methodBody -> macroObj)))
        case map:mutable.Map[String, Any] => map.getOrElse(method, None) match {
          case None => map(method)=mutable.Map(macroName->mutable.Map[String, Any](typeModifier->isAbstract, methodBody -> macroObj))
          case macros: mutable.Map[String, Any] => macros(macroName)=mutable.Map[String, Any](typeModifier->isAbstract, methodBody -> macroObj)
        }
      }
    }

    def eval(scopeName: String = scopeVariable, objectName: String=null, methodName: String = null): Any = {
//      bindingScope(scopeVariable)=None
      this match {
//        To get any basic value types
        case Value(i) => i

//        To return variables value
        case Variable(name) =>
          findInScope(scopeName, objectName, methodName, name)

        case Assign(variable, value) =>
          storeVariable(scopeVariable, objectName, methodName, variable, value.eval(scopeName, objectName, methodName) )

//        Insert a list of values in a set, creates a set if not already present
        case Insert(setname, objectList) => println("Inserting")
          setname match {
            case Variable(name) =>
              setname.eval(scopeName, objectName, methodName) match {
              case None =>
                val setObj: mutable.Set[Any] = mutable.Set[Any]()
                for (obj <- objectList) {
                  setObj += obj.eval(scopeName, objectName, methodName)
                }
                storeVariable(scopeName, objectName, methodName, name, setObj)
              case setObj: mutable.Set[Any] =>
                for (obj <- objectList) {
                  setObj += obj.eval(scopeName, objectName, methodName)
                }
              case _ => throw Exception("Not a set type")
            }
            case _ => throw Exception("Setname not of variable type")
          }

//        Deletes an object from a set
        case Delete(setname: Variable, obj: SetOper) =>
          println("Deleting")
          setname match {
            case Variable(name) => setname.eval(scopeName, objectName, methodName) match {
              case None => "No Such set variable defined"
              case setObj: mutable.Set[Any] =>
                setObj -= obj.eval(scopeName, objectName, methodName)
                storeVariable(scopeName, objectName, methodName, name, setObj)
              case _ => throw Exception("Not a set type")
            }
            case _ => throw Exception("Setname not of variable type")
          }

//          Checks if a object is present in a set
        case Check(setname, obj) => println("Check")
          setname match {
            case Variable(name) => setname.eval(scopeName, objectName, methodName) match {
              case None => throw Exception("No Such set variable defined")
              case setObj: mutable.Set[Any] =>
                println("Check returns :"+setObj.contains(obj.eval(scopeName, objectName, methodName)))
                setObj.contains(obj.eval(scopeName, objectName, methodName))
              case _ => throw Exception("Not a set type")
            }
            case _ => throw Exception("Setname not of variable type")
          }

//        returns a union of two sets
        case Union(setA, setB) => println("Union")
          setA.eval(scopeName, objectName, methodName) match {
            case None => setB.eval(scopeName, objectName, methodName)
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case None => setA.eval(scopeName, objectName, methodName)
                case setObjB: mutable.Set[Any] =>
                  setObjA.union(setObjB)
                case _ => throw Exception("B is not of Set type.")
              }
            case _ => throw Exception("A is not of Set type.")
          }

//        returns intersection of two sets
        case Intersection(setA, setB) => println("Intersection")
          setA.eval(scopeName, objectName, methodName) match {
            case None => Set.empty
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case None => Set.empty
                case setObjB: mutable.Set[Any] =>
                  setObjA.intersect(setObjB)
                case _ => throw Exception("B is not of Set type.")
              }
            case _ => throw Exception("A is not of Set type.")
          }

//        returns a difference of two sets
        case Difference(setA, setB) => println("Difference")
          setA.eval(scopeName, objectName, methodName) match {
            case None => setB.eval(scopeName, objectName, methodName)
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case None => setA.eval(scopeName, objectName, methodName)
                case setObjB: mutable.Set[Any] =>
                  setObjA.diff(setObjB)
                case _ => throw Exception("B is not of Set type.")
              }
            case _ => throw Exception("A is not of Set type.")
          }

//        returns symmetric difference of two sets
        case Symm_Difference(setA, setB) => println("Symmetric Difference")
          val diff1 = Difference(setA, setB)
          val diff2 = Difference(setB, setA)
          Union(diff1, diff2).eval(scopeName, objectName, methodName)

//        returns Cartesian Product of two sets
        case Cartesian_Product(setA, setB) => println("Cartesian Product")
          setA.eval(scopeName, objectName, methodName) match {
            case None => setB.eval(scopeName, objectName, methodName)
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case None => setA.eval(scopeName, objectName, methodName)
                case setObjB: mutable.Set[Any] =>
                  val cross = for {
                    x <- setObjA; y <- setObjB
                  } yield (x, y)
                  cross
                case null => throw Exception("B is not of Set type.")
              }
            case null => throw Exception("A is not of Set type.")
          }

//        creates a named operation
        case Macro(name, operation, isAbstract) => println("Macro")
          storeMacro(scopeName, name, isAbstract, operation)

//        Evaluates a Macro
        case MacroEval(name) => println("Substituting Macro")
          val seq: Seq[SetOper] = findMacroInScope(scopeName, name)
          if seq != null then
            for(x <- seq) {
              x.eval(scopeName, objectName, methodName)
            }
          else seq

//        Case to run scope
        case Scope(sName, oper) => println("Scope:"+sName)
          val map = bindingScope.getOrElse(sName,None)
          map match {
            case None => bindingScope(sName)=mutable.Map[String, Any]()
            case scopeMap: mutable.Map[String, Any] =>
          }
          oper.eval(scopeName+connector+sName)
      }

    }
