/*
 *  Copyright 2017, MEXSY
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mexsy.engine.enhancer;

import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_LABEL;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;

import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.InvalidContentException;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Enhancer
 * @author Anuj
 */
@Component(immediate = true, metatype = true)
@Service
@Properties(value={
    @Property(name=EnhancementEngine.PROPERTY_NAME,value="docstruct-enhancer")
})
public class DocstructEnhancer extends AbstractEnhancementEngine<IOException,RuntimeException>
    implements EnhancementEngine, ServiceProperties {
    
    private static Logger LOG = LoggerFactory.getLogger(DocstructEnhancer.class);
    
    @Property(value = "test")
    public static final String INPUT_PROPERTY = "com.mexsy.engine.enhancer.inputprop";
    
    /**
     * InputProp
     */
    private String inputProp;

    /**
     * The default value for the Execution of this Engine. Currently set to
     * {@link ServiceProperties#ORDERING_EXTRACTION_ENHANCEMENT} + 17. It should run after Metaxa and LangId.
     */
    public static final Integer defaultOrder = ServiceProperties.ORDERING_EXTRACTION_ENHANCEMENT + 17;
    
    public Map<String,Object> getServiceProperties() {
        return Collections.unmodifiableMap(Collections.singletonMap(
            ENHANCEMENT_ENGINE_ORDERING, (Object) defaultOrder));
    }

    public int canEnhance(ContentItem ci) throws EngineException {
        // check if content is present
        try {
            if((ContentItemHelper.getText(ci.getBlob()) == null) || 
                    (ContentItemHelper.getText(ci.getBlob()).trim().isEmpty())){
                return CANNOT_ENHANCE;
            }
        } catch (IOException e) {
            LOG.error("Failed to get the text for " +
            		"enhancement of content: " + ci.getUri(), e);
            throw new InvalidContentException(this, ci, e);
        }
        // default enhancement is synchronous enhancement
        return ENHANCE_SYNCHRONOUS;
    }

    public void computeEnhancements(ContentItem ci) throws EngineException {
        // write results (requires a write lock)
        // not required as we are enhancing synchronously
        //ci.getLock().writeLock().lock();
        try {
            // get the metadata graph
            Graph metadata = ci.getMetadata();
            // update some sample data
            //IRI textAnnotation = EnhancementEngineHelper.createTextEnhancement(ci, this);
            //metadata.add(new TripleImpl(textAnnotation, ENHANCER_ENTITY_LABEL, new PlainLiteralImpl("MEXSY-DOCSTRUCT")));
            LOG.info("MEXSY DOCSTRUCT: Enhancement Succeeded");
        } finally {
            //ci.getLock().writeLock().unlock();
        }
    }
    
    /**
     * Activate and read the properties
     * @param ce the {@link ComponentContext}
     */
    @Activate
    protected void activate(ComponentContext ce) throws ConfigurationException {
        try {
            super.activate(ce);
        } catch (IOException e) {
            // log
            LOG.error("Failed to update the configuration", e);
        }
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = ce.getProperties();
        // update the service URL if it is defined
        if(properties.get(INPUT_PROPERTY) != null){
            this.inputProp = (String) properties.get(INPUT_PROPERTY);
        }
    }
    
    /**
     * Deactivate
     * @param ce the {@link ComponentContext}
     */
    @Deactivate
    protected void deactivate(ComponentContext ce) {
        super.deactivate(ce);
    }

    /**
     * Gets the Service URL
     * @return
     */
    public String getServiceURL() {
        return inputProp;
    }
            
}
