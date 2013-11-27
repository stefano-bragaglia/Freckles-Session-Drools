/**
 * 
 */
package rec;

import java.io.PrintStream;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.definition.type.FactType;
import org.drools.runtime.ClassObjectFilter;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

/**
 * @author stefano
 * 
 */
public class Session {

	private static final String PACKAGE = "rec.dom";

	private static final String[] TYPES = { "MVI", "Sample", "Event", "Fluent" };

	private KnowledgeBase base;

	private KnowledgeSessionConfiguration config;

	private FactType event;

	private StatefulKnowledgeSession session;

	protected Session(KnowledgeBase base, KnowledgeSessionConfiguration config) {
		if (base == null)
			throw new IllegalArgumentException(
					"Illegal 'base' in Session(KnowledgeBase, KnowledgeSessionConfiguration): " + base);
		if (config == null)
			throw new IllegalArgumentException(
					"Illegal 'config' in Session(KnowledgeBase, KnowledgeSessionConfiguration): " + config);
		this.base = base;
		this.config = config;
		this.event = base.getFactType(PACKAGE, "Event");
		assert invariant() : "Illegal state in Session(KnowledgeBase, KnowledgeSessionConfiguration)";
	}

	public void clear() {
		if (null != session) {
			StatefulKnowledgeSession zombie = session;
			session = base.newStatefulKnowledgeSession(config, null);
			zombie.dispose();
		}
		assert invariant() : "Illegal state in Session.isRunning()";
	}

	protected int count(String type) {
		Class<?> c = session.getKnowledgeBase().getFactType(PACKAGE, type).getFactClass();
		int result = session.getObjects(new ClassObjectFilter(c)).size();
		assert invariant() : "Illegal state in DroolsTest.count(String)";
		return result;
	}

	protected void dump(PrintStream stream) {
		if (stream == null)
			stream = System.out;
		stream.println("--[ WM content ]--------------------------------------------------------------");
		for (String type : TYPES)
			stream.println(String.format("- %s(%d)", type, count(type)));
		stream.println("------------------------------------------------------------------------------");
		for (Object object : session.getObjects())
			System.out.println("> " + object);
		stream.println("==============================================================================");
		assert invariant() : "Illegal state in Session.dump()";
	}

	protected StatefulKnowledgeSession getMachinery() {
		assert invariant() : "Illegal state in Session.getMachinery()";
		return session;
	}

	/**
	 * @return
	 */
	private boolean invariant() {
		return (base != null && config != null && event != null);
	}

	public boolean isRunning() {
		boolean result = (null != session);
		assert invariant() : "Illegal state in Session.isRunning()";
		return result;
	}

	public FactHandle notify(String name, Map<String, Object> values) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException(
					"Illegal 'name' argument in Session.notify(String, Map<String, Object>): " + name);
		if (null == values)
			throw new IllegalArgumentException(
					"Illegal 'values' argument in Session.notify(String, Map<String, Object>): " + values);
		FactHandle handle = null;
		if (null != session)
			try {
				Object eventObj = event.newInstance();
				event.set(eventObj, "values", values);
				handle = session.insert(eventObj);
				session.fireAllRules();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		assert invariant() : "Illegal state in Session.notify(String, Map<String, Object>)";
		return handle;
	}

	public void start() {
		if (null == session) {
			session = base.newStatefulKnowledgeSession(config, null);
			session.fireAllRules();
		}
		assert invariant() : "Illegal state in Session.start()";
	}

	public void stop() {
		if (null != session) {
			StatefulKnowledgeSession zombie = session;
			session = null;
			zombie.dispose();
		}
		assert invariant() : "Illegal state in Session.stop()";
	}

}
