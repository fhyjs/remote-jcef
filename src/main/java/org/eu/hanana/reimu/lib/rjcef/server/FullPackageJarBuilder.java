package org.eu.hanana.reimu.lib.rjcef.server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class FullPackageJarBuilder {

    /**
     * 扫描某个包路径及其子目录，将所有文件（class + 资源）打包到 JAR
     *
     * @param packageName 包名，例如 org.eu.hanana
     * @param out 输出流（可以是文件或网络流）
     * @param mainClass 可选 Main-Class
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void buildJarFromPackage(String packageName, OutputStream out, String mainClass) throws IOException, URISyntaxException {
        JarOutputStream jos = new JarOutputStream(out);

        // 先写 Manifest
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        if (mainClass != null && !mainClass.isEmpty()) {
            manifest.getMainAttributes().putValue("Main-Class", mainClass);
        }
        jos.putNextEntry(new JarEntry("META-INF/"));
        jos.closeEntry();
        jos.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
        manifest.write(jos);
        jos.closeEntry();

        String path = packageName.replace('.', '/');

        // 获取类路径资源
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            if ("file".equals(url.getProtocol())) {
                // 类在目录
                Path dir = Paths.get(url.toURI());
                Files.walk(dir).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        try {
                            String entryName = path + "/" + dir.relativize(filePath).toString().replace(File.separatorChar, '/');
                            JarEntry entry = new JarEntry(entryName);
                            jos.putNextEntry(entry);
                            Files.copy(filePath, jos);
                            jos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if ("jar".equals(url.getProtocol())) {
                // 类在 jar 内
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry je = entries.nextElement();
                        if (je.getName().startsWith(path) && !je.isDirectory()) {
                            JarEntry newEntry = new JarEntry(je.getName());
                            jos.putNextEntry(newEntry);
                            try (InputStream is = jarFile.getInputStream(je)) {
                                is.transferTo(jos);
                            }
                            jos.closeEntry();
                        }
                    }
                }
            }
        }

        jos.close();
    }

    public static void main(String[] args) throws Exception {
        try (FileOutputStream fos = new FileOutputStream("fullPackage.jar")) {
            buildJarFromPackage("org.eu.hanana", fos, null);
            System.out.println("JAR 创建完成！");
        }
    }
}
