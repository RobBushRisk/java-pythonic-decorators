import com.bob.Parser;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
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

}
