package team094;

import battlecode.common.MapLocation;

public class SimpleEncoder {
	public static enum MessageType {
		ENEMY, RADIUS, MOVETO, CENTERHERE, LEADERCHECK, ZOMBIEDEN, NEUTRALARCHON, TURRETQUORUM, ARCHONQUORUM;
		private static final MessageType[] values = values();
	};

	private static final int MASK = 0xFFFF;

	public static int encodeType(MessageType type) {
		return type.ordinal();
	}

	public static MessageType decodeType(int message) {
		return MessageType.values[message];
	}

	public static int encodeLocation(MapLocation loc) {
		int x = loc.x;
		int y = loc.y;
		int message = (x << 16 | y & MASK);
		return message;
	}

	public static MapLocation decodeLocation(int message) {
		return new MapLocation(getX(message), getY(message));
	}

	private static int getX(int in) {
		return in >> 16;
	}

	private static int getY(int in) {
		return (short) (in & MASK);
	}
}