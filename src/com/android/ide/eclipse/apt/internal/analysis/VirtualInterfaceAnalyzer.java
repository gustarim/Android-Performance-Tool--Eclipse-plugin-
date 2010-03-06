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
import org.objectweb.asm.tree.MethodNode;

/**
 * This analyzer check if are present INVOKE_INTERFACE call
 * following the guideline:<br/>
 * @see <a href="http://developer.android.com/guide/practices/design/performance.html#prefer_virtual">Prefer Virtual Over Interface</a> 
 * @author Mattia Gustarini
 *
 */
public final class VirtualInterfaceAnalyzer extends SpecificAnalyzer {
	
	public VirtualInterfaceAnalyzer() {
		super("Prefer Virtual Over Interface", "If it is possible, change the interface type of the object to a concrete type");
	}

	/* (non-Javadoc)
	 * @see ch.usi.inf.gustarim.apt.analysis.SpecificAnalyzer#analyzeMethod(org.objectweb.asm.tree.MethodNode)
	 */
	@Override
	protected Collection<Problem> analyzeMethod(final MethodNode methodNode) {
		final LinkedList<Problem> problems = new LinkedList<Problem>();
		final InsnList instructions = methodNode.instructions;
		for (int i = 0; i < instructions.size(); i++) {
			final AbstractInsnNode insnNode = instructions.get(i);
			if (insnNode.getOpcode() == Opcodes.INVOKEINTERFACE) {
				final Problem problem = new Problem(insnNode);
				problems.add(problem);
			}
		}
		return problems;
	}
}
