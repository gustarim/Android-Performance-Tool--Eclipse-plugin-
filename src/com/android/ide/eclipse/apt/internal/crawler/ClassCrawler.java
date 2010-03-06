/**
 * Copyright (C) Mattia Gustarini
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.android.ide.eclipse.apt.internal.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * This class retrieves all the classes present in an Android project.
 * @author Mattia Gustarini
 *
 */
public final class ClassCrawler {
	public static Collection<ClassNode> sClassNodes;
	
	/**
	 * Scans an Android project to retrieve all the class files in it.
	 * @param path - The Android project main folder location (can be relative or absolute)
	 * @return A collection of class nodes ready to be used with the ASM library for the analysis
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Collection<ClassNode> scanAndroidProject(final String path) throws FileNotFoundException, IOException {
		final File mainDirectory = new File(path);
		return scanDirectory(mainDirectory);
	}
	
	/**
	 * Scans an Android project directory to retrieve all the class files in it.
	 * @param dir - The directory to be scanned
	 * @return A collection of class nodes ready to be used with the ASM library for the analysis
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Collection<ClassNode> scanDirectory(final File dir) throws FileNotFoundException, IOException {
		final LinkedList<ClassNode> classes = new LinkedList<ClassNode>();
		final File[] files = dir.listFiles();
		for (final File file : files) {
			if (file.isFile()) {
				final String name = file.getName();
				//get all the classes except the class R
				if (name.endsWith(".class") && !name.startsWith("R$") && !name.equals("R.class")) {
					final ClassReader classReader = new ClassReader(new FileInputStream(file));
					final ClassNode classNode = new ClassNode();
					classReader.accept(classNode, 0);
					classes.add(classNode);
				}
			}
			else {
				classes.addAll(scanDirectory(file));
			}
		}
		sClassNodes = classes;
		return classes;
	}
}
