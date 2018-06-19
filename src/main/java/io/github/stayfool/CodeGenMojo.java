package io.github.stayfool;

import io.github.stayfool.config.DatabaseConfig;
import io.github.stayfool.config.DatabaseProperties;
import io.github.stayfool.config.GenerateConfig;
import io.github.stayfool.module.Entity;
import io.github.stayfool.module.EntityField;
import io.github.stayfool.module.Repository;
import io.github.stayfool.util.Constant;
import io.github.stayfool.util.NameUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Pattern;

import static org.apache.velocity.runtime.RuntimeConstants.ENCODING_DEFAULT;

/**
 * JPA generate generate plugin
 *
 * @author paoding
 * @date 2017/12/05
 */
@Mojo(name = "generate", threadSafe = true)
public class CodeGenMojo extends AbstractMojo {

    private String author = System.getProperty("user.name");
    private String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    private Pattern pattern = Pattern.compile("\\s*\\w+\\s+\\w+\\s+\\w+.*;$");

    @Parameter(required = true)
    private DatabaseConfig database;

    @Parameter(required = true)
    private GenerateConfig generate;

    private DatabaseProperties databaseProperties;

    private Connection connection;

    private Set<String> excludeFields;

    @Override
    public void execute() throws MojoExecutionException {

        PreparedStatement statement = null;
        try {
            initVelocity();
            initDatabaseConfig();
            initSuperClassField();
            initConnection();
            String baseDir = getBaseDir();

            statement = connection.prepareStatement(databaseProperties.getTableInfoSql());
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String tableName = results.getString(databaseProperties.getTableName());
                String tableComment = results.getString(databaseProperties.getTableComment());

                if (!needGenerate(tableName)) {
                    continue;
                }

                String idType = generateEntity(baseDir, tableName, tableComment);

                if (generate.getRepository() != null) {
                    generateRepository(idType, tableName, baseDir);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("generate code failed", e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成实体类
     *
     * @param baseDir baseDIR
     * @return 主键类型
     * @throws Exception IOException
     */
    private String generateEntity(String baseDir, String tableName, String tableComment) throws Exception {

        String idType = null;

        //获取表的详细信息
        String sql = String.format(databaseProperties.getTableFieldSql(), tableName);
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet fieldResult = statement.executeQuery();

        List<EntityField> fieldList = new ArrayList<>();
        Set<String> imports = new HashSet<>();

        // 处理表的列
        while (fieldResult.next()) {

            String column = fieldResult.getString(databaseProperties.getColumnName());
            String name = NameUtil.underlineToCamel(column);

            if (excludeFields.contains(name)) {
                getLog().info("field " + name + " is in super class, skip");
                continue;
            }

            EntityField field = new EntityField();

            field.setColumn(column);
            field.setName(name);

            String dbType = fieldResult.getString(databaseProperties.getColumnType());
            String javaType = databaseProperties.getJavaType(dbType);
            javaType = resolveImport(imports, javaType);

            field.setType(javaType);

            field.setComment(fieldResult.getString(databaseProperties.getTableComment()));
            field.setMethod(NameUtil.capitalizeFirst(field.getName()));

            // 避免多重主键设置，目前只取第一个找到ID，并放到list中的索引为0的位置
            String key = fieldResult.getString(databaseProperties.getColumnKey());
            // 处理ID
            if (databaseProperties.getColumnKeyValue().equalsIgnoreCase(key)) {
                idType = field.getType();
                field.setId(true);
                try {
                    String generator = fieldResult.getString(databaseProperties.getColumnGenerator());
                    if (StringUtils.isNotEmpty(generator)) {
                        if (Constant.AUTO_INCREMENT.equalsIgnoreCase(generator)) {
                            generator = Constant.GENERATION_AUTO_INCREMENT;
                        } else if (Constant.SEQUENCE.equalsIgnoreCase(generator)) {
                            generator = Constant.GENERATION_SEQUENCE;
                        } else {
                            generator = Constant.GENERATION_AUTO;
                        }
                        field.setGenerator(generator);
                    }
                } catch (SQLException ignore) {
                }
            }

            fieldList.add(field);
        }

        if (generate.getEntity().getUseLombok()) {
            imports.add(Constant.LOMBOK_DATA);
            if (StringUtils.isNotBlank(generate.getEntity().getSuperClass())) {
                imports.add(Constant.LOMBOK_EQUALS_AND_HASH_CODE);
            }
        }

        String pkg = getEntityPkg();
        String name = NameUtil.capitalizeFirst(NameUtil.underlineToCamel(handleTableName(tableName)));
        String superClass = "";
        if (StringUtils.isNotBlank(generate.getEntity().getSuperClass())) {
            superClass = generate.getEntity().getSuperClass().trim();
            if (superClass.indexOf(Constant.DOT) > 0) {
                imports.add(superClass);
                superClass = superClass.substring(superClass.lastIndexOf(Constant.DOT) + 1);
            }
        } else {
            imports.add(Constant.SERIALIZABLE);
        }

        List<String> importList = new ArrayList<>(imports);
        importList.sort(null);

        Entity entity = Entity.builder()
                .author(author)
                .comment(tableComment)
                .date(date)
                .fields(fieldList)
                .imports(importList)
                .name(name)
                .pkg(pkg)
                .table(tableName)
                .superClass(superClass)
                .useLombok(generate.getEntity().getUseLombok())
                .needColumnAnnotation(generate.getEntity().getNeedColumnAnnotation())
                .build();

        Files.createDirectories(Paths.get(baseDir + generate.getEntity().getPkg()));

        String fileName = baseDir + generate.getEntity().getPkg() + File.separator + name + Constant.JAVA_FILE_TYPE;

        if (Files.notExists(Paths.get(fileName)) || generate.getOverride()) {

            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, ENCODING_DEFAULT));
            Velocity.mergeTemplate(
                    generate.getEntity().getTemplate(),
                    ENCODING_DEFAULT,
                    fromObject(entity),
                    writer);
            writer.close();

            getLog().info(fileName + " generated");
        }

        return idType;
    }

    /**
     * 生成repository
     *
     * @param idType    idType
     * @param tableName tableName
     * @param baseDir   baseDir
     * @throws Exception MIOException
     */
    private void generateRepository(String idType, String tableName, String baseDir) throws Exception {

        String entityName = NameUtil.capitalizeFirst(NameUtil.underlineToCamel(handleTableName(tableName)));
        String entityPkg = getEntityPkg();
        String repositoryPkg = getSpringRepositoryPkg();
        String repositoryName = entityName + Constant.REPOSITORY_SUFFIX;
        String superInterface = generate.getRepository().getSuperInterface();

        if (StringUtils.isNotBlank(generate.getEntity().getIdType())) {
            idType = generate.getEntity().getIdType().trim();
        }

        if (StringUtils.isBlank(idType)) {
            idType = Constant.SERIALIZABLE;
        }

        Set<String> imports = new HashSet<>();
        if (idType.indexOf(Constant.DOT) > 0) {
            imports.add(idType);
            idType = idType.substring(idType.lastIndexOf(Constant.DOT) + 1);
        }
        imports.add(entityPkg + Constant.DOT + entityName);
        imports.add(superInterface);
        superInterface = superInterface.substring(superInterface.lastIndexOf(Constant.DOT) + 1);

        List<String> importList = new ArrayList<>(imports);
        importList.sort(null);

        Repository repository = Repository.builder()
                .pkg(repositoryPkg)
                .imports(importList)
                .author(author)
                .date(date)
                .table(tableName)
                .name(repositoryName)
                .superInterface(superInterface)
                .entityName(entityName)
                .entityIdType(idType)
                .build();

        Files.createDirectories(Paths.get(baseDir + generate.getRepository().getPkg()));

        String fileName = baseDir + generate.getRepository().getPkg() + File.separator
                + repositoryName + Constant.JAVA_FILE_TYPE;

        if (Files.notExists(Paths.get(fileName)) || generate.getOverride()) {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, ENCODING_DEFAULT));
            Velocity.mergeTemplate(
                    generate.getRepository().getTemplate(),
                    ENCODING_DEFAULT,
                    fromObject(repository),
                    writer);
            writer.close();

            getLog().info(fileName + " generated");
        }
    }

