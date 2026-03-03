package io.dm.data.yaml;

import kotlin.io.FileTreeWalk;

import java.nio.file.Path;
import java.util.function.Predicate;

public abstract class YamlFile<T> {

    public abstract String path();

    public abstract <T extends Object> void forEachFile(T yamlObject);

    public abstract Class<?> dataClass();

    public Predicate<Path> additionalFileFiler(){
        return path -> true;
    }

    public boolean includeSubDirectories(){
        return false;
    }



}
