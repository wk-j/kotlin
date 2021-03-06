@Ann(Foo.i, Foo.s, Foo.f, Foo.d, Foo.l, Foo.b, Foo.bool, Foo.c, Foo.str, Foo.charAsInt, Foo.intAsChar) class MyClass

fun box(): String {
    val ann = javaClass<MyClass>().getAnnotation(javaClass<Ann>())
    if (ann == null) return "fail: cannot find Ann on MyClass}"
    if (ann.i != 2) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.s != 2.toShort()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.f != 2.toFloat()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.d != 2.toDouble()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.l != 2.toLong()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (ann.b != 2.toByte()) return "fail: annotation parameter i should be 2, but was ${ann.i}"
    if (!ann.bool) return "fail: annotation parameter i should be true, but was ${ann.i}"
    if (ann.c != 'c') return "fail: annotation parameter i should be c, but was ${ann.i}"
    if (ann.str != "str") return "fail: annotation parameter i should be str, but was ${ann.i}"
    if (ann.i2 != '3'.toInt()) return "fail: annotation parameter i2 should be ${'3'.toInt()}, but was ${ann.i}"
    if (ann.c2 != 3.toChar()) return "fail: annotation parameter c2 should be 3, but was ${ann.i}"
    return "OK"
}

@Retention(AnnotationRetention.RUNTIME)
annotation class Ann(
        val i: Int,
        val s: Short,
        val f: Float,
        val d: Double,
        val l: Long,
        val b: Byte,
        val bool: Boolean,
        val c: Char,
        val str: String,
        val i2: Int,
        val c2: Char
)