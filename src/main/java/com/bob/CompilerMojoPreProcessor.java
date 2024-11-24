package com.bob;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Mojo(name = "decorator-compiler-preprocessor", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompilerMojoPreProcessor extends AbstractMojo {

    @Parameter(required = true)
    private String sourceDirectory;

    public void execute() {

        try {
            FileUtils.copyDirectoryStructure(new File(sourceDirectory), new File("backup_compiler_storage"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.copyDirectoryStructure(new File(sourceDirectory), new File("temp_compiler_storage"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.deleteDirectory(new File(sourceDirectory));
            Parser.parse(new File("temp_compiler_storage"), new File(sourceDirectory));
        } catch (Exception e) {
            throw new RuntimeException("if you lose your source code, check in backup_compiler_storage \n" + e);
        }

    }

}
