/**
 * 
 */
package rec;

import java.io.PrintStream;
import java.util.Collection;
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

	// private FactType event;

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
		// this.event = base.getFactType(PACKAGE, "Event");
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
		Collection<Object> objects = session.getObjects();
		if (objects.size() > 0) {
			stream.println("------------------------------------------------------------------------------");
			for (Object object : session.getObjects())
				System.out.println("> " + object);
		}
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
		return (base != null && config != null);
	}

	public boolean isRunning() {
		boolean result = (null != session);
		assert invariant() : "Illegal state in Session.isRunning()";
		return result;
	}

	public FactHandle notify(String name, Object value, Map<String, Object> params) {
		if (null == name || (name = name.trim()).isEmpty())
			throw new IllegalArgumentException(
					"Illegal 'name' argument in Session.notify(String, Object, Map<String, Object>): " + name);
		if (null == value)
			throw new IllegalArgumentException(
					"Illegal 'value' argument in Session.notify(String, Object, Map<String, Object>): " + value);
		if (null == params)
			throw new IllegalArgumentException(
					"Illegal 'values' argument in Session.notify(String, Object, Map<String, Object>): " + params);
		FactHandle handle = null;
		if (null != session)
			try {
				FactType type = base.getFactType(PACKAGE, name);
				if (null != type) {
					Object eventObj = type.newInstance();
					type.set(eventObj, "value", value);
					type.set(eventObj, "params", params);
					handle = session.insert(eventObj);
					session.fireAllRules();
				} else
					System.err.println("* Unexpected event '" + name + "' (ignored)");
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		assert invariant() : "Illegal state in Session.notify(String, Object, Map<String, Object>)";
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
