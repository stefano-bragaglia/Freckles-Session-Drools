/**
 * 
 */
package ec;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;

/**
 * @author stefano
 * 
 */
public class DroolsSession implements Session {

	private static final String DEFINITIONS = "Multi.drl";
	private static final String LITE_MODE = "Lite.drl";
	private static final String FULL_MODE = "Full.drl";

	public DroolsSession() {

		assert invariant() : "Illegal state in DroolsSession()";
	}

	/**
	 * @return
	 */
	private boolean invariant() {
		return (true);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void notify(String event, String[] params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

		KnowledgeBuilder builder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		builder.add(ResourceFactory.newClassPathResource(DEFINITIONS),
				ResourceType.DRL);
		builder.add(ResourceFactory.newClassPathResource(FULL_MODE),
				ResourceType.DRL);
		KnowledgeBuilderErrors errors = builder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors)
				System.err.println(error);
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBaseConfiguration baseCfg = KnowledgeBaseFactory
				.newKnowledgeBaseConfiguration();
		baseCfg.setOption(EventProcessingOption.STREAM);
		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase(baseCfg);
		base.addKnowledgePackages(builder.getKnowledgePackages());
		KnowledgeSessionConfiguration sessionCfg = KnowledgeBaseFactory
				.newKnowledgeSessionConfiguration();
		sessionCfg.setOption(ClockTypeOption.get("realtime"));
		StatefulKnowledgeSession session = base.newStatefulKnowledgeSession(
				sessionCfg, null);
		session.fireAllRules();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
