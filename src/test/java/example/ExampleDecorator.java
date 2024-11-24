package example;

import com.bob.Decorator;

public @interface ExampleDecorator {

    public class ExampleDecoratorClass extends Decorator {

        public void apply() {
            System.out.println("Hello from ExampleDecorator.apply()");
            proceed();
        }

    }

}
