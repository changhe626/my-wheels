package com.onyx.spring.ioc2;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描所有的bean,查找标记有@bean的类
 */
public class ProjectScanner {

    /**
     * 保存所有的class文件的路径
     */
    private static List<String> classPaths = new ArrayList<String>();

    /**
     * 保存所有的生成的class,单例
     */
    public static Map<String, Class> beans = new HashMap<>();


    public static void init(String path) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
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
                Bean bean = (Bean) annotation;
                String name = bean.name();
                if (StringUtils.isNoneBlank(name)) {
                    beans.put(name, clazz);
                } else {
                    int indexOf = s.lastIndexOf(".");
                    String substring = s.substring(indexOf+1, s.length());
                    String s1 = toLowerFirstLetter(substring);
                    beans.put(s1, clazz);
                }
            }
        }
    }


    /**
     * 该方法会得到所有的类，将类的绝对路径写入到classPaths中
     *
     * @param file
     */
    private static void doPath(File file) {
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
    private static String toLowerFirstLetter(String string) {
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
