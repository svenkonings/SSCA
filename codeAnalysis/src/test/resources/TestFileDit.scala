// DIT 1
class Test1 {

}

// DIT 2
class Test2 extends A {

}

// DIT 3
class Test3 extends B {

}

// DIT 1
class Test4 extends D {

}

// DIT 2
class Test5 extends A with D {

}

// DIT 1
class Test6 extends D with E {

}

// DIT 2
class Test7 extends A with F {

}

class A{

}

class B extends C {

}


class C {

}

trait D{

}

trait E{

}

trait F extends D with E {

}