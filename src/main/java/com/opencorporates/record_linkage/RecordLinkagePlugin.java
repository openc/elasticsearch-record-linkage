package com.opencorporates.record_linkage;

import java.util.Collection;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

import com.opencorporates.record_linkage.scoring.RecordLinkageScorer;

/**
 * Registers record-linkage related scoring methods.
 */
public class RecordLinkagePlugin extends Plugin implements ScriptPlugin {
	
    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
    	return new RecordLinkageScorer();
    }
}
