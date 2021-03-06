package foo

// CHECK_NOT_CALLED: test
// CHECK_NOT_CALLED: fn

class A(val x: Any? = null) {
    inline
    fun test<reified T, reified R>(b: B) = b.fn<T, R>()

    inline
    fun B.fn<reified T, reified R>() = x is T && y is R
}

class B(val y: Any? = null)

class X
class Y

fun box(): String {
    val x = X()
    val y = Y()

    assertEquals(true, A(x).test<X, Y>(B(y)), "A(x).test<X, Y>(B(y))")
    assertEquals(false, A(y).test<X, Y>(B(y)), "A(y).test<X, Y>(B(y))")
    assertEquals(false, A(y).test<X, Y>(B(x)), "A(y).test<X, Y>(B(x))")
    assertEquals(false, A(x).test<X, Y>(B(x)), "A(x).test<X, Y>(B(x))")

    return "OK"
}