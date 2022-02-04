package CS474

object OurArithExpDSL:
  type BasicType = Int
  enum ArithExp:
    case Value(input: BasicType)
    case Var(name: String)
    case Add(op1: ArithExp, op2: ArithExp)
    case Sub(op1: ArithExp, op2: ArithExp)
    private val bindingScoping: Map[String, Int] = Map("x"->2, "Adan"->10)

    def eval: BasicType =
      this match {
        case Value(i) => i
        case Var(name) => bindingScoping(name)
        case Add(op1, op2) => op1.eval + op2.eval

        case Sub(op1, op2) => op1.eval - op2.eval
      }

//  @main def runArithExp: Unit =
//    import ArithExp.*
//    val firstExpression = Sub(Add(Add(Value(2), Value(3)),Var("Adan")), Var("x")).eval
//    println(firstExpression)
