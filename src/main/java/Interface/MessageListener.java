package Interface;
public interface MessageListener {
	final static String END_GAME = "END_GAME";

	public Object parseMessage(Object obj);
}
