package org.codehaus.jdt.groovy.internal.compiler.ast;

import groovy.lang.GroovyClassLoader;

import java.util.List;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * Grails runs an ASTTransform org.codehaus.groovy.grails.compiler.injection.GlobalPluginAwareEntityASTTransformation. But this
 * transform doesn't execute when compiler is called from within STS/Eclipse because it requires plugin information inside of
 * GrailsBuildSettings to be intialized and present in BuildSettingsHolder. All of this is finicky and fragile to setup. So instead,
 * this somewhat hacky workaround in the Groovy Eclipse compiler does the same thing as the tranforms.
 * 
 * @author Kris De Volder
 */
public class GrailsGlobalPluginAwareEntityInjector extends PrimaryClassNodeOperation {

	private static final boolean DEBUG = false;

	private static void debug(String msg) {
		System.out.println(msg);
	}

	private static class PluginInfo {

		final String name;
		final String version;

		public PluginInfo(String name, String version) {
			this.name = name;
			this.version = version;
		}

		@Override
		public String toString() {
			return "Plugin(name=" + name + ", version=" + version + ")";
		}

	}

	private GroovyClassLoader groovyClassLoader;

	// If true then some part of injector has broken down so avoid trying again
	private boolean broken = false;

	public GrailsGlobalPluginAwareEntityInjector(GroovyClassLoader groovyClassLoader) {
		this.groovyClassLoader = groovyClassLoader;
	}

	@Override
	public void call(SourceUnit _sourceUnit, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
		if (broken) {
			return;
		}
		if (_sourceUnit instanceof EclipseSourceUnit) {
			EclipseSourceUnit sourceUnit = (EclipseSourceUnit) _sourceUnit;
			try {
				if (classNode.isAnnotationDefinition()) {
					return;
				}

				if (!isFirstClassInModule(classNode)) {
					// The Grails version of the transform only walk the first class in a module
					return;
				}

				IFile file = sourceUnit.getEclipseFile();
				PluginInfo info = getInfo(file);
				if (info != null) {
					if (DEBUG) {
						debug("APPLY transform: " + classNode);
					}

					// The transform should be applied. (code below lifted from
					// org.codehaus.groovy.grails.compiler.injection.GlobalPluginAwareEntityASTTransformation)
					Class<?> GrailsPlugin_class = Class.forName("org.codehaus.groovy.grails.plugins.metadata.GrailsPlugin", false,
							groovyClassLoader);

					final ClassNode annotation = new ClassNode(GrailsPlugin_class);
					final List<?> list = classNode.getAnnotations(annotation);
					if (!list.isEmpty()) {
						return;
					}

					final AnnotationNode annotationNode = new AnnotationNode(annotation);
					annotationNode.addMember("name", new ConstantExpression(info.name));
					annotationNode.addMember("version", new ConstantExpression(info.version));
					annotationNode.setRuntimeRetention(true);
					annotationNode.setClassRetention(true);

					classNode.addAnnotation(annotationNode);
				} else {
					if (DEBUG) {
						debug("SKIP transform: " + classNode);
					}
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
				broken = true;
			}
		}
	}

	private boolean isFirstClassInModule(ClassNode classNode) {
		ModuleNode module = classNode.getModule();
		if (module != null) {
			List<ClassNode> classes = module.getClasses();
			if (classes != null && classes.size() > 0) {
				return classes.get(0) == classNode;
			}
		}
		return false;
	}

	public static PluginInfo getInfo(IFile file) {
		if (file == null) {
			return null;
		}
		IPath path = file.getFullPath();
		// The path is expected to have this form
		// Example:
		// /test-pro/.link_to_grails_plugins/audit-logging-0.5.4/grails-app/controllers/org/codehaus/groovy/grails/plugins/orm/auditable/AuditLogEventController.groovy
		// Pattern:
		// /<project-name>/.link_to_grails_plugins/<plugin-name>-<plugin-version>/<the-rest-of-it>

		// Also stuff in the 'test' folder is excluded from the transform
		// See grails.util.PluginBuildSettings.getPluginInfoForSource(String)
		// Test folder path looks like:
		// /<project-name>/.link_to_grails_plugins/<plugin-name>-<plugin-version>/test/<the-rest-of-it>
		if (path != null) {
			if (path.segmentCount() > 3) {
				String link = path.segment(1);
				if (link.equals(".link_to_grails_plugins")) { // Same as in JDT SourceFile
					String pluginNameAndVersion = path.segment(2);
					int split = findVersionDash(pluginNameAndVersion);
					if (split >= 0) {
						if ("test".equals(path.segment(3))) {
							// Exclude "test" folder in plugins
							return null;
						} else {
							// Pattern matched, extract relevant info.
							return new PluginInfo(pluginNameAndVersion.substring(0, split),
									pluginNameAndVersion.substring(split + 1));
						}
					}
				}
			}
		}
		// If the expected pattern isn't found, no info is extracted
		// => the transform will not apply.

		return null;
	}

	/**
	 * Find position of the dash separating plugin name from version.
	 * 
	 * @return position of dash or -1 if dash not found.
	 */
	private static int findVersionDash(String pluginNameAndVersion) {
		int split = pluginNameAndVersion.lastIndexOf('-');
		if (pluginNameAndVersion.endsWith("-SNAPSHOT")) {
			split = pluginNameAndVersion.lastIndexOf('-', split - 1);
		}
		return split;
	}
}
