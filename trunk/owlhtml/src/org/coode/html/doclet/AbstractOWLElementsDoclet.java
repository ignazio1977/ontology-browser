/*
* Copyright (C) 2007, University of Manchester
*/
package org.coode.html.doclet;

import org.coode.html.OWLHTMLKit;
import org.coode.html.impl.OWLHTMLConstants;
import org.coode.html.impl.OWLHTMLProperty;
import org.coode.html.renderer.ElementRenderer;
import org.coode.html.renderer.OWLHTMLRenderer;
import org.coode.html.util.URLUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Jan 25, 2008<br><br>
 */
public abstract class AbstractOWLElementsDoclet<O extends OWLObject, E extends OWLObject> extends ElementsDoclet<O, E> {

    private OWLHTMLKit kit;

    private Set<OWLOntology> ontologies;

    private boolean inlineMedia = true;


    public AbstractOWLElementsDoclet(String name, Format format, OWLHTMLKit kit) {
        super(name, format);
        this.kit = kit;
        setComparator(kit.getOWLServer().getComparator());
    }

    protected final OWLHTMLKit getOWLHTMLKit(){
        return kit;
    }

    public void setOntologies(Set<OWLOntology> onts){
        this.ontologies = onts;
    }

    public void setInlineMedia(boolean inlineMedia){
        this.inlineMedia = inlineMedia;
    }

    protected final Collection<E> getElements(){
        Set<OWLOntology> onts = ontologies;
        if (onts == null){
            onts = getOWLHTMLKit().getVisibleOntologies();
        }
        Set<E> elements = new HashSet<E>();
        elements.addAll(getAssertedElements(onts));
        if (showInferences()){
            elements.addAll(getInferredElements(onts));
        }
        return elements;
    }

    private boolean showInferences() {
        return getOWLHTMLKit().getHTMLProperties().isSet(OWLHTMLProperty.optionShowInferences);
    }

    protected abstract Collection<E> getAssertedElements(Set<OWLOntology> ontologies);

    // TODO: make abstract?
    protected Collection<E> getInferredElements(Set<OWLOntology> ontologies){
        return Collections.emptySet();
    }

    @Override
    protected String getCSSClass(E object) { // TODO: should we cache to prevent multiple queries?
        if (showInferences() && !getAssertedElements(getOWLHTMLKit().getVisibleOntologies()).contains(object)){
            return OWLHTMLConstants.INFERRED_CSS_CLASS;
        }
        else{
            return OWLHTMLConstants.ASSERTED_CSS_CLASS;
        }
    }

    protected final ElementRenderer<? super E> getElementRenderer() {
        return new OWLHTMLRenderer(kit){
            @Override
            public void render(OWLObject obj, URL pageURL, PrintWriter out) {
                super.render(obj, pageURL, out);
                if (inlineMedia){
                    IRI iri = null;
                    if (obj instanceof OWLEntity){
                        iri = ((OWLEntity)obj).getIRI();
                    }
                    else if (obj instanceof IRI){
                        iri = (IRI)obj;
                    }
                    if (iri != null){
                        if (URLUtils.isImageURL(iri)){
                            out.print("<img class=\"thumb\" src=\"");
                            out.print(iri);
                            out.println("\" height=\"100\" />");
                        }
                        else{
                            // TODO: make a play button
//                if (URLUtils.isSoundURL(iri)){
//                    out.print("<EMBED src=\"");
//                    out.print(iri);
//                    out.println("\" autostart=\"true\" hidden=\"true\"/>");
//                }
                        }
                    }
                }
            }
        };
    }

    public String getID() {
        return super.getID() + " (" + getElements().size() + ")";
    }
}
