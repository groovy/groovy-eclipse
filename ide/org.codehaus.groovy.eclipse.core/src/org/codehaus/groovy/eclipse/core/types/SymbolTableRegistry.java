 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.types;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newEmptyList;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newLinkedMap;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyAccessibleFieldsTable;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyFieldsTable;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyLocalsTable;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyParametersTable;
import org.codehaus.groovy.eclipse.core.types.impl.JavaAccessibleFieldsTable;

/**
 * Registry for symbol tables.
 * 
 * @author empovazan
 */
public class SymbolTableRegistry {
    private static class SymbolTable implements ISymbolTable,
            IGroovyProjectAware {
        ISymbolTable[] tables;

        public SymbolTable(ISymbolTable[] tables) {
            this.tables = tables;
        }

        public GroovyDeclaration lookup(String name) {
            for (int i = 0; i < tables.length; i++) {
                GroovyDeclaration type = tables[i].lookup(name);
                if (type != null) {
                    return type;
                }
            }

            return null;
        }

        public void setGroovyProject(GroovyProjectFacade project) {
            for (int i = 0; i < tables.length; i++) {
                if (tables[i] instanceof IGroovyProjectAware) {
                    ((IGroovyProjectAware) tables[i])
                            .setGroovyProject(project);
                }
            }
        }
    }

    /**
     * Map of a context id to a class.
     */
    static Map<String, List<Class>> mapIdToTableClasses = newMap();

    static {
        // Note that order is importants in order to preserve fields -> super
        // fields shodowing.
        registerSymbolTable(GroovyLocalsTable.class, new String[] {
                ISourceCodeContext.CONSTRUCTOR_SCOPE,
                ISourceCodeContext.METHOD_SCOPE,
                ISourceCodeContext.CLOSURE_SCOPE });
        registerSymbolTable(GroovyParametersTable.class, new String[] {
                ISourceCodeContext.CONSTRUCTOR_SCOPE,
                ISourceCodeContext.METHOD_SCOPE,
                ISourceCodeContext.CLOSURE_SCOPE });
        registerSymbolTable(GroovyFieldsTable.class, new String[] {
                ISourceCodeContext.CONSTRUCTOR_SCOPE,
                ISourceCodeContext.METHOD_SCOPE,
                ISourceCodeContext.CLOSURE_SCOPE });
        registerSymbolTable(GroovyAccessibleFieldsTable.class,
                new String[] { ISourceCodeContext.CLASS });
        registerSymbolTable(JavaAccessibleFieldsTable.class,
                new String[] { ISourceCodeContext.CLASS });
    }

    /**
     * Create all symbol tables that are valid for the given context. Any tables
     * that implement IContextAware are passed the context.
     * 
     * @param contexts
     * @return
     */
    private static ISymbolTable[] createSymbolTables(ISourceCodeContext context) {
        List<ISymbolTable> results = newEmptyList();
        List tableClasses = mapIdToTableClasses.get(context.getId());
        if (tableClasses != null) {
            for (Iterator iter = tableClasses.iterator(); iter.hasNext();) {
                Class tableClass = (Class) iter.next();
                try {
                    ISymbolTable table = newSymbolTable(tableClass);
                    if (table instanceof ISourceCodeContextAware) {
                        ((ISourceCodeContextAware) table)
                                .setSourceCodeContext(context);
                    }
                    results.add(table);
                } catch (InstantiationException e) {
                    GroovyCore.logException("Error creating symbol table", e);
                } catch (IllegalAccessException e) {
                    GroovyCore.logException("Error creating symbol table", e);
                }
            }
        }
        return results.toArray(new ISymbolTable[results.size()]);
    }

    /**
     * Create a symbol table for the given contexts. The table will be made
     * aware of the contexts. It does not however implements
     * {@link ISourceCodeContextAware}. It does implements
     * {@link IGroovyProjectAware} and the project, if any, should be passed to
     * the symbol table before use.
     * 
     * @param contexts
     * @return
     */
    public static ISymbolTable createSymbolTable(ISourceCodeContext[] contexts) {
        return new SymbolTable(createSymbolTables(contexts));
    }

    /**
     * Create all symbol tables that are valid for the given contexts. Any
     * tables that implement {@link ISourceCodeContextAware} are passed the
     * context.
     * 
     * @param contexts
     * @return
     */
    private static synchronized ISymbolTable[] createSymbolTables(
            ISourceCodeContext[] contexts) {
        // A linked hash map is used so that newer symbol tables of the same
        // class will replace older ones.
        // The result is in order of creation, which is important due to the way
        // default tables are registered.
        // This is a rare occurance when a symbol table is registered for
        // contexts which appear in a context path
        // at the same time, e.g. fields tables could be in method scope and in
        // class scope.
        final Map<Class, ISymbolTable> mapClassToInstance = newLinkedMap();
        for (int i = 0; i < contexts.length; ++i) {
            ISymbolTable[] tables = createSymbolTables(contexts[i]);
            for (int j = 0; j < tables.length; j++) {
                mapClassToInstance.put(tables[j].getClass(), tables[j]);
            }
        }
        ISymbolTable[] result = new ISymbolTable[mapClassToInstance.size()];
        int ix = 0;
        for (Iterator iter = mapClassToInstance.values().iterator(); iter
                .hasNext(); ++ix) {
            result[ix] = (ISymbolTable) iter.next();
        }
        return result;
    }

    private static synchronized void registerSymbolTable(Class tableClass,
            String[] contextIds) {
        for (int i = 0; i < contextIds.length; ++i) {
            List<Class> tableClasses = mapIdToTableClasses.get(contextIds[i]);
            if (tableClasses == null) {
                tableClasses = newEmptyList();
                mapIdToTableClasses.put(contextIds[i], tableClasses);
            }
            tableClasses.add(tableClass);
        }
    }

    private static ISymbolTable newSymbolTable(Class tableClass)
            throws InstantiationException, IllegalAccessException {
        return (ISymbolTable) tableClass.newInstance();
    }
}
