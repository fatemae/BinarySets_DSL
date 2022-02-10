# BinarySetsDSL-CS474

## Brief Project Description
A language for users of Set Theory to create and evaluate binary operations on sets

## Instructions on Running Code
`sbt compile` used to compile the code<br>
`run` used to run the main class<br>
`test` used to run all the test cases

## Implementation
1. Implemented Binary Set Theory operations embedded in scala language. The Set Operations implemented include Insert, Delete, Union, Intersection, Difference, Symmetric Difference, Cartesian Product.
2. Implemented Macros to reuse an expression
3. Implemented Scoping rules

## Binary Set Expressions
Always call eval function on the expression to run it<br>
1. Insert<br>
   To insert values and variables into a set. It will create a new set if not already present.<br>
   `val op = Insert(Variable("setname") , List(Value("set"), Value("34"), Value(1234)))` <br>
   `op.eval` - To run this expression<br>

2. Delete<br>
   To delete a value or variable from a set. <br>
   `val op = Delete(Variable("setname") , Value("value"))` <br>

3. Union<br>
   To perform a union of two sets. It returns the result of Union <br>
   `val op = Union(Variable("setnameA") , Variable("setnameB"))` <br>

4. Intersection<br>
   To perform intersection of two sets. It returns the result of Intersection <br>
   `val op = Intersection(Variable("setnameA") , Variable("setnameB"))` <br>

5. Difference<br>
   To perform difference of setA and setB. It returns the result of Difference of set <br>
   `val op = Difference(Variable("setnameA") , Variable("setnameB"))` <br>

6. Symmetric Difference<br>
   To perform Symmetric Difference of two sets. It returns the result of Symmetric Difference <br>
   `val op = Symm_Difference(Variable("setnameA") , Variable("setnameB"))` <br>

7. Cartesian Product<br>
   To perform Cartesian Product of two sets. It returns the result of the product <br>
   `val op = Cartesian_Product(Variable("setnameA") , Variable("setnameB"))` <br>

8. Macro<br>
   Creates a Macro for an set operation.<br>
   `val op = Macro("someName", Delete(Variable("B"), Value("set")))`

9. MacroEval<br>
   To evaluate any already created Macro<br>
   `MacroEval("someName").eval`

10. Scope<br>
   To create a scope <br>
   `Scope("scopename", Scope("othername", Insert(Variable("someSetName"), List(Value("var"), Value(1), Value("somestring"))))).eval`
