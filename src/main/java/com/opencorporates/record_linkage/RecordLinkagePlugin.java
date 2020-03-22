package com.opencorporates.record_linkage;

import java.util.Collection;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;

import com.opencorporates.record_linkage.scoring.RecordLinkageScorer;

/**
 * A plugin which adds scoring metrics useful for record linkage
 * scenarios. These scoring metrics can either be used to refine 
 * the scores of search results or be included as an additional field.
 */
public class RecordLinkagePlugin extends Plugin implements ScriptPlugin {
	
    @Override
    public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
    	return new RecordLinkageScorer();
    }
}
