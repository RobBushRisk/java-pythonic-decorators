package example;

public class ExampleDecoratedClass {

    @ExampleDecorator2
    @ExampleDecorator
    public void doSomething() {
        // do thing that will be decorated
    }

}
