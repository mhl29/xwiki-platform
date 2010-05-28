package com.xpn.xwiki.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Mock;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.test.AbstractBridgedXWikiComponentTestCase;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;

public class XWikiServletURLFactoryTest extends AbstractBridgedXWikiComponentTestCase
{
    private static final String MAIN_WIKI_NAME = "xwiki";

    private XWikiConfig config;

    private XWikiServletURLFactory urlFactory = new XWikiServletURLFactory();

    private Map<String, Map<String, XWikiDocument>> databases = new HashMap<String, Map<String, XWikiDocument>>();

    private Map<String, XWikiDocument> getDocuments(String database, boolean create) throws XWikiException
    {
        if (!this.databases.containsKey(database)) {
            if (create) {
                this.databases.put(database, new HashMap<String, XWikiDocument>());
            } else {
                throw new XWikiException(XWikiException.MODULE_XWIKI_STORE, XWikiException.ERROR_XWIKI_UNKNOWN,
                    "Database " + database + " does not exists.");
            }
        }

        return this.databases.get(database);
    }

    private XWikiDocument getDocument(DocumentReference documentReference) throws XWikiException
    {
        XWikiDocument document = new XWikiDocument(documentReference);

        Map<String, XWikiDocument> docs = getDocuments(document.getDatabase(), false);

        if (docs.containsKey(document.getFullName())) {
            return docs.get(document.getFullName());
        } else {
            return document;
        }
    }

    private void saveDocument(XWikiDocument document) throws XWikiException
    {
        document.setNew(false);
        Map<String, XWikiDocument> database = getDocuments(document.getDatabase(), true);
        database.remove(document.getFullName());
        database.put(document.getFullName(), document);
    }

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.databases.put(MAIN_WIKI_NAME, new HashMap<String, XWikiDocument>());

        XWiki xwiki = new XWiki()
        {
            public XWikiDocument getDocument(String fullname, XWikiContext context) throws XWikiException
            {
                return XWikiServletURLFactoryTest.this.getDocument(
                    Utils.getComponent(DocumentReferenceResolver.class, "currentmixed").resolve(fullname));
            }

            public XWikiDocument getDocument(DocumentReference documentReference, XWikiContext context)
                throws XWikiException
            {
                return XWikiServletURLFactoryTest.this.getDocument(documentReference);
            }

            public String getXWikiPreference(String prefname, String defaultValue, XWikiContext context)
            {
                return defaultValue;
            }

            protected void registerWikiMacros()
            {

            }
        };
        xwiki.setConfig((this.config = new XWikiConfig()));
        xwiki.setDatabase(getContext().getDatabase());

        Mock mockXWikiRequest = mock(XWikiRequest.class, new Class[] {}, new Object[] {});
        mockXWikiRequest.stubs().method("getServletPath").will(returnValue(""));
        mockXWikiRequest.stubs().method("getContextPath").will(returnValue("/xwiki"));
        mockXWikiRequest.stubs().method("getHeader").will(returnValue(null));

        getContext().setWiki(xwiki);
        getContext().setRequest((XWikiRequest) mockXWikiRequest.proxy());

        XWikiDocument wiki1Doc = getDocument(new DocumentReference(MAIN_WIKI_NAME, "XWiki", "XWikiServerWiki1"));
        BaseObject wiki1Obj = wiki1Doc.newObject("XWiki.XWikiServerClass", getContext());
        wiki1Obj.setStringValue("server", "wiki1server");
        saveDocument(wiki1Doc);

        getContext().setURL(new URL("http://127.0.0.1/xwiki/view/InitialSpace/InitialPage"));

        this.urlFactory.init(getContext());
    }

    public void testCreateURLOnMainWiki() throws MalformedURLException
    {
        URL url = this.urlFactory.createURL("Space", "Page", "view", "param1=1", "anchor", "xwiki", getContext());
        assertEquals(new URL("http://127.0.0.1/xwiki/bin/view/Space/Page?param1=1#anchor"), url);
    }

    public void testCreateURLOnSubWiki() throws MalformedURLException
    {
        URL url = this.urlFactory.createURL("Space", "Page", "view", "param1=1", "anchor", "wiki1", getContext());
        assertEquals(new URL("http://wiki1server/xwiki/bin/view/Space/Page?param1=1#anchor"), url);
    }

    public void testCreateURLOnSubWikiInVirtualMode() throws MalformedURLException
    {
        this.config.setProperty("xwiki.virtual", "1");

        URL url = this.urlFactory.createURL("Space", "Page", "view", "param1=1", "anchor", "wiki1", getContext());
        assertEquals(new URL("http://wiki1server/xwiki/bin/view/Space/Page?param1=1#anchor"), url);
    }

    public void testCreateURLOnMainWikiInPathMode() throws MalformedURLException
    {
        this.config.setProperty("xwiki.virtual.usepath", "1");

        URL url = this.urlFactory.createURL("Space", "Page", "view", "param1=1", "anchor", "xwiki", getContext());
        assertEquals(new URL("http://127.0.0.1/xwiki/bin/view/Space/Page?param1=1#anchor"), url);
    }

    public void testCreateURLOnSubWikiInPathMode() throws MalformedURLException
    {
        this.config.setProperty("xwiki.virtual.usepath", "1");

        URL url = this.urlFactory.createURL("Space", "Page", "view", "param1=1", "anchor", "wiki1", getContext());
        assertEquals(new URL("http://127.0.0.1/xwiki/wiki/wiki1server/view/Space/Page?param1=1#anchor"), url);
    }

    public void testCreateURLOnSubWikiInVirtualModeInPathMode() throws MalformedURLException
    {
        this.config.setProperty("xwiki.virtual", "1");
        this.config.setProperty("xwiki.virtual.usepath", "1");

        URL url = this.urlFactory.createURL("Space", "Page", "view", "param1=1", "anchor", "wiki1", getContext());
        assertEquals(new URL("http://127.0.0.1/xwiki/wiki/wiki1server/view/Space/Page?param1=1#anchor"), url);
    }
}