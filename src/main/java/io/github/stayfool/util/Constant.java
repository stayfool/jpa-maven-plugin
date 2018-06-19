package io.github.stayfool.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * @author paoding
 * @date 2017/12/6
 */
public interface Constant {

    String DOT = ".";
    String ESCAPE_DOT = "\\.";
    String SEMICOLON = ";";
    String EQUAL = "=";
    String SPACE = " ";
    String UNDER_LINE = "_";
    String LEFT_BRACKET = "(";
    String FILE_PROTOCOL = "file:";

    String JAVA_FILE_TYPE = ".java";
    String CLASS_FILE_TYPE = ".class";
    String REPOSITORY_SUFFIX = "Repository";

    String TYPE_STRING = "String";

    String AUTO_INCREMENT = "auto_increment";
    String SEQUENCE = "sequence";
    String GENERATION_AUTO_INCREMENT = "GenerationType.IDENTITY";
    String GENERATION_SEQUENCE = "GenerationType.SEQUENCE";
    String GENERATION_AUTO = "GenerationType.AUTO";

    String SERIALIZABLE = "java.io.Serializable";
    String LOMBOK_DATA = "lombok.Data";
    String LOMBOK_EQUALS_AND_HASH_CODE = "lombok.EqualsAndHashCode";

    String COMPILE_BASE_DIR = "/target/classes/";
    String SOURCE_BASE_DIR = "/src/main/java/";
}
