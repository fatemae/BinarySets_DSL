

import CONSTANTS.*
import ClassOperations.ClassOper.*
import ClassOperations.objectMap
import SetOperations.SetOper.*

import java.util
import collection.mutable
import scala.util.control

object SetOperations:
  val bindingScope: mutable.Map[String, mutable.Map[String, Any]]
    = collection.mutable.Map[String, mutable.Map[String, Any]](scopeVariable->mutable.Map[String, Any](method->mutable.Map[String, Seq[SetOperations.SetOper]](),"var"->"var"))

  val exceptionClassScope: mutable.Map[String, mutable.Map[String, Any]]
  = collection.mutable.Map[String, mutable.Map[String, Any]](scopeVariable->mutable.Map[String, Any](method->mutable.Map[String, Seq[SetOperations.SetOper]]()))




  enum SetOper:
    case Value(input: Any)
    case Variable(name: String)
    case Assign(variable: String, value: SetOper)
    case Insert(setname: Variable, objectList: List[SetOper])
    case Delete(setName: Variable, obj: SetOper)
    case Check(setName: Variable, obj: SetOper)
    case OptimizedUnion(oper: SetOper)
    case Union(setA: SetOper, setB: SetOper)
    case OptimizedIntersection(oper: SetOper)
    case Intersection(setA: SetOper, setB: SetOper)
    case OptimizedDifference(oper: SetOper)
    case Difference(setA: SetOper, setB: SetOper)
    case Symm_Difference(setA: SetOper, setB: SetOper)
    case OptimizedCProduct(oper: SetOper)
    case Cartesian_Product(setA: SetOper, setB: SetOper)
    case NewObject(staticType:String, dynamicType:String, variable: Variable, params: Parameters)
    case InvokeMethod(variable: ObjectType, mName: String, params: Parameters)
    case IfThenElse(cond: Check, thenCase: List[SetOper], elseCase: List[SetOper])
    case Macro(mName: String, operation: Seq[SetOper], isAbstract:String = null)
    case MacroEval(mName: String)
    case Scope(sName: String, oper: SetOper)
    case CatchException(variable: ObjectType, opers: SetOper*)
    case TryCatch(codeBlock: List[SetOper], catchException: List[CatchException])
    case ThrowException(className: String, variable: ObjectType)
    case PrintField(field: String)
    case ExpressionContainer(setOper: List[SetOper])

    private def findInScope(scopeName: String, objectName: String, methodName: String, variableName: String): Any = {
      val scopeList = scopeName.split('+').toList
      if(objectName==null) {
        for(l<-scopeList){
          bindingScope.getOrElse(l, None) match{
            case None =>
            case map: mutable.Map[String, Any] => map.getOrElse(field, None) match {
              case None =>
              case fields: mutable.Map[String, Any] =>
                val temp = fields.getOrElse(variableName, None)
                if(temp!=None)
                  return temp
            }
          }
        }
      }else{
        val l = scopeList.last
        if(methodName!=null){
          objectMap.getOrElse(l, None) match{
            case None => return None
            case map: mutable.Map[String, Any] => map.getOrElse(objectName, None) match {
              case objectDetails: mutable.Map[String, Any] => objectDetails.getOrElse(method, None) match {
                case methods: mutable.Map[String, Any] => methods.getOrElse(methodName, None) match {
                  case methodDetails: mutable.Map[String, Any] => methodDetails.getOrElse(methodParams, None) match {
                    case params: mutable.Map[String, Any] => if(params.getOrElse(variableName, None) !=None)
                      return params.getOrElse(variableName, None)
                    case None => return None
                  }
                  case None => return None
                }
                case None => return None
              }
              case None => return None
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


    private def checkForException(code : List[SetOper], scopeName :String, objectName :String, methodName : String): java.util.ArrayList[SetOperations.SetOper] = {
      val temp:util.ArrayList[SetOper] = util.ArrayList()
      val c = code.takeWhile {
        case thr: ThrowException =>
          temp.add(thr)
          false
        case IfThenElse(c: Check, t: List[SetOper], e: List[SetOper]) =>
          var x = false
          c.eval(scopeName, objectName, methodName) match {
            case b: Boolean => val ex: java.util.ArrayList[SetOper] = if (b) {
              checkForException(t, scopeName, objectName, methodName)
            } else {
              checkForException(e, scopeName, objectName, methodName)
            }
              temp.addAll(ex)
            case _ => x = true
          }
          temp.size() <= 0
        case TryCatch(codeBlock, catchEx) => val ex = checkForException(codeBlock, scopeName, objectName, methodName)
          temp.addAll(ex)
          temp.size() <= 0
        case p =>
          p.eval(scopeName, objectName, methodName)
          true
      }
      temp
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
//                        println("inObjectScope:"+inObjectScope)
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

    def map(f: SetOper => SetOper) : SetOper = {
      val res =
        this match {
          case expC :ExpressionContainer => this.eval() match {
            case ls: List[SetOper] => ls.foldRight(List.empty[SetOper])((x: SetOper, res: List[SetOper])=>
              val r = f(x).eval() match {
                case op: SetOper => op
                case v: Set[Any] => Value(v)
                case _ => Value("")
              }
              r :: res)
            case _ => Nil
          }
          case _ => Nil
        }
      ExpressionContainer(res)
    }

    def eval(scopeName: String = scopeVariable, objectName: String=null, methodName: String = null): SetOper|Any = {
//      bindingScope(scopeVariable)=None
      this match {
        //        To get any basic value types
        case Value(i) => i

        //        To return variables value
        case Variable(name) =>
          val x = findInScope(scopeName, objectName, methodName, name)
          if(x==None)
            Variable(name)
          else x

        case Assign(variable, value) =>
          storeVariable(scopeName, objectName, methodName, variable, value.eval(scopeName, objectName, methodName))

        //        Insert a list of values in a set, creates a set if not already present
        case Insert(setname, objectList) =>
          setname match {
            case Variable(name) =>
//              println("Inserting:" + name)
              setname.eval(scopeName, objectName, methodName) match {
                case Variable(name) =>
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
//          println("Deleting")
          setname match {
            case Variable(name) => setname.eval(scopeName, objectName, methodName) match {
              case Variable(name) => Delete(setname, obj)
              case setObj: mutable.Set[Any] =>
                setObj -= obj.eval(scopeName, objectName, methodName)
                storeVariable(scopeName, objectName, methodName, name, setObj)
              case _ => throw Exception("Not a set type")
            }
            case _ => throw Exception("Setname not of variable type")
          }

        //          Checks if a object is present in a set
        case Check(setname, obj) =>
//          println("Check")
          setname match {
            case Variable(name) => setname.eval(scopeName, objectName, methodName) match {
              case Variable(nm) => Check(setname, obj)
              case setObj: mutable.Set[Any] =>
//                println("Check returns :" + setObj.contains(obj.eval(scopeName, objectName, methodName)))
                setObj.contains(obj.eval(scopeName, objectName, methodName))
              case _ => throw Exception("Not a set type")
            }
            case _ => throw Exception("Setname not of variable type")
          }

        //        returns a union of two sets
        case Union(setA, setB) =>
//          println("Union")
          setA.eval(scopeName, objectName, methodName) match {
            case op :SetOper =>
              setB.eval(scopeName, objectName, methodName) match {
                case opB: SetOper => Union(op, opB)
                case setObjB: mutable.Set[Any] =>
                  Union(op, Value(setObjB))
                case _ => throw Exception("B is not of Set type.")
              }
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case opB: SetOper =>
                  Union(Value(setObjA),opB)
                case setObjB: mutable.Set[Any] =>
                  setObjA.union(setObjB)
                case _ => throw Exception("B is not of Set type.")
              }
            case _ => throw Exception("A is not of Set type.")
          }

        case OptimizedUnion(oper: SetOper) =>
          oper match {
            case Union(setA, setB) =>
              val x = setA.eval(scopeName, objectName, methodName)
              val y = setB.eval(scopeName, objectName, methodName)
              if(x==y) x
              else if(x==null || x==Nil || x.equals(Nil) || x.equals(Set.empty)) y
              else if(y==null || y==Nil || y.equals(Nil) || y.equals(Set.empty)) x
              else oper.eval(scopeName, objectName, methodName)
            case _ =>
          }

        //        returns intersection of two sets
        case Intersection(setA, setB) =>
//          println("Intersection")
          setA.eval(scopeName, objectName, methodName) match {
            case op: SetOper =>
              setB.eval(scopeName, objectName, methodName) match {
                case opB: SetOper => Intersection(op , opB)
                case setObjB: mutable.Set[Any] =>
                  Intersection(op, Value(setObjB))
                case _ => throw Exception("B is not of Set type.")
              }
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case opB: SetOper => Intersection(Value(setObjA), opB)
                case setObjB: mutable.Set[Any] =>
                  setObjA.intersect(setObjB)
                case _ => throw Exception("B is not of Set type.")
              }
            case _ => throw Exception("A is not of Set type.")
          }

        case OptimizedIntersection(oper: SetOper) =>
          oper match {
            case Intersection(setA, setB) =>
              val x = setA.eval(scopeName, objectName, methodName)
              val y = setB.eval(scopeName, objectName, methodName)
              if(x==y) x
              else if(x==null || x==Nil || x.equals(Nil) || x.equals(Set.empty)) Set.empty
              else if(y==null || y==Nil || y.equals(Nil) || y.equals(Set.empty)) Set.empty
              else oper.eval(scopeName, objectName, methodName)
            case _ =>
          }

        //        returns a difference of two sets
        case Difference(setA, setB) =>
//          println("Difference")
          setA.eval(scopeName, objectName, methodName) match {
            case opA: SetOper => setB.eval(scopeName, objectName, methodName) match {
              case opB: SetOper => Difference(opA, opB)
              case setObjB: mutable.Set[Any] =>
                Difference(opA, Value(setObjB))
              case _ => throw Exception("B is not of Set type.")
            }
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case opB : SetOper => Difference(Value(setObjA), opB)
                case setObjB: mutable.Set[Any] =>
                  setObjA.diff(setObjB)
                case _ => throw Exception("B is not of Set type.")
              }
            case _ => throw Exception("A is not of Set type.")
          }

        case OptimizedDifference(oper: SetOper) =>
          oper match {
            case Difference(setA, setB) =>
              val x = setA.eval(scopeName, objectName, methodName)
              val y = setB.eval(scopeName, objectName, methodName)
              if(x==y) Set.empty
              else if(x==null || x==Nil || x.equals(Nil) || x.equals(Set.empty)) y
              else if(y==null || y==Nil || y.equals(Nil) || y.equals(Set.empty)) x
              else oper.eval(scopeName, objectName, methodName)
            case _ =>
          }

        //        returns symmetric difference of two sets
        case Symm_Difference(setA, setB) =>
//          println("Symmetric Difference")
          val diff1 = Difference(setA, setB)
          val diff2 = Difference(setB, setA)
          Union(diff1, diff2).eval(scopeName, objectName, methodName)

        //        returns Cartesian Product of two sets
        case Cartesian_Product(setA, setB) =>
//          println("Cartesian Product")
          setA.eval(scopeName, objectName, methodName) match {
            case opA:SetOper => setB.eval(scopeName, objectName, methodName) match {
              case opB: SetOper => Cartesian_Product(opA, opB)
              case setObjB: mutable.Set[Any] =>
                Cartesian_Product(opA, Value(setObjB))
              case null => throw Exception("B is not of Set type.")
            }
            case setObjA: mutable.Set[Any] =>
              setB.eval(scopeName, objectName, methodName) match {
                case opB: SetOper => Cartesian_Product(Value(setObjA), opB)
                case Nil => Set.empty
                case setObjB: mutable.Set[Any] =>
                  val cross = for {
                    x <- setObjA; y <- setObjB
                  } yield (x, y)
                  cross
                case null => throw Exception("B is not of Set type.")
              }
            case null => throw Exception("A is not of Set type.")
          }

        case OptimizedCProduct(oper: SetOper) =>
          oper match {
            case Cartesian_Product(setA, setB) =>
              val x = setA.eval(scopeName, objectName, methodName)
              val y = setB.eval(scopeName, objectName, methodName)
              if(x==null || x==Nil || x.equals(Nil) || x.equals(Set.empty)) Set.empty
              else if(y==null || y==Nil || y.equals(Nil) || y.equals(Set.empty)) Set.empty
              else oper.eval(scopeName, objectName, methodName)
            case _ =>
          }

        //        creates a named operation
        case Macro(name, operation, isAbstract) =>
//          println("Macro")
          storeMacro(scopeName, name, isAbstract, operation)

        //        Evaluates a Macro
        case MacroEval(name) =>
//          println("Substituting Macro")
          val seq: Seq[SetOper] = findMacroInScope(scopeName, name)
          if seq != null then
            for (x <- seq) {
              x.eval(scopeName, objectName, methodName)
            }
          else seq

        //        Case to run scope
        case Scope(sName, oper) =>
//          println("Scope:" + sName)
          val map = bindingScope.getOrElse(sName, None)
          map match {
            case None => bindingScope(sName) = mutable.Map[String, Any]()
            case scopeMap: mutable.Map[String, Any] =>
          }
          oper.eval(scopeName + connector + sName)

        case NewObject(staticType: String, dynamicType: String, variable: Variable, params: Parameters) =>
//          println("NewObject")
          val scopeNm = scopeName.split(connector.charAt(0)).toList.last
          val classType = ClassOperations.getClassInfo(dynamicType, typeModifier)
          if (bindingScope.getOrElse(staticType, None) == None || bindingScope.getOrElse(dynamicType, None) == None)
            throw Exception("Class Not defined")

          else if (classType != null && !classType.equals(exceptionType)) {
            throw Exception("cannot create object of abstract class or Interface")
          } else {
            variable match {
              case Variable(v) =>

                val classTemp = if (staticType.equals(dynamicType))
                  ClassOperations.cloneClassMap(bindingScope.getOrElse(staticType, null))
                else ClassOperations.cloneInheritedClassMap(staticType, dynamicType, bindingScope.getOrElse(staticType, null), bindingScope.getOrElse(dynamicType, null))
                classTemp(CONSTANTS.staticType) = staticType
                classTemp(CONSTANTS.dynamicType) = dynamicType
                objectMap.getOrElse(scopeNm, None) match {
                  //put static and dynamic type
                  case map: collection.mutable.Map[String, Any] => map(v) = classTemp
                  case None => objectMap(scopeNm) = mutable.Map[String, Any](v -> classTemp)
                }
                ClassOperations.runMethod(scopeNm, v, staticType, params)
              case _ =>
            }
          }

        case InvokeMethod(variable: ObjectType, mName: String, params: Parameters) =>
//          println("InvokeMethod")
          val scopeNm = scopeName.split(connector.charAt(0)).toList.last
          //          println(variable)
          variable match {
            case ObjectType(name) =>
              variable.eval(scopeNm) match {
                case map =>
                  ClassOperations.runMethod(scopeNm, name, mName, params)
                case None => throw Exception("object not defined")
              }
            case null =>
          }

        case PrintField(fieldName: String) =>
          val s = scopeName.split(connector.charAt(0)).toList.last
          objectMap.getOrElse(s, null) match {
            case objects: mutable.Map[String, Any] => objects.getOrElse(objectName, null) match {
              case objectDetails: mutable.Map[String, Any] => objectDetails.getOrElse(field, null) match {
                case fields: mutable.Map[String, Any] => 
                  println(fieldName + ": " + fields.getOrElse(fieldName, null))
                case _ => throw Exception("Object do not have fields")
              }
              case _ => throw Exception("no such object in scope")
            }
            case _ => throw Exception("no such scope defined")
          }

        case IfThenElse(cond: Check, thenCase: List[SetOper], elseCase: List[SetOper]) =>
          cond.eval(scopeName, objectName, methodName) match {
            case op: Check => IfThenElse(op, thenCase, elseCase)
            case c: Boolean => if (c) {
              val x = checkForException(thenCase, scopeName, objectName, methodName)
//              println(x)
            } else {
              checkForException(elseCase, scopeName, objectName, methodName)
            }
            case _ => null
          }

        case CatchException(variable: ObjectType, opers: _*) =>
          variable match {
            case o: ObjectType =>
              for (oper <- opers) {
                oper.eval(scopeName)
              }
            case _ => throw Exception("not an object of exception")
          }

        case ThrowException(className: String, variable: ObjectType) =>
//          println("Throw Exception")
          mutable.Map[String, Any]("class" -> className, "object" -> variable)

        case TryCatch(codeBlock: List[SetOper], catchExceptions: List[CatchException]) =>
//          println("TryCatch")
          var exceptionCaught = false
          val x = checkForException(codeBlock, scopeName, objectName, methodName)
          if (x.size() > 0) {
            for(catchException <- catchExceptions)
            catchException match {
              case CatchException(obj: ObjectType, opers: _*) =>
                obj.eval(scopeName.split(connector.charAt(0)).toList.last) match {
                  case object1: mutable.Map[String, Any] =>
                    object1.getOrElse(staticType, None) match {
                      case str: String => x.get(0) match {
                        case ThrowException(c: String, v: ObjectType) =>
                          if (str.equals(c)) {
                            exceptionCaught = true
                            catchException.eval(scopeName, objectName, methodName)
                          }
                        case _ =>
                      }
                      case _ =>
                    }
                  case _ =>
                }
              case _ =>
            }
            if(!exceptionCaught)
              throw Error("Uncaught Exception")
          }

        case ExpressionContainer(list: List[SetOper]) =>
          list


      }

    }
