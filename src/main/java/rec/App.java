/**
 * 
 */
package rec;

import java.util.HashMap;

import rec.config.Mode;

/**
 * @author stefano
 * 
 */
public class App {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Builder builder = new Builder();
		builder.setMode(Mode.FULL);
		// This .drl is the file to be generated from the editor
		builder.addResource("specific.drl");

		Session session = builder.build();
		session.start();
		// This event is defined in the above .drl
		session.notify("MyEvent", new HashMap<String, Object>());
		session.dump(null);
		session.stop();

		System.out.println("Done.");
	}

}
