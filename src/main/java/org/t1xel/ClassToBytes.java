package org.t1xel;

import java.io.*;
import java.util.*;

public class ClassToBytes {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Not enough args");
            return;
        }

        File directory = new File(args[0]);

        if (!directory.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        if (!directory.isDirectory()) {
            System.out.println("File is not a directory.");
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".class"));
        if (files == null || files.length == 0) {
            System.out.println("Folder does not have files.");
            return;
        }

        List<ClassPair> classes = new ArrayList<>();
        Map<String, ClassPair> classMap = new HashMap<>();

        for (File file : files) {
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[16384];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                byte[] classData = byteArrayOutputStream.toByteArray();
                ClassPair classPair = new ClassPair(classData);
                classPair.classInfo = new ClassInfo(classData);
                classes.add(classPair);
                classMap.put(classPair.classInfo.name, classPair);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        for (ClassPair classPair : classes) {
            ClassInfo classInfo = classPair.classInfo;
            ClassPair superClassPair = classMap.get(classInfo.superClass);
            if (superClassPair != null) {
                superClassPair.priority = Math.max(superClassPair.priority, classPair.priority + 1);
            }
            for (String interfaceName : classInfo.interfaces) {
                ClassPair interfacePair = classMap.get(interfaceName);
                if (interfacePair != null) {
                    interfacePair.priority = Math.max(interfacePair.priority, classPair.priority + 1);
                }
            }
        }

        System.out.println("Loaded " + classes.size() + " classes. Sorting..");
        Collections.sort(classes);
        classes.forEach(classPair -> System.out.println(classPair.classInfo.name));

        System.out.println("Sorted. Saving header file...");
        for (ClassPair classPair : classes) {
            String className = classPair.classInfo.name.replace('.', '_');
            String headerFileName = className + ".h";
            try (PrintStream printStream = new PrintStream(headerFileName)) {
                printStream.println("#include \"jni.h\"");
                printStream.print("const unsigned char " + className + "[]={");

                byte[] classData = classPair.classData;
                for (int i = 0; i < classData.length; i++) {
                    printStream.printf("0x%02X", classData[i]);
                    if (i < classData.length - 1) {
                        printStream.print(",");
                    }
                    if ((i + 1) % 32 == 0) {
                        printStream.println();
                    }
                }
                printStream.println("};");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        System.out.println("Done!");
        System.exit(0);
    }
}
