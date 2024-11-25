package example;

public class ExampleDecoratedClass {

    @ExampleDecorator2
    @ExampleDecorator
    public void doSomething() {
        System.out.println("Hello from ExampleDecorator.apply()");
        if (true) {
            {
                System.out.println("Hello from ExampleDecorator2.apply()");
                if (true) {
                    {
                        // do thing that will be decorated
                    }
                }
            }
        }
    }
}
