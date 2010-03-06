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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This analyzer checks if there are uses of for each loops with iterable objects
 * following the guideline:<br/>
 * @see <a href="http://developer.android.com/guide/practices/design/performance.html#foreach">Use Enhanced For Loop Syntax With Caution</a>
 * @author Mattia Gustarini
 */
public final class EnhancedForLoopAnalyzer extends SpecificAnalyzer {
	private static final String ITERATOR_CLASS = "java/util/Iterator";

	public EnhancedForLoopAnalyzer() {
		super("Enhanced For Loop", "Use for each loops with caution, because it creates additional objects (i.e. Iterator)");
	}

	/* (non-Javadoc)
	 * @see ch.usi.inf.gustarim.apt.analysis.SpecificAnalyzer#analyzeMethod(org.objectweb.asm.tree.MethodNode)
	 */
	@Override
	protected Collection<Problem> analyzeMethod(MethodNode methodNode) {
		final LinkedList<Problem> problems = new LinkedList<Problem>();
		final LinkedList<Problem> iterators = new LinkedList<Problem>();
		final InsnList instructions = methodNode.instructions;
		for (int i = 0; i < instructions.size(); i++) {
			final AbstractInsnNode insnNode = instructions.get(i);
			if (insnNode.getOpcode() == Opcodes.INVOKEINTERFACE) {
				final MethodInsnNode interfaceInst = (MethodInsnNode)insnNode;
				if (interfaceInst.owner.equals(ITERATOR_CLASS)) {
					final Problem problem = new Problem(insnNode);
					if (iterators.contains(problem)) {
						iterators.remove(problem);
						problems.add(problem);
					}
					else {
						iterators.add(problem);						
					}
				}
			}
		}
		return problems;
	}

}
