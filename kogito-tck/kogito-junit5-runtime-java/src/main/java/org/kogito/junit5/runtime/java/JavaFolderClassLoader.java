/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kogito.junit5.runtime.java;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class JavaFolderClassLoader extends ClassLoader {

    private Path path;

    public JavaFolderClassLoader(ClassLoader parentClassLoader, Path path) {
        this.path = path;
    }

    @Override
    public Class<?> loadClass(String name)
            throws ClassNotFoundException {
        Class<?> clazz = getClass(name);
        return clazz != null ? clazz : super.loadClass(name);
    }

    private Class<?> getClass(String name)
            throws ClassNotFoundException {
        String file = name.replace('.', File.separatorChar) + ".class";
        try {
            byte[] clazzBytes = loadClassData(file);
            Class<?> c = defineClass(name, clazzBytes, 0, clazzBytes.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] loadClassData(String name) throws IOException {
        // Opening the file
        InputStream stream = new FileInputStream(path.resolve(name).toFile());
        int size = stream.available();
        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        // Reading the binary data
        in.readFully(buff);
        in.close();
        return buff;
    }

    public void destroy() {
        try {
            // create a stream
            Stream<Path> files = Files.walk(path);

            files.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::deleteOnExit);

            // close the stream
            files.close();

        } catch (IOException ex) {
            // do nothing
        }
    }
}
