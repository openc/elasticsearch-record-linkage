package com.opencorporates.record_linkage;

import java.io.IOException;
import java.util.Collection;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.script.ScriptContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.opencorporates.record_linkage.scoring.RecordLinkageScorer;

public class RecordLinkagePluginTest {
    
    RecordLinkagePlugin SUT;
    
    @Before
    public void setUp() {
        SUT = new RecordLinkagePlugin();
    }
    
    @Test
    public void testPlugin() {
        @SuppressWarnings("unchecked")
        Collection<ScriptContext<?>> context = (Collection<ScriptContext<?>>)Mockito.mock(Collection.class);
        Settings settings = Settings.EMPTY;
        
        Assert.assertTrue(SUT.getScriptEngine(settings, context) instanceof RecordLinkageScorer);
    }
    
    @After
    public void tearDown() throws IOException {
        SUT.close();
    }
}
