fun test() {
    class Test {
        fun unaryPlus(vararg a: Int): Test = Test()
    }
    val test = Test()
    test.unaryPl<caret>us()
}
