package CS474

object SetOperations :
  type BasicType= Any
  enum SetOper:
    case Value(input: BasicType)
    case Variable(name: String)
    case Insert(setname: SetOper, objectList: List[SetOper])
    case Delete(setName: SetOper, obj: SetOper)
    private var bindingScope: scala.collection.mutable.Map[BasicType, BasicType] = Map()

    def eval: BasicType = {
      this match {
        case Value(i) => i
        case Variable(name) => bindingScope(name)
        case Insert(setname, objectList) =>
          if (setname.eval == null){
            var setObj: Set[BasicType] = Set()
            for(obj <- objectList) {
              setObj+=obj.eval
            }
            bindingScope(setname.eval)=setObj
          }
      }
    }

  @main def runSetExp: Unit =
    import SetOper.*
    val firstExpression = Insert(Variable("NewSet"),List(Variable("set"),Variable("1234"), Value(1234))).eval
    println(firstExpression)