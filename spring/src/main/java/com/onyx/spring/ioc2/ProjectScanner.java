package com.onyx.spring.ioc2;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描所有的bean,查找标记有@bean的类
 */
public class ProjectScanner {

    private String path;

    public ProjectScanner(String path) {
        this.path = path;
    }

    /**
     * 保存所有的class文件的路径
     */
    private List<String> classPaths = new ArrayList<String>();

    /**
     * 保存所有的生成的class,单例
     */
    public Map<String, Object> beans = new HashMap<>();


    public void init() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //先把包名转换为路径,首先得到项目的classpath
        String classpath = ProjectScanner.class.getResource("/").getPath();
        //然后把我们的包名basPach转换为路径名
        path = path.replace(".", File.separator);
        //然后把classpath和basePack合并
        String searchPath = classpath + path;
        doPath(new File(searchPath));
        //这个时候我们已经得到了指定包下所有的类的绝对路径了。我们现在利用这些绝对路径和java的反射机制得到他们的类对象
        for (String s : classPaths) {
            //把 D:\work\code\20170401\search-class\target\classes\com\baibin\search\a\A.class 这样的绝对路径转换为全类名com.baibin.search.a.A
            s = s.replace(classpath.replace("/", "\\").replaceFirst("\\\\", ""), "").replace("\\", ".").replace(".class", "");
            Class clazz = Class.forName(s);
            Annotation annotation = clazz.getDeclaredAnnotation(Bean.class);
            if (annotation != null) {
                if (annotation instanceof Bean) {
                    Object o = clazz.newInstance();
                    Bean bean = (Bean) annotation;
                    String name = bean.name();
                    if (StringUtils.isNoneBlank(name)) {
                        beans.put(name, o);
                    } else {
                        int indexOf = s.lastIndexOf(".");
                        String substring = s.substring(indexOf + 1, s.length());
                        String s1 = toLowerFirstLetter(substring);
                        beans.put(s1, o);
                    }
                }
            }
        }
        System.out.println(beans);
    }


    /**
     * 进行属性的注入...
     */
    public void injectProperty() throws IllegalAccessException, InstantiationException {
        for (Object value : beans.values()) {
            Field[] fields = value.getClass().getDeclaredFields();
            for (Field field : fields) {
                Autowired annotation = field.getAnnotation(Autowired.class);
                if (annotation == null) {
                    continue;
                }
                if (annotation instanceof Autowired) {
                    Autowired autowired = annotation;
                    String beanName = autowired.beanName();
                    field.setAccessible(true);
                    if (StringUtils.isBlank(beanName)) {
                        beanName = field.getName();
                    }
                    Object o = beans.get(beanName);
                    if (o == null) {
                        throw new RuntimeException("没有找到名字是:" + beanName + "的bean");
                        //第二个地方查找
                    }
                    field.set(value, o);
                }
            }
        }
        System.out.println(beans);

    }


    /**
     * 该方法会得到所有的类，将类的绝对路径写入到classPaths中
     *
     * @param file
     */
    private void doPath(File file) {
        if (file.isDirectory()) {
            //文件夹我们就递归
            File[] files = file.listFiles();
            for (File f1 : files) {
                doPath(f1);
            }
        } else {
            //标准文件
            //标准文件我们就判断是否是class文件
            if (file.getName().endsWith(".class")) {
                //如果是class文件我们就放入我们的集合中。
                classPaths.add(file.getPath());
            }
        }
    }


    /**
     * 首字母变成小写
     *
     * @param string
     * @return
     */
    private String toLowerFirstLetter(String string) {
        if (StringUtils.isNoneBlank(string)) {
            String lowerCase = String.valueOf(string.charAt(0)).toLowerCase();
            StringBuilder builder = new StringBuilder(lowerCase);
            builder.append(string.substring(1, string.length()));
            return builder.toString();
        } else {
            return null;
        }
    }

}
