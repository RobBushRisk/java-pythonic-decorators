import com.bob.Parser;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.bob.Parser.getDecorators;


public class TestParser {

    @Test
    public void testFindingDecorators() {
        List<AnnotationDeclaration> decorators = Parser.getDecorators(new File("src/test"));
        Assertions.assertEquals("ExampleDecorator", decorators.getFirst().getNameAsString());
        Assertions.assertEquals(1, decorators.size());
    }

    @Test
    public void testFindingDecoratedMethods() {
        List<AnnotationDeclaration> decorators = Parser.getDecorators(new File("src/test"));
        AnnotationDeclaration decorator = decorators.getFirst();

        List<MethodDeclaration> decoratedMethods = Parser.getDecoratedMethods(decorator, new File("src/test"));
        Assertions.assertEquals("doSomething", decoratedMethods.getFirst().getNameAsString());
        Assertions.assertEquals(1, decoratedMethods.size());
    }

    @Test
    public void testParsingExample() throws IOException {
        String decoratedClassResult = "package example;\n" +
                "\n" +
                "public class ExampleDecoratedClass {\n" +
                "\n" +
                "    @ExampleDecorator\n" +
                "    public void doSomething() {\n" +
                "        System.out.println(\"Hello from ExampleDecorator.apply()\");\n" +
                "        {\n" +
                "            // do thing that will be decorated\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        Parser.parse(new File("src/test/java/example"), new File("parsedExample"));
        Assertions.assertEquals(
                Files.readString(Path.of("src/test/java/exampleOutput.txt")),
                Files.readString(Path.of("parsedExample/ExampleDecoratedClass.java")));
    }

}
