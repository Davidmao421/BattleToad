package simpleplayer;

public enum PacketType {
	   ECHO(0),
	    OTHER(31);

	    public final int header;
	    private PacketType(int header) {
	        this.header = header;
	    }
}
