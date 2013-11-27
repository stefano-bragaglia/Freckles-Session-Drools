/**
 * 
 */
package rec;

import java.util.HashSet;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.conf.ClockTypeOption;

import rec.config.Clock;
import rec.config.Mode;
import rec.config.Style;

/**
 * @author stefano
 * 
 */
public class Builder {

	public static Builder getTestBuilder() {
		return new Builder(Style.STREAM, Clock.PSEUDO);
	}

	private Clock clock;

	private Mode mode;

	private Set<String> resources;

	private Style style;

	public Builder() {
		this.clock = Clock.REALTIME;
		this.mode = Mode.FULL;
		this.resources = new HashSet<>();
		this.style = Style.STREAM;
		assert invariant() : "Illegal state in Builder()";
	}

	private Builder(Style style, Clock clock) {
		if (style == null)
			throw new IllegalArgumentException("Illegal 'style' argument in Builder(Style, Clock): " + style);
		if (clock == null)
			throw new IllegalArgumentException("Illegal 'clock' argument in Builder(Style, Clock): " + clock);
		this.clock = clock;
		this.mode = Mode.FULL;
		this.resources = new HashSet<>();
		this.style = style;
		assert invariant() : "Illegal state in Builder(Style, Clock)";
	}

	public Builder addResource(String resource) {
		if (resource == null || (resource = resource.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'resource' in Builder.addResource(String): " + resource);
		resources.add(resource);
		assert invariant() : "Illegal state in Builder.addResource(String)";
		return this;
	}

	public Session build() {
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		builder.add(ResourceFactory.newClassPathResource("multi.drl"), ResourceType.DRL);
		if (Mode.LITE == mode)
			builder.add(ResourceFactory.newClassPathResource("lite.drl"), ResourceType.DRL);
		else
			builder.add(ResourceFactory.newClassPathResource("full.drl"), ResourceType.DRL);
		for (String resource : resources)
			builder.add(ResourceFactory.newClassPathResource(resource), ResourceType.DRL);
		if (builder.hasErrors()) {
			for (KnowledgeBuilderError error : builder.getErrors())
				System.err.println(error);
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBaseConfiguration baseCfg = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		if (Style.STREAM == style)
			baseCfg.setOption(EventProcessingOption.STREAM);
		else
			baseCfg.setOption(EventProcessingOption.CLOUD);
		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase(baseCfg);
		base.addKnowledgePackages(builder.getKnowledgePackages());
		KnowledgeSessionConfiguration sessionCfg = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
		if (Clock.PSEUDO == clock)
			sessionCfg.setOption(ClockTypeOption.get("psuedo"));
		else
			sessionCfg.setOption(ClockTypeOption.get("realtime"));
		Session result = new Session(base, sessionCfg);
		assert invariant() : "Illegal state in Builder.build()";
		return result;
	}

	/**
	 * Invariant check against the internal state.
	 * 
	 * @return <code>true</code> if this instance's state is consistent,
	 *         <code>false</code> otherwise
	 */
	private boolean invariant() {
		return (clock != null && mode != null && resources != null && style != null);
	}

	public Builder removeResource(String resource) {
		if (resource == null || (resource = resource.trim()).isEmpty())
			throw new IllegalArgumentException("Illegal 'resource' in Builder.removeResource(String): " + resource);
		resources.remove(resource);
		assert invariant() : "Illegal state in Builder.removeResource(String)";
		return this;
	}

	protected Builder setClock(Clock clock) {
		if (clock == null)
			throw new IllegalArgumentException("Illegal 'clock' argument in Builder.setClock(Clock): " + clock);
		this.clock = clock;
		assert invariant() : "Illegal state in Builder.setClock(Clock)";
		return this;
	}

	public Builder setMode(Mode mode) {
		if (mode == null)
			throw new IllegalArgumentException("Illegal 'mode' argument in Builder.setMode(Mode): " + mode);
		this.mode = mode;
		assert invariant() : "Illegal state in Builder.setMode(Mode)";
		return this;
	}

	protected Builder setStyle(Style style) {
		if (style == null)
			throw new IllegalArgumentException("Illegal 'style' argument in Builder.setStyle(Style): " + style);
		this.style = style;
		assert invariant() : "Illegal state in Builder.setStyle(Style)";
		return this;
	}

}
