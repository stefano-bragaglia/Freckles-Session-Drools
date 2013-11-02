/**
 * 
 */
package ec;

/**
 * @author stefano
 * 
 */
public interface Session {

	public void clear();

	public boolean isRunning();

	public void notify(String event, String[] params);

	public void start();

	public void stop();

}
