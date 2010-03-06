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
package com.android.ide.eclipse.apt.internal.analysis;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * The base class for all the analyzers</br>
 * It implements the methods that are common to all the analyzers.
 * @author Mattia Gustarini
 *
 */
abstract class SpecificAnalyzer {
	/**
	 * Inner class representing the problem encountered by
	 * an analyzer
	 */
	protected class Problem {
		int mLine;
		
		public Problem(final AbstractInsnNode insnNode) {
			mLine = findLine(insnNode);
		}
		
		/**
		 * Utility method used to find the line in the source code (java source code file)
		 * from the instruction node provided by ASM.
		 * @param insnNode The current instruction node
		 * @return The line in the source file related to the given instruction node
		 */
		protected int findLine(final AbstractInsnNode insnNode) {
			int line = 0;
			if (insnNode.getType() != AbstractInsnNode.LINE) {
				AbstractInsnNode lineNode = insnNode.getPrevious();
				while (lineNode.getType() != AbstractInsnNode.LINE) {
					lineNode = lineNode.getPrevious();
				}
				line = ((LineNumberNode)lineNode).line;
			}
			else {
				line = ((LineNumberNode)insnNode).line;
			}
			return line;
		}
		
		/**
		 * Given a resource (java source file) it created and place the needed marker related to the 
		 * current problem instance
		 * @param resource
		 */
		public void setMarker(final IResource resource) {
			try {
				final IMarker marker = resource.createMarker(IMarker.PROBLEM);
				if (marker.exists()) {
					marker.setAttribute(IMarker.MESSAGE, mName + ": " + mProblemDescription);
					marker.setAttribute(IMarker.LINE_NUMBER, mLine);
					marker.setAttribute(IMarker.TRANSIENT, true);
					marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
				}
			} catch (final CoreException e) {
				  setMarker(resource);
				  //retry...
				  //I know can lead to a loop...
			}
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			return obj instanceof Problem && ((Problem)obj).mLine == mLine;
		}
	}
	
	/**
	 * The collection of problems found by the analyzer
	 */
	private Collection<Problem> mProblems;
	/**
	 * The name of the analyzer
	 */
	private final String mName;
	/**
	 * The description of the problem related to the analyzer
	 */
	private final String mProblemDescription;
	
	public SpecificAnalyzer(final String name, final String problemDesc) {
		mName = name;
		mProblemDescription = problemDesc;
		mProblems = new LinkedList<Problem>();
	}
	
	/**
	 * Starts the analysis of the specific analyzer
	 * @param classNode The current class node to be analyzed
	 */
	public void analyse(final ClassNode classNode) {
		mProblems = new LinkedList<Problem>();
		final Collection<Problem> foundProblems = analyzeClass(classNode);
		mProblems.addAll(foundProblems);
	}
	
	/**
	 * Get the encountered problems of this specific analyzer
	 * @return
	 */
	public Collection<Problem> getProblems() {
		return mProblems;
	}
	
	/**
	 * Analyze a class</br>
	 * the method can be reimplemented by a specific analyzer if it wants to perform
	 * other analysis rather then the analysis of each method present in it 
	 * @param classNode - the class to be analyzed by the specific analyzer
	 * @return a collection of problems related to the specific analyzer
	 */
	@SuppressWarnings("unchecked")
	protected Collection<Problem> analyzeClass(final ClassNode classNode) {
		final Collection<Problem> problems = new LinkedList<Problem>();
		final List<MethodNode> methodNodes = (List<MethodNode>)classNode.methods;
		//analyze each method present in a class node
		for (final MethodNode methodNode : methodNodes) {
			final Collection<Problem> foundProblems = analyzeMethod(methodNode);
			problems.addAll(foundProblems);
		}
		return problems;
	}
	
	/**
	 * Analyze a method</br>
	 * Abstract method that must be implemented by the specific analyzer in order to
	 * perform its specific performance check
	 * @param methodNode - the method to be analyzed by the specific analyzer
	 * (must be from the current analyzed classNode passed to the method analyzeClass)
	 * @return a collection of problems related to the specific analyzer
	 */
	protected abstract Collection<Problem> analyzeMethod(final MethodNode methodNode);
}
