package team094;

public enum PacketType {
	   ECHO(0),
	   NEW_ROBOT(1),
	    OTHER(31);

	    public final int header;
	    private PacketType(int header) {
	        this.header = header;
	    }
}
