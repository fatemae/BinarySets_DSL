# BinarySets-withAbstractClassesAndInterfaces-CS474
Fatema Engineeringwala - 675589901

## Brief Project Description
A language for users of Set Theory to create and evaluate binary operations on sets along with classes, abstract classes , interfaces and its inheritance.

## Instructions on Running Code
`sbt compile` used to compile the code<br>
`run` used to run the main class<br>
`test` used to run all the test cases

## Implementation
1. Implemented Binary Set Theory operations embedded in scala language. The Set Operations implemented include Insert, Delete, Union, Intersection, Difference, Symmetric Difference, Cartesian Product.
2. Implemented Macros to reuse an expression
3. Implemented Scoping rules
4. Implemented Class Declaration with its fields and methods
5. Inheritance of classes
6. Object Creation
7. Necessary to have a constructor of a class to instantiate its object
8. Can only inherit from one class at a time.
9. Nested classes
10. Implemented Concept of Abstract Classes
11. Abstract Methods
12. Interfaces
13. implementation of Interfaces


## Classes and Inheritance datatypes
1. ClassDef<br>
   It will be used for defining a class, its methods and variables etc. It takes one mandatory String value that is className and any number of class datatypes like ClassDef(for nested classes), Field, Constructor, Method, and EXTENDS which are described below.<br>
   `ClassDef("A", Field("a"), Constructor(Parameters(null),Assign("a", Value(2))))` . This will create a class A with field 'a' and a constructor with no parameters.<br>
2. Field<br>
   This datatype are used inside ClassDef to define class fields. It takes just one string value for field name <br>
3. Constructor<br>
   This datatype is used in classdef to define class constructor. It takes one mandatory value of datatype Parameters and any number SetExpressions as its body.<br>
4. Methods<br>
   This datatype is used in classdef to define class methods. It takes a string method name, a Parameter datatype to define methods input params and any number of setExpressions as its body. The last line of the body will be used as the return value of the function.<br>
   `Method("newMethod", Parameters(collection.mutable.Map("x"->None, "y"->None)), Insert(Variable("setZ"), List(Variable("x"), Variable("y"))),Union(Variable("setX"), Variable("setZ")))`
5. Parameters<br>
   Used in constructors and methods to define its params. It takes a Map of key value pairs as input.<br>
6. EXTENDS<br>
   Used in ClassDef to declare inheritance. It takes one class name string value to define the class that it extends, given the class is already defined before extending it. EXTENDS should be used as the last field of classDef so that it can read through aal the defined methods and fields to make sure that parents classes abstract methods are overridden or not.<br>
   `ClassDef("A", EXTENDS("B"))` means class A extends class B. <br>
7. NewObject<br>
   Used to instantiate an object of a class defined earlier. It takes two string class names for static and dynamic type of object, Variable type for variable name and Parameters for the constructor if any.<br>
   `NewObject("A", "A",Variable("newObj"), Parameters(collection.mutable.Map("v"->2,"v1"->collection.mutable.HashSet("var","set", "34", 1234))))`
8. InvokeMethod<br>
   Used to invoke a method pre-defined in a class using a class object. It takes Variable type object for a class, String method name and Parameters for the method if any.<br>
   `InvokeMethod(Object("newObj"), "newMethod", Parameters(collection.mutable.Map("x"->1, "y"->2)))`
9. Object<br>
   Used to access a Object created using NewObject. It takes string type of object name.<br>
   `Object("newObj")`
10. AbstractClassDef<br>
   Used to define an Abstract Class. It has one mandatory string field to define Class name, it can take in any methods, fields or constructor declaration. It can also nest another Abstract class, class or interface.<br>
   `AbstractClassDef("C", AbstractMethod("method3", Parameters(null)), Method("method2", Parameters(null), null), EXTENDS("B"))`
11. AbstractMethod<br>
   Used to define an Abstract Method i.e. a method without its implementation details. It can be encapsulated in any abstract class or Interface. It takes mandatory methodname and Parameter type value.<br>
   `AbstractMethod("method1", Parameters(null))`
12. InterfaceDef<br>
   Used to define an Interface Class, a class which has only abstract methods, no constructor and cannot have its own object created. It take string field of interface name and any number of abstract methods and fields.<br>
   `InterfaceDef("newIntr", AbstractMethod("m1", Parameters(null)))`
13. Implements(interface: String)<br>
   Used in an abstract or non-abstract class to define which interface it implements. It takes one string value of interface name. Implements should be the last field in class def or a field after all method and field declarations to ensure that class has already overriden abstract parent methods.<br>
   `ClassDef("X", Method("m1", Parameters(null), Insert(Variable("x"), List(Value(3)))), Implements("newIntr1"))`

## Binary Set Expressions
Always call eval function on the expression to run it<br>
1. Insert<br>
   To insert values and variables into a set. It will create a new set if not already present.<br>
   `val op = Insert(Variable("setname") , List(Value("set"), Value("34"), Value(1234)))` <br>
   `op.eval` - To run this expression<br>

2. Assign<br>
   To assign a class variable with a value. Used in the constructor or any methods of class.<br>
   `Assign("setX", Variable("v1"))`<br>

4. Delete<br>
   To delete a value or variable from a set. <br>
   `val op = Delete(Variable("setname") , Value("value"))` <br>

5. Union<br>
   To perform a union of two sets. It returns the result of Union <br>
   `val op = Union(Variable("setnameA") , Variable("setnameB"))` <br>

6. Intersection<br>
   To perform intersection of two sets. It returns the result of Intersection <br>
   `val op = Intersection(Variable("setnameA") , Variable("setnameB"))` <br>

7. Difference<br>
   To perform difference of setA and setB. It returns the result of Difference of set <br>
   `val op = Difference(Variable("setnameA") , Variable("setnameB"))` <br>

8. Symmetric Difference<br>
   To perform Symmetric Difference of two sets. It returns the result of Symmetric Difference <br>
   `val op = Symm_Difference(Variable("setnameA") , Variable("setnameB"))` <br>

9. Cartesian Product<br>
   To perform Cartesian Product of two sets. It returns the result of the product <br>
   `val op = Cartesian_Product(Variable("setnameA") , Variable("setnameB"))` <br>

10. Macro<br>
    Creates a Macro for a set operation.<br>
    `val op = Macro("someName", Delete(Variable("B"), Value("set")))`

11. MacroEval<br>
    To evaluate any already created Macro<br>
    `MacroEval("someName").eval`

12. Scope<br>
    To create a scope <br>
    `Scope("scopename", Scope("othername", Insert(Variable("someSetName"), List(Value("var"), Value(1), Value("somestring"))))).eval`
