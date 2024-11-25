package example;

import com.bob.Decorator;

public @interface ExampleDecorator2 {

    public class ExampleDecoratorClass extends Decorator {

        public void apply() {
            System.out.println("Hello from ExampleDecorator2.apply()");
            if (true) {
                proceed();
            }
        }

    }

}
