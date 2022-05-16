
import ClassOperations.ClassOper.*
import SetOperations.*
import SetOperations.SetOper.*
import CONSTANTS.{method, *}
import util.control.Breaks._
import java.util.*
import scala.collection.immutable.ArraySeq
import scala.collection.mutable

object ClassOperations :
  val objectMap: mutable.Map[String, mutable.Map[String, Any]] =
    collection.mutable.Map[String, mutable.Map[String, Any]]()

  enum ClassOper:
    case ClassDef(cName: String, oper:ClassOper*)
    case EXTENDS(cls: String)
    case Field(fName: String)
    case Parameters(param: mutable.Map[String, Any])
    case Constructor(params: Parameters, opers : SetOper*)
    case Method(mName: String, params: Parameters, opers: SetOper*)
    case NewObject(staticType:String, dynamicType:String, variable: Variable, params: Parameters)
    case Object(name: String)
    case InvokeMethod(variable: Object, mName: String, params: Parameters)
    case AbstractClassDef(cName: String, classOper: ClassOper*)
    case AbstractMethod(mName:String, params:Parameters)
    case Implements(interface: String)
    case InterfaceDef(name: String , interfaceOper: ClassOper*)

    private def combineMaps(newParams: Parameters, orignalParams: mutable.Map[String, Any]): mutable.Map[String, Any] = {
      newParams match {
        case Parameters(p : mutable.Map[String, Any]) =>
          for {
            (k,v) <- orignalParams
            lv     <- p.get(k)
          } yield (k,lv)
        case _ => orignalParams
      }

    }

    /**
     * @param cName: String -  class name
     * @param classType: String - type like interface, abstract or normal class
     * @param scopeName: String - class Scope
     * @param opers: Seq[ClassOper] - all class operations
     * stores the class in binding scope*/
    private def defineClass(cName: String, classType: String, scopeName:String, opers:Seq[ClassOper]):Unit ={
      bindingScope(cName)=mutable.Map[String, Any](parentClass->null, implementsClass->null, typeModifier->classType)
      for(oper<-opers){
        oper.eval(scopeName)
      }
    }

    /**
     * @param cls: String - class name
     * @return ArrayList of all classes in the inheritance hierarchy*/
    private def getParents(cls: String, parentType: String): ArrayList[String] ={
      val parents = ArrayList[String]()
      var parent = cls
      while(parent!=null){
        parents.add(parent)
        parent = getClassInfo(parent, parentType)
      }
      Collections.reverse(parents)
      parents
    }

    /**
     * @param cls: String - class name
     * @return ArrayList of all abstract methods that are not implemented in the inheritance hierarchy*/
    private def getAbstractMethods(cls:String, parentType: String) : ArrayList[String]= {
      val methodList = ArrayList[String]()
      var parent = cls
      val parents = getParents(cls, parentType)
      val itr= parents.iterator()
      while(itr.hasNext) {
        parent = itr.next()
        bindingScope.getOrElse(parent, null) match {
          case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(method, null) match {
            case methods: mutable.Map[String, Any] =>
              for ((methodName: String, methodDetails: mutable.Map[String, Any]) <- methods) {
                methodDetails.getOrElse(typeModifier, null) match {
                  case abs: String => methodList.add(methodName)
                  case null => if(methodList.contains(methodName))
                    methodList.remove(methodName)
                }
              }
            case null =>
          }
          case null =>
        }
      }

      methodList
    }

    private def getAllFieldsOrMethodsInHierarchyTill(staticType: String, parentType: String, getItem:String): mutable.Map[String, Any] ={
      val parents = getParents(staticType, parentType)
      val itr= parents.iterator()
      var parent = ""
      val items = mutable.Map[String, Any]()
      while(itr.hasNext) {
        parent = itr.next()
        bindingScope.getOrElse(parent, null) match {
          case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(getItem, null) match {
            case fields: mutable.Map[String, Any] =>
              if(getItem.equals(field))
                items.addAll(cloneFields(fields))
              else items.addAll(cloneMethods(fields))
            case _ =>
          }
          case _ =>
        }

      }
      items
    }



    /**
     * @param cls: String - class name
     * @param staticParentClass: String - class in parent hierarchy of cls
     * @return ArrayList of all constructors in the inheritance hierarchy till staticParentClass*/
    private def getAllConstructorMethods(cls:String, staticParentClass: String) : mutable.Map[String, Any]= {
      val methodList = mutable.Map[String, Any]()
      var parent = cls
      val parents = getParents(cls, parentClass)
      val itr= parents.iterator()
      while(itr.hasNext && !parent.equals(staticParentClass)) {
        parent = itr.next()
        if(!parent.equals(staticParentClass)){
          bindingScope.getOrElse(parent, null) match {
            case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(method, null) match {
              case methods: mutable.Map[String, Any] =>
                for ((methodName: String, methodDetails: mutable.Map[String, Any]) <- methods) {
                  if(methodName.startsWith(parent+connector)){
                    methodList(methodName)=cloneMethod(methodDetails)
                  }
                }
              case null =>
            }
            case null =>
          }
        }
      }
      methodList
    }

    /**
     * @param cls: String - class name
     * @return mutable.HashMap of all methods of class cls*/
    private def getMethodsofClass(cls:String) : mutable.Map[String, Any]= {
          bindingScope.getOrElse(cls, null) match {
            case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(method, null) match {
              case methods: mutable.Map[String, Any] => methods
              case null => null
            }
            case null => null
      }
    }


    /**
     * @param parent: String - parentClass name
     * @param classNm: String - class name
     * @return boolean true if classNm is not in the parent heirarchy else returns true */
    def checkCircularComposition(parent: String, classNm: String): Boolean = {
      var newParent = getClassInfo(parent, parentClass)
      while(newParent!=null){
        if(newParent.equals(classNm))
          return false
        newParent = getClassInfo(newParent, parentClass)
      }
      true
    }

    /**
     * @param BaseClass: String - the base class
     * @param parentClass: String - the class that is extended
     * @return boolean value whether the base class inherits or implements abstract methods of parent class*/
    def checkForAbstractMethods(BaseClass: String, parentClass: String, parentType: String): Boolean ={
//      println("BaseClass:"+BaseClass)
//      println("parentClass:"+parentClass)
      if(getClassInfo(parentClass, typeModifier)!=null){
        val abstractMethodList = getAbstractMethods(parentClass, parentType)
        bindingScope.getOrElse(BaseClass, null) match {
          case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(method, null) match {
            case methods: mutable.Map[String, Any] =>
              val itr =abstractMethodList.iterator()
              while(itr.hasNext){
                val methodNm = itr.next()
                if(!methods.contains(methodNm))
                  return false
                else{
                  methods.getOrElse(methodNm, null) match {
                    case methodDetails: mutable.Map[String, Any] => methodDetails.getOrElse(typeModifier,null) match {
                      case str: String => if(str.equals(abstractType))
                                            return false
                      case null =>
                    }
                    case null => return abstractMethodList.size()==0
                  }
                }
              }
            case null =>
              return abstractMethodList.size()==0
          }
          case null => return abstractMethodList.size()==0
        }

      }
      true
    }

    private def runMethod(scopeName: String, objectName: String, methodName: String, params: Parameters): Any = {
      objectMap.getOrElse(scopeName,None) match {
        case objects: mutable.Map[String, Any] =>
          objects.getOrElse(objectName,None) match {
          case objectDetails: mutable.Map[String, Any]=> objectDetails.getOrElse(method, None) match {
            case methodMap: mutable.Map[String, Any] =>
              methodMap.getOrElse(methodName+connector+getParameterLength(params), None) match {
              case method: mutable.Map[String, Any] =>
                method.getOrElse(methodBody, None) match {
                case seq: Seq[SetOper] => method.getOrElse(methodParams, None) match {
                  case methodP : mutable.Map[String, Any] => method(methodParams)=combineMaps(params, methodP)
                  case None =>
                }
                for(i <- 0 until seq.length-1){
                  seq(i).eval(scopeName, objectName, methodName+connector+getParameterLength(params))
                }
                seq.last.eval(scopeName, objectName, methodName+connector+getParameterLength(params))
                case None => null
              }
              case None => null
            }
            case None =>null
          }
          case None =>null
        }
        case None =>null
      }
    }

    /**
     * @param parameters: take Parameter type value
     * @return integer value of count of parameters*/
    private def getParameterLength(parameters: Parameters) = {
      parameters match {
        case Parameters(p) =>
          if(p!=null) p.size
          else 0
      }
    }


    /**
     * @param name: String - class name
     * @return boolean value whether there is a interface with the given name*/
    private def isInterface(name :String): Boolean = {
      bindingScope.getOrElse(name, null) match {
        case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(typeModifier, null) match {
          case str: String =>str.equals(interfaceType)
          case _ =>false
        }
        case null =>false
      }
    }

    /**
     * @param className: String - class name
     * @param info: String - class typeModifier or class parent
     * @return String - info value of class className*/
    private def getClassInfo(className :String, info: String):String = {
      bindingScope.getOrElse(className, null) match {
        case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(info, null) match {
          case classinfo:String => classinfo
          case _ => null
        }
        case null => null
      }
    }

    def hasAtleastOneAbstractMethod(name: String): Boolean = {
      bindingScope.getOrElse(name, null) match {
        case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(method, null) match {
          case methods: mutable.Map[String, Any] =>
            for((mName: String, mDetails: mutable.Map[String, Any])<-methods){
              mDetails.getOrElse(typeModifier, null) match {
                case str:String => if(str.equals(abstractType)){
                  return true
                }
                case _ =>
              }
            }
          case _ =>
        }
        case _ =>
      }
      false
    }

    /**
     * @param className: String - name of a class
     * @param parentName: String - parent class name
     * sets parentName as parent for class className given that it does not already have a parent
     * */
    private def setParent(className :String, parentName:String): Unit = {
      bindingScope.getOrElse(className, null) match {
        case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(parentClass, null) match {
          case parent:String => throw Exception("Multiple Inheritance not supported.")
          case _ => classDetails(parentClass)=parentName
        }
        case null =>
      }
    }

    /**
     * @param className: String - name of a class
     * @param parentInterfaceName: String - parent Interface name
     * sets parentInterfaceName as parent for class className given that it does not already have a parent Interface
     * */
    private def setParentInterface(className :String, parentInterfaceName:String): Unit = {
      bindingScope.getOrElse(className, null) match {
        case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(implementsClass, null) match {
          case parent:String => throw Exception("Multiple Inheritence not supported.")
          case _ => classDetails(implementsClass)=parentInterfaceName
        }
        case null =>
      }
    }

    private def cloneMethods(methods: mutable.Map[String, Any]) = {
      val newMethods = mutable.Map[String, Any]()
      for((methodNm: String, methodDetails: mutable.Map[String, Any]) <- methods){
        newMethods(methodNm) = cloneMethod(methodDetails)
      }
      newMethods
    }


    private def cloneFields(fieldDetails :mutable.Map[String, Any]) = {
      val newFields = mutable.Map[String, Any]()
      for((fieldNm: String, fieldDetail: mutable.Set[Any])<- fieldDetails){
        newFields(fieldNm) = fieldDetail.clone()
      }
      newFields
    }

    private def cloneMethod(methodDetails :mutable.Map[String, Any]) = {
      val newMethod = mutable.Map[String, Any]()
      newMethod(methodBody)=cloneMethodBody(methodDetails)
      newMethod(methodParams)=cloneMethodParams(methodDetails)
      newMethod
    }

    private def cloneMethodParams(methodDetails :mutable.Map[String, Any])={
      val newParamMap = mutable.Map[String, Any]()
      methodDetails.getOrElse(methodParams, None) match {
        case paramsMap: mutable.Map[String, Any] =>
          for ((paramName, paramValue) <- paramsMap) {
            paramValue match {
              case set: mutable.Set[Any] => newParamMap(paramName) = set.clone();
              case value => newParamMap(paramName) = value
            }
          }
          newParamMap
        case _ => newParamMap
      }
    }

    private def cloneMethodBody(methodDetails :mutable.Map[String, Any])={
      methodDetails.getOrElse(methodBody, None) match {
        case body => body
        case None =>
      }
    }

    private def cloneInheritedClassMap(staticType: String, dynamicType: String, staticClassMap: mutable.Map[String, Any], dynamicClassMap: mutable.Map[String, Any]) ={
      val newMap: mutable.Map[String, Any] = mutable.Map[String, Any]()
//      println("@#$%$#"+ bindingScope)
//      println("staticType: "+staticType)
//      println("dynamicType:"+dynamicType)
      val imp = getClassInfo(dynamicType, implementsClass)
      var parents: ArrayList[String] = null
      if(imp==null) {
        parents = getParents(dynamicType, parentClass)
      }else{
        parents = getParents(dynamicType, parentClass)
        parents.addAll(getParents(imp, parentClass))
      }
//      println(parents)
      if(parents.contains(staticType)) {
//        parents = parents.subList(parents.indexOf(staticType), parents.indexOf(dynamicType)+1)
        val newFieldMap=getAllFieldsOrMethodsInHierarchyTill(staticType, parentClass, field)
        for(f<-newFieldMap.keys.toList){
          var parent = dynamicType
          breakable{
            while(parent!=staticType){
              bindingScope.getOrElse(parent, null) match {
                case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(field, null) match {
                  case fields: mutable.Map[String, Any] => if(fields.contains(f)){
                    fields.getOrElse(f,null) match {
                      case field: mutable.Set[Any] => newFieldMap(f) = field.clone()
                      case _ =>
                    }
                    break
                  }
                  case _ =>
                }
                case _ =>
              }
              parent = getClassInfo(parent, parentClass)
            }
          }
        }

        val constructorMethods = getAllConstructorMethods(dynamicType, staticType)
        val NewMethodMap: mutable.Map[String, Any] = getAllFieldsOrMethodsInHierarchyTill(staticType, parentClass,method)
//        println(NewMethodMap)
        for(m<-NewMethodMap.keys.toList){
          var parent = dynamicType
          breakable{
            while(parent!=staticType){
              bindingScope.getOrElse(parent, null) match {
                case classDetails: mutable.Map[String, Any] => classDetails.getOrElse(method, null) match {
                  case methods: mutable.Map[String, Any] => if(methods.contains(m)){
                    methods.getOrElse(m,null) match {
                      case method: mutable.Map[String, Any] => NewMethodMap(m) = cloneMethod(method)
                        case _ =>
                    }
                    break
                  }
                  case _ =>
                }
                case _ =>
              }
              parent = getClassInfo(parent, parentClass)
            }
          }

        }
        NewMethodMap.addAll(constructorMethods)
        newMap(method)=NewMethodMap
      }else{
        throw Exception("static and dynamic type are not in inheritance hierarchy")
      }

      newMap
    }

    private def cloneClassMap(classMap: mutable.Map[String, Any]) ={
      val newMap: mutable.Map[String, Any] = mutable.Map[String, Any]()
      classMap.getOrElse(method, None) match {
        case methodMap: mutable.Map[String, Any] =>
          val methods = mutable.Map[String, Any]()
          for((methodName:String ,methodDetails: mutable.Map[String, Any])<-methodMap){
            val NewMethodMap = mutable.Map[String, Any]()
            methodDetails.getOrElse(methodBody, None) match {
              case body => NewMethodMap(methodBody)=body
              case None =>
            }
            NewMethodMap(methodParams)=cloneMethodParams(methodDetails)
            methods(methodName) = NewMethodMap
          }
          newMap(method) = methods
        case None =>
      }
      classMap.getOrElse(field, None) match {
        case fieldsMap: mutable.Map[String, Any] =>
          val newFieldMap = mutable.Map[String, Any]()
          for((fieldName, fieldValue)<-fieldsMap){
            fieldValue match {
              case set: mutable.Set[Any] => newFieldMap(fieldName)=set.clone();
              case value => newFieldMap(fieldName) = value
            }
          }
          newMap(field) = newFieldMap
        case None =>
      }
      newMap
    }

    private def updateMethodParamsInBindingScope(classNm: String, methodNm: String, params: Parameters): Unit ={
      params match {
        case Parameters(p) =>
          SetOperations.bindingScope.getOrElse(classNm, null) match {
            case map: mutable.Map[String, Any] => map.getOrElse(method, null) match {
              case methodMap: mutable.Map[String, Any] => methodMap.getOrElse(methodNm, null) match {
                case method: mutable.Map[String, Any] => method(methodParams) = p
                case null => methodMap(methodNm)=mutable.Map[String, Any](methodParams->p)
              }
              case null => map(method) = mutable.Map[String, Any](methodNm->mutable.Map[String,Any](methodParams->p))
            }
            case null => SetOperations.bindingScope(classNm) = mutable.Map[String, Any](methodNm -> mutable.Map[String, Any](methodParams -> p))
          }
        case null =>
      }
    }

    def eval(className: String = scopeVariable) : Any = {
      this match {
        case ClassDef(name: String, opers : _*) =>
          defineClass(name, null, className+connector+name, opers)

        case EXTENDS(cls: String)=>
          val classNm = className.split(connector.charAt(0)).toList.last
          val classType = getClassInfo(classNm, typeModifier)
          val newParentClassType = getClassInfo(cls, typeModifier)
          val parent = getClassInfo(classNm, parentClass)
          if (parent == null) {
            if(classType==null || (classType!=null && classType.equals(abstractType))) {
              if(newParentClassType==null || (newParentClassType!=null && newParentClassType.equals(abstractType))) {
                cls match {
                  case name: String =>
                    if (!classNm.equals(name) && checkCircularComposition(name, classNm)) {
                      val hasAbstractMethods = checkForAbstractMethods(classNm, name, parentClass)
                      if (getClassInfo(classNm, typeModifier) == null && !hasAbstractMethods) {
//                        println("esrdtfygbuh" + hasAbstractMethods)

                        bindingScope.remove(classNm)
                        throw Exception("Doesnot Implement abstract methods of parent class")
                      } else {
                        if (bindingScope.getOrElse(name, null) != null)
                          setParent(classNm, name)
                        else {
                          throw Exception(name + " Class not defined")
                        }
                      }
                    } else {
                      throw Exception("Cannot inherit itself.")
                    }

                  case _ => throw Exception("Class Name not a string")
                }
              }else{
                throw Exception("Class cannot extend Interface")
              }
            }else{
              if(newParentClassType==null || (newParentClassType!=null && newParentClassType.equals(abstractType))){
                throw Exception("Interface cannot extend a class/ abstract class")
              }else{
                cls match {
                  case name: String =>
                    if (!classNm.equals(name) && checkCircularComposition(name, classNm)) {
//                      val hasAbstractMethods = checkForAbstractMethods(classNm, name, parentClass)
                      if (bindingScope.getOrElse(name, null) != null)
                        setParent(classNm, name)
                      else {
                        throw Exception(name + " Class not defined")
                      }
                    } else {
                      throw Exception("Cannot inherit itself.")
                    }

                  case _ => throw Exception("Class Name not a string")
                }
              }
            }
          }else{
            throw Exception("Cannot extend more than one class/interface")
          }
//          println(bindingScope)

        case Field(fName: String) =>
          Insert(Variable(fName), collection.immutable.List[SetOper]()).eval(className)

        case Object(name) => objectMap.getOrElse(className, None) match {
          case objects: mutable.Map[String, Any] =>
            objects.getOrElse(name, None)
          case None => None
        }

        case Parameters(param: mutable.Map[String, Any]) =>
          if (param==null) mutable.Map[String, Any]() else param

        case Constructor(params: Parameters,opers: _*)  =>
          val classNm = className.split(connector.charAt(0)).toList.last
          if(isInterface(classNm)){
            throw Exception("Interface cannot have a constructor.")
          }else {
            val methodNm = classNm + connector + getParameterLength(params)
            Macro(methodNm, opers).eval(className)
            updateMethodParamsInBindingScope(classNm, methodNm, params)
          }


        case Method(mName:String, params:Parameters , opers: _*) =>
          val classNm = className.split(connector.charAt(0)).toList.last
          val methodNm = mName+connector+getParameterLength(params)
          if(!isInterface(classNm)) {
            Macro(methodNm, opers).eval(className)
            updateMethodParamsInBindingScope(classNm, methodNm, params)
          }else{
            throw Exception("Interface cannot have Concrete Methods.")
          }

        case NewObject(staticType:String, dynamicType:String, variable:Variable, params: Parameters) =>
          val scopeNm = className.split(connector.charAt(0)).toList.last
          if(bindingScope.getOrElse(staticType,None)==None || bindingScope.getOrElse(dynamicType,None)==None)
            throw Exception("Class Not defined")
          else if(getClassInfo(dynamicType, typeModifier)!=null){
            throw Exception("cannot create object of abstract class or Interface")
          } else {
              variable match {
                case Variable(v) =>

                  val classTemp = if(staticType.equals(dynamicType))
                    cloneClassMap(bindingScope.getOrElse(staticType, null))
                  else cloneInheritedClassMap(staticType, dynamicType, bindingScope.getOrElse(staticType, null), bindingScope.getOrElse(dynamicType, null))

                  objectMap.getOrElse(scopeNm, None) match {
                    //put static and dynamic type
                    case map: collection.mutable.Map[String, Any] => map(v) = classTemp
                    case None => objectMap(scopeNm) = mutable.Map[String, Any](v -> classTemp)
                  }
                  runMethod(scopeNm, v, staticType, params)
                case _ =>
              }
          }

        case InvokeMethod(variable: Object, mName: String, params: Parameters) =>
          println("InvokeMethod")
          val scopeNm = className.split(connector.charAt(0)).toList.last
//          println(variable)
          variable match {
            case Object(name) =>
              variable.eval(scopeNm) match {
                case map =>
                  runMethod(scopeNm, name,mName, params)
                case None => throw Exception("object not defined")
              }
            case null =>
          }

        case AbstractClassDef(cName: String, classOper: _*) =>
          defineClass(cName, abstractType, className+connector+cName, classOper)
          if(!hasAtleastOneAbstractMethod(cName)){
            bindingScope.remove(cName)
            throw Exception("Abstract Class doesn't have any abstract methods.")
          }


        case AbstractMethod(mName:String, params:Parameters) =>
          val methodNm = mName+connector+getParameterLength(params)
          val classNm = className.split(connector.charAt(0)).toList.last
          val isAbstractType:String = getClassInfo(classNm, typeModifier)
          if(isAbstractType!=null){
            Macro(methodNm, null, isAbstractType).eval(className)
            val classNm = className.split(connector.charAt(0)).toList.last
            updateMethodParamsInBindingScope(classNm, methodNm, params)
          }else{
            throw Exception("Only abstract class or Interface can have abstract methods")
          }



        case Implements(interface: String)=>
          val classNm = className.split(connector.charAt(0)).toList.last
          val classType = getClassInfo(classNm, typeModifier)
          if(classType==null || (classType!=null && classType.equals(abstractType))) {
            val newParentClassType = getClassInfo(interface, typeModifier)
            if(newParentClassType==null || (newParentClassType!=null && newParentClassType.equals(abstractType))){
              throw  Exception(interface+" is not an interface.")
            }else{
              val parent = getClassInfo(classNm, implementsClass)
              if(parent==null){
                val hasAbstractMethods = checkForAbstractMethods(classNm, interface, parentClass)
                if (getClassInfo(classNm, typeModifier) == null && !hasAbstractMethods) {

                  bindingScope.remove(classNm)
                  throw Exception("Doesnot Implement abstract methods of parent class")
                } else {
                  if (bindingScope.getOrElse(interface, null) != null)
                    setParentInterface(classNm, interface)
                  else {
                    throw Exception(interface + " interface not defined")
                  }
                }
              }else{
                throw Exception("Cannot implement more than one Interface")
              }
            }

          }else{
            throw Exception("Interface cannot implement an interface.")
          }

        case InterfaceDef(name: String, interfaceOper: _*) =>
          defineClass(name, interfaceType, className+connector+name, interfaceOper)
      }
    }




