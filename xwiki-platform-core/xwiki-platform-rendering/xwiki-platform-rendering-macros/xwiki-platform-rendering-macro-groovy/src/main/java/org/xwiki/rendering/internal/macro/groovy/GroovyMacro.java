/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.macro.groovy;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.macro.script.AbstractJSR223ScriptMacro;
import org.xwiki.rendering.macro.script.JSR223ScriptMacroParameters;

/**
 * Execute script in provided script language.
 * 
 * @version $Id$
 * @since 1.7M3
 */
@Component
@Named("groovy")
@Singleton
public class GroovyMacro extends AbstractJSR223ScriptMacro<JSR223ScriptMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "Execute a groovy script.";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "the groovy script to execute";

    /**
     * Create and initialize the descriptor of the macro.
     */
    public GroovyMacro()
    {
        super("Groovy", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION));
    }
}
