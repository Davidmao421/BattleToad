package team094old;

public enum PacketType {
	   ECHO(0),
	   NEW_ROBOT(1),
	   ATTACK_ENEMY(2),
	   PANIC(3),
	   PANIC_OVER(4),
	   DEAD(5),
	   CHANGE_SCHEME(6),
	   LOCAL_ATTACK(7),
	   PARTS_CACHE(8),
	   NEUTRAL_ROBOT(9),
	    OTHER(15);

	    public final int header;
	    private PacketType(int header) {
	        this.header = header;
	    }
}