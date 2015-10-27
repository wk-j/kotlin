import kotlin.properties.Delegates

open class A {
    private var privatePropertyPrivateSet: Int by Delegates.notNull()
        private set

    private var privatePropertyProtectedSet: Int by Delegates.notNull()
        protected set

    private var privatePropertyInternalSet: Int by Delegates.notNull()
        internal set

    private var privatePropertyPublicSet: Int by Delegates.notNull()
        public set

    protected var protectedPropertyPrivateSet: Int by Delegates.notNull()
        private set

    protected var protectedPropertyProtectedSet: Int by Delegates.notNull()
        protected set

    protected var protectedPropertyInternalSet: Int by Delegates.notNull()
        internal set

    protected var protectedPropertyPublicSet: Int by Delegates.notNull()
        public set

    internal var internalPropertyPrivateSet: Int by Delegates.notNull()
        private set

    internal var internalPropertyProtectedSet: Int by Delegates.notNull()
        protected set

    internal var internalPropertyInternalSet: Int by Delegates.notNull()
        internal set

    internal var internalPropertyPublicSet: Int by Delegates.notNull()
        public set

    public var publicPropertyPrivateSet: Int by Delegates.notNull()
        private set

    public var publicPropertyProtectedSet: Int by Delegates.notNull()
        protected set

    public var publicPropertyInternalSet: Int by Delegates.notNull()
        internal set

    public var publicPropertyPublicSet: Int by Delegates.notNull()
        public set

    init {
        privatePropertyPrivateSet = 1
        privatePropertyProtectedSet = 1
        privatePropertyInternalSet = 1
        privatePropertyPublicSet = 1

        protectedPropertyPrivateSet = 1
        protectedPropertyProtectedSet = 1
        protectedPropertyInternalSet = 1
        protectedPropertyPublicSet = 1

        internalPropertyPrivateSet = 1
        internalPropertyProtectedSet = 1
        internalPropertyInternalSet = 1
        internalPropertyPublicSet = 1

        publicPropertyPrivateSet = 1
        publicPropertyProtectedSet = 1
        publicPropertyInternalSet = 1
        publicPropertyPublicSet = 1
    }
}

class B : A() {
    init {
        <!INVISIBLE_MEMBER!>privatePropertyPrivateSet<!> = 2
        <!INVISIBLE_MEMBER!>privatePropertyProtectedSet<!> = 2
        <!INVISIBLE_MEMBER!>privatePropertyInternalSet<!> = 2
        <!INVISIBLE_MEMBER!>privatePropertyPublicSet<!> = 2

        <!INVISIBLE_SETTER!>protectedPropertyPrivateSet<!> = 2
        protectedPropertyProtectedSet = 2
        protectedPropertyInternalSet = 2
        protectedPropertyPublicSet = 2

        <!INVISIBLE_SETTER!>internalPropertyPrivateSet<!> = 2
        internalPropertyProtectedSet = 2
        internalPropertyInternalSet = 2
        internalPropertyPublicSet = 2

        <!INVISIBLE_SETTER!>publicPropertyPrivateSet<!> = 2
        publicPropertyProtectedSet = 2
        publicPropertyInternalSet = 2
        publicPropertyPublicSet = 2
    }
}

fun main(args: Array<String>) {
    val b = B()

    b.<!INVISIBLE_MEMBER!>privatePropertyPrivateSet<!> = 3
    b.<!INVISIBLE_MEMBER!>privatePropertyProtectedSet<!> = 3
    b.<!INVISIBLE_MEMBER!>privatePropertyInternalSet<!> = 3
    b.<!INVISIBLE_MEMBER!>privatePropertyPublicSet<!> = 3

    b.<!INVISIBLE_MEMBER!>protectedPropertyPrivateSet<!> = 3
    b.<!INVISIBLE_MEMBER!>protectedPropertyProtectedSet<!> = 3
    b.<!INVISIBLE_MEMBER!>protectedPropertyInternalSet<!> = 3
    b.<!INVISIBLE_MEMBER!>protectedPropertyPublicSet<!> = 3

    <!INVISIBLE_SETTER!>b.internalPropertyPrivateSet<!> = 3
    <!INVISIBLE_SETTER!>b.internalPropertyProtectedSet<!> = 3
    b.internalPropertyInternalSet = 3
    b.internalPropertyPublicSet = 3

    <!INVISIBLE_SETTER!>b.publicPropertyPrivateSet<!> = 3
    <!INVISIBLE_SETTER!>b.publicPropertyProtectedSet<!> = 3
    b.publicPropertyInternalSet = 3
    b.publicPropertyPublicSet = 3
}