package com.bob.mojo;

import com.bob.Parser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Mojo(name = "decorator-compiler-preprocessor", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompilerPreProcessor extends AbstractMojo {

    @Parameter(required = true)
    private String sourceDirectory;

    public void execute() {

        File sourceFile = new File(sourceDirectory);

        try {
            FileUtils.copyDirectory(sourceFile, new File("backup_compiler_storage"));
            FileUtils.copyDirectory(sourceFile, new File("temp_compiler_storage"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.deleteDirectory(sourceFile);
            Parser.parse(new File("temp_compiler_storage"), sourceFile);
        } catch (Exception e) {
            throw new RuntimeException("if you lose your source code, check in backup_compiler_storage \n" + e);
        }

    }

}
