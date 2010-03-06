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
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * This analyzer checks if there are virtual methods that can be made static
 * following the guideline:<br/>
 * @see <a href="http://developer.android.com/guide/practices/design/performance.html#prefer_static">Prefer Static Over Virtual</a>
 * @author Mattia Gustarini
 * This analyzer can give false positive in case of Override methods that doesn't access the object instance in its body
 */
public final class StaticVirtualAnalyzer extends SpecificAnalyzer {
	protected final class StaticVirtualProblem extends Problem {

		public StaticVirtualProblem(final AbstractInsnNode insnNode) {
			super(insnNode);
			mLine -= 1;
		}
	}
	
	public StaticVirtualAnalyzer() {
		super("Prefer Static Over Virtual", "If a method doesn't access object fields make it static!");
	}
	
	/* (non-Javadoc)
	 * @see com.android.ide.eclipse.apt.internal.analysis.SpecificAnalyzer#analyzeClass(org.objectweb.asm.tree.ClassNode)
	 */
	@Override
	protected Collection<Problem> analyzeClass(final ClassNode classNode) {
		final Collection<Problem> problems = new LinkedList<Problem>();
		if ((classNode.access & Opcodes.ACC_INTERFACE) == 0) {
			problems.addAll(super.analyzeClass(classNode));
		}
		return problems;
	}

	/* (non-Javadoc)
	 * @see ch.usi.inf.gustarim.apt.analysis.SpecificAnalyzer#analyzeMethod(org.objectweb.asm.tree.MethodNode)
	 */
	@Override
	protected Collection<Problem> analyzeMethod(final MethodNode methodNode) {
		final Collection<Problem> problems = new LinkedList<Problem>();
		final int acc = methodNode.access;
		if ((acc & Opcodes.ACC_STATIC) == 0 && (acc & Opcodes.ACC_ABSTRACT) == 0) {
			final InsnList instructions = methodNode.instructions;
			//find if a method load this (ALOAD 0)
			boolean aload0 = false;
			for (int i = 0; i < instructions.size(); i++) {
				final AbstractInsnNode instruction = instructions.get(i);
				if (instruction.getOpcode() == Opcodes.ALOAD) {
					final VarInsnNode aload = (VarInsnNode)instruction;
					aload0 = aload.var == 0;
					if (aload0) {
						break;
					}
				}
			}
			if (!aload0) {
				final Problem problem = new StaticVirtualProblem(instructions.get(1));
				problems.add(problem);
			}
		}
		return problems;
	}
}
