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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This analyzer checks if are present FLOAT operations
 * following the guideline:<br/>
 * @see <a href="http://developer.android.com/guide/practices/design/performance.html#avoidfloat">Avoid Float</a>
 * @author Mattia Gustarini
 *
 */
public final class FloatAnalyzer extends SpecificAnalyzer implements Opcodes {
	private static final String FLOAT_CLASS = "java/lang/Float";
	
	/**
	 * The array storing all the possible float operations
	 */
	private static final int[] FLOAT_OPERATIONS = {
			F2D, F2I, F2L, FADD, FALOAD, FASTORE, FCMPG, FCMPL, FCONST_0,
			FCONST_1, FCONST_2, FDIV, FLOAD, FMUL, FNEG, FREM, FRETURN, FSTORE, FSUB
	};

	public FloatAnalyzer() {
		super("Float", "Avoid Float operations: they are expensive in embeded systems!");
		Arrays.sort(FLOAT_OPERATIONS);
	}

	/* (non-Javadoc)
	 * @see ch.usi.inf.gustarim.apt.analysis.SpecificAnalyzer#analyzeMethod(org.objectweb.asm.tree.MethodNode)
	 */
	@Override
	protected Collection<Problem> analyzeMethod(final MethodNode methodNode) {
		final Collection<Problem> problems = new LinkedList<Problem>();
		final InsnList instructions = methodNode.instructions;
		for (int i = 0; i < instructions.size(); i++) {
			final AbstractInsnNode instruction = instructions.get(i);
			final int res = Arrays.binarySearch(FLOAT_OPERATIONS, instruction.getOpcode());
			boolean isProblem = res >= 0;
			if (!isProblem && instruction.getType() == AbstractInsnNode.METHOD_INSN) {
				final String owner = ((MethodInsnNode)instruction).owner;
				isProblem = FLOAT_CLASS.equals(owner);
			}
			if (isProblem) {
				final Problem problem = new Problem(instruction);
				if (!problems.contains(problem)) {
					problems.add(problem);
				}
			}
		}
		return problems;
	}

}
