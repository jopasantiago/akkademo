
val x = 3
x

def --> = {

}

3.toString
"magicvariable".toList

var r = 55


class Dog(var name: String) {
  var age: Int = _

  def getAge = age

  def callDog(greeting: String) = {
    println(s"$greeting $name")
  }

  def this() = this("generic dog")
}

val dog = new Dog("Pies")
dog.age
dog.name
dog.name = "Lody"
dog.name
dog.age = 3

def -->(h: String): Boolean = true


dog callDog "HEY"

val newDog = new Dog
newDog.name

val list = List(1, 2, 3)

def isOne(x: Int): Boolean = x == 1
list.exists(isOne)
list.exists(x => x == 1)
list.exists(_ == 1)

dog eq newDog

def padString(input: String, length: Int, char: Char): String = {
  if (input.size < length) {
    padString(input + char, length - 1, char)
  } else {
    input
  }
}