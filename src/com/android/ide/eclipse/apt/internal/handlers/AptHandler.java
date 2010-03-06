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
package com.android.ide.eclipse.apt.internal.handlers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.objectweb.asm.tree.ClassNode;

import com.android.ide.eclipse.apt.AptPlugin;
import com.android.ide.eclipse.apt.internal.analysis.Analyzer;
import com.android.ide.eclipse.apt.internal.crawler.ClassCrawler;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AptHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public AptHandler() {
	}

	/**
	 * the command has been executed, so extract the needed information
	 * from the application context.
	 */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			final ISelection selection = page.getSelection();
			final IResource resource = extractSelection(selection);
			if (resource != null) {
				final IProject project = resource.getProject();
				try {
					if (!project.isOpen()) {
						project.open(null);
					}
					
					if (project.hasNature(AptPlugin.ANDROID_NATURE)) {
						final IFolder bin = project.getFolder(AptPlugin.CLASS_FOLDER);
						if (bin.exists()) {
							final String projectPath = project.getLocation().toOSString();
							final Collection<ClassNode> classes = ClassCrawler.scanAndroidProject(projectPath + "/" + AptPlugin.CLASS_FOLDER);
							Analyzer.analyseProject(project, classes);
						}
						else {
							MessageDialog.openInformation(
									window.getShell(),
									"Apt",
									"The bin folder cannot be found in the " +
									"Android project: " + project.getName());
						}
					}
					else {
						MessageDialog.openInformation(
								window.getShell(),
								"Apt",
								"The project " + project.getName() +
								" is not an Android project");
					}
				} catch (final CoreException e) {
					MessageDialog.openInformation(
							window.getShell(),
							"Apt",
							"An eclipse core error occured during the anlysis of the " +
							"Android project: " + project.getName());
					e.printStackTrace();
					return null;
				} catch (final FileNotFoundException e) {
					MessageDialog.openInformation(
							window.getShell(),
							"Apt",
							"The bin folder cannot be found in the " +
							"Android project: " + project.getName());
					e.printStackTrace();
					return null;
				} catch (final IOException e) {
					MessageDialog.openInformation(
							window.getShell(),
							"Apt",
							"An IO error occured during the anlysis of the " +
							"Android project: " + project.getName());
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}
	
	private IResource extractSelection(final ISelection sel) {
		if (!(sel instanceof IStructuredSelection)) {
			return null;
		}
		
		final IStructuredSelection ss = (IStructuredSelection) sel;
		final Object element = ss.getFirstElement();
		
		if (element instanceof IResource) {
			return (IResource) element;
		}
		
		if (!(element instanceof IAdaptable)) {
			return null;
		}
		
		final IAdaptable adaptable = (IAdaptable)element;
		final Object adapter = adaptable.getAdapter(IResource.class);
		return (IResource) adapter;
	}
}