    /**
     * 初始化数据库连接
     */
    private void initConnection() throws MojoExecutionException {
        try {
            Class.forName(database.getDatasource().getDriverClass());
            connection = DriverManager.getConnection(
                    database.getDatasource().getUrl(),
                    database.getDatasource().getUsername(),
                    database.getDatasource().getPassword());
        } catch (Exception e) {
            throw new MojoExecutionException("init databaseProperties connection failed", e);
        }
    }

    /**
     * 设置模版引擎，主要指向获取模版路径
     */
    private void initVelocity() {
        Properties p = new Properties();
        p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, "");
        p.setProperty(Velocity.FILE_RESOURCE_LOADER_CACHE, "true");
        p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
    }

    /**
     * 初始化数据库相关配置
     */
    private void initDatabaseConfig() {
        databaseProperties = database.initDatabaseProperties();
    }

    /**
     * 初始化输出目录
     *
     * @return 输出目录
     */
    private String getBaseDir() throws IOException {
        String dir = StringUtils.isEmpty(generate.getBaseDir()) ? "" : generate.getBaseDir();
        dir = (dir.endsWith(File.separator)) ? dir : dir + File.separator;

        if (!StringUtils.isEmpty(generate.getBasePkg())) {
            dir += generate.getBasePkg().replaceAll(Constant.ESCAPE_DOT, File.separator);
            dir += File.separator;
        }

        Files.createDirectories(Paths.get(dir));

        return dir;
    }

    /**
     * 判断表是否需要生成
     *
     * @param tableName 表名
     * @return 是或否
     */
    private boolean needGenerate(String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            return false;
        }

        if (database.getIncludes() != null && database.getIncludes().size() > 0) {
            for (String include : database.getIncludes()) {
                if (include.equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
            return false;
        }

        if (database.getExcludes() != null && database.getExcludes().size() > 0) {
            for (String exclude : database.getExcludes()) {
                if (exclude.equalsIgnoreCase(tableName)) {
                    return false;
                }
            }
            return true;
        }

        return true;
    }

    /**
     * 去除表的前缀
     *
     * @param tableName 表名
     * @return 去除前缀后的表明
     */
    private String handleTableName(String tableName) {
        String prefix = database.getTablePrefix();
        if (prefix != null && !(prefix = prefix.trim()).isEmpty()) {
            tableName = tableName.substring(prefix.length());
        }
        return tableName;
    }

    /**
     * 将对象转成模板上下文
     *
     * @param obj 对象
     * @return 上下文
     */
    private Context fromObject(Object obj) throws IllegalAccessException {
        Map<String, Object> context = new HashMap<>(16);
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            context.put(field.getName(), field.get(obj));
        }
        return new VelocityContext(context);
    }

    /**
     * 获取实体的包名
     *
     * @return 实体的包名
     */
    private String getEntityPkg() {
        return getBasePkg() + generate.getEntity().getPkg();
    }

    /**
     * 获取DAO的包名
     *
     * @return DAO的包名
     */
    private String getSpringRepositoryPkg() {
        return getBasePkg() + generate.getRepository().getPkg();
    }

    /**
     * 获取根包
     *
     * @return 根包
     */
    private String getBasePkg() {
        return StringUtils.isEmpty(generate.getBasePkg()) ? "" : generate.getBasePkg().trim() + Constant.DOT;
    }

    /**
     * 初始化生成实体时需要忽略的字段
     */
    private void initSuperClassField() {
        excludeFields = new HashSet<>();

        if (generate.getEntity().getSuperClass() == null || generate.getEntity().getSuperClass() == null) {
            return;
        }

        List<String> configFields = generate.getEntity().getExcludeFields();
        if (configFields != null) {
            excludeFields.addAll(configFields);
        }

        if (generate.getEntity().getOverrideSuperClassField()) {
            return;
        }
        try {
            String sourceFile = generate.getEntity().getSuperClass();
            sourceFile = sourceFile.replaceAll(Constant.ESCAPE_DOT, File.separator);
            String projectPath = Paths.get("").toAbsolutePath().toString();
            sourceFile = projectPath + Constant.SOURCE_BASE_DIR + sourceFile + Constant.JAVA_FILE_TYPE;

            Path sourceFilePath = Paths.get(sourceFile);
            if (Files.notExists(sourceFilePath)) {
                getLog().warn("super class source file not exists : " + sourceFile);
                return;
            }
            Files.readAllLines(sourceFilePath).stream()
                    .filter(line -> pattern.matcher(line).matches())
                    .forEach(line -> {
                        line = line.replace(Constant.SEMICOLON, "");
                        line = line.split(Constant.EQUAL)[0];
                        line = line.trim();
                        line = line.substring(line.lastIndexOf(Constant.SPACE) + 1);
                        excludeFields.add(line);

                        getLog().info("find super class field : " + line);
                    });
        } catch (Exception e) {
            getLog().warn("super class not found, ignore");
            getLog().warn(e);
        }
    }

    /**
     * 解析一个类型是否需要引入
     *
     * @param imports 引入列表
     * @param type    类型全称
     * @return 类型简称
     */
    private String resolveImport(Set<String> imports, String type) {
        int index = type.lastIndexOf(Constant.DOT);
        if (index < 0) {
            return type;
        }
        imports.add(type);
        return type.substring(index + 1);
    }
}
