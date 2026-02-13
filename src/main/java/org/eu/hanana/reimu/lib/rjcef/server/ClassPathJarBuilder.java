package org.eu.hanana.reimu.lib.rjcef.server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class ClassPathJarBuilder {

    /**
     * 将指定类所在的类路径的所有文件复制到 JarOutputStream
     *
     * @param clazz 目标类
     * @param jos JarOutputStream
     * @param mainClass 可选 Main-Class
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void copyClassPathToJar(Class<?> clazz, FileOutputStream outJarFile, String mainClass) throws IOException, URISyntaxException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        if (mainClass != null) {
            manifest.getMainAttributes().putValue("Main-Class", mainClass);
        }

        try (JarOutputStream jos = new JarOutputStream(outJarFile, manifest)) {
            String classResourcePath = clazz.getName().replace('.', '/') + ".class";
            URL classUrl = clazz.getClassLoader().getResource(classResourcePath);
            if (classUrl == null) throw new IOException("Cannot find class resource: " + clazz.getName());

            if ("file".equals(classUrl.getProtocol())) {
                Path classFile = Paths.get(classUrl.toURI());
                Path baseDir = classFile;
                for (int i = 0; i < clazz.getName().split("\\.").length; i++) baseDir = baseDir.getParent();
                Path finalBaseDir = baseDir;

                Files.walk(baseDir).forEach(path -> {
                    if (Files.isRegularFile(path)) {
                        try {
                            String entryName = finalBaseDir.relativize(path).toString().replace(File.separatorChar, '/');
                            jos.putNextEntry(new JarEntry(entryName));
                            Files.copy(path, jos);
                            jos.closeEntry();
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                });
            } else if ("jar".equals(classUrl.getProtocol())) {
                String jarPath = classUrl.getPath().substring(5, classUrl.getPath().indexOf("!"));
                try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry je = entries.nextElement();
                        if (!je.isDirectory() && !je.getName().equalsIgnoreCase("META-INF/MANIFEST.MF")) {
                            JarEntry newEntry = new JarEntry(je.getName());
                            jos.putNextEntry(newEntry);
                            try (InputStream is = jarFile.getInputStream(je)) {
                                is.transferTo(jos);
                            }
                            jos.closeEntry();
                        }
                    }
                }
            } else {
                throw new IOException("Unsupported protocol: " + classUrl.getProtocol());
            }
        }
    }


    public static void main(String[] args) throws Exception {
        try (FileOutputStream fos = new FileOutputStream("output.jar")) {

            // 将某个类所在的类路径完整复制到 JAR
            copyClassPathToJar(ClassPathJarBuilder.class, fos, "ClassPathJarBuilder");
        }
        System.out.println("Jar 已生成！");
    }
}
