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
package org.xwiki.classloader.internal.protocol.attachmentjar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.classloader.ExtendedURLStreamHandler;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link AttachmentURLStreamHandler}.
 *  
 * @version $Id$
 * @since 2.0.1
 */
public class AttachmentURLStreamHandlerTest extends AbstractComponentTestCase
{
    private Mockery mockery = new Mockery();
    
    private AttachmentReferenceResolver arf;
    private DocumentAccessBridge dab;
    
    private ExtendedURLStreamHandler handler;
    
    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        this.arf = this.mockery.mock(AttachmentReferenceResolver.class);
        DefaultComponentDescriptor<AttachmentReferenceResolver> descriptorARF =
            new DefaultComponentDescriptor<AttachmentReferenceResolver>();
        descriptorARF.setRole(AttachmentReferenceResolver.class);
        descriptorARF.setRoleHint("current");
        getComponentManager().registerComponent(descriptorARF, this.arf);

        this.dab = this.mockery.mock(DocumentAccessBridge.class);
        DefaultComponentDescriptor<DocumentAccessBridge> descriptorDAB =
            new DefaultComponentDescriptor<DocumentAccessBridge>();
        descriptorDAB.setRole(DocumentAccessBridge.class);
        getComponentManager().registerComponent(descriptorDAB, this.dab);
        
        this.handler = getComponentManager().lookup(ExtendedURLStreamHandler.class, "attachmentjar");
    }

    @Test
    public void testInvalidAttachmentJarURL() throws Exception
    {
        URL url = new URL(null, "http://invalid/url", (URLStreamHandler) this.handler);

        try {
            url.openConnection();
            Assert.fail("Should have thrown an exception here");
        } catch (RuntimeException expected) {
            Assert.assertEquals("An attachment JAR URL should start with [attachmentjar://], got [http://invalid/url]",
                expected.getMessage());
        }
    }

    @Test
    public void testAttachmentJarURL() throws Exception
    {
        URL url = new URL(null, "attachmentjar://Space.Page@filename", (URLStreamHandler) this.handler);
        
        final AttachmentReference attachmentReference = new AttachmentReference("filename",
            new DocumentReference("wiki", "space", "page"));
        mockery.checking(new Expectations() {{
            oneOf(arf).resolve("Space.Page@filename"); will(returnValue(attachmentReference));
            oneOf(dab).getAttachmentContent(attachmentReference);
                will(returnValue(new ByteArrayInputStream("content".getBytes())));
        }});
        
        URLConnection connection = url.openConnection();
        InputStream input = null;
        try {
            connection.connect();
            input = connection.getInputStream();
            Assert.assertEquals("content", IOUtils.toString(input));
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
    
    /**
     * Verify that URL-encoded chars are decoded.
     */
    @Test
    public void testAttachmentJarURLWithEncodedChars() throws Exception
    {
        URL url = new URL(null, "attachmentjar://some%20page", (URLStreamHandler) this.handler);
        
        mockery.checking(new Expectations() {{
            oneOf(arf).resolve("some page");
        }});
        
        url.openConnection();
    }
}