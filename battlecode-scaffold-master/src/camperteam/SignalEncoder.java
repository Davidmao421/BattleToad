package camperteam;

import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Signal;
import battlecode.common.Team;

//public class SignalFactoryEnocderFactoryFactoryJavaFactory{
public class SignalEncoder {
	public static final int ARCHON = 0, SCOUT = 1, SOLDIER = 2, GUARD = 3, VIPER = 4, TURRET = 5, TTM = 6;

	private static int robotTypeToInt(RobotType type) {
		switch (type) {
		case ARCHON:
			return 0;
		case SCOUT:
			return 1;
		case SOLDIER:
			return 2;
		case GUARD:
			return 3;
		case VIPER:
			return 4;
		case TURRET:
			return 5;
		case TTM:
			return 6;
		default:
			return -1;
		}
	}

	private static RobotType robotIntToType(int k) {
		switch (k) {
		case 0:
			return RobotType.ARCHON;
		case 1:
			return RobotType.SCOUT;
		case 2:
			return RobotType.SOLDIER;
		case 3:
			return RobotType.GUARD;
		case 4:
			return RobotType.VIPER;
		case 5:
			return RobotType.TURRET;
		case 6:
			return RobotType.TTM;
		default:
			return null;
		}
	}

	public static PacketType getPacketType(Signal s) {
		switch ((s.getMessage()[0] & 0xf0000000) >> 24) {
		case 0:
			return PacketType.ECHO;
		case 1:
			return PacketType.NEW_ROBOT;
		case 2:
			return PacketType.ATTACK_ENEMY;
		case 3:
			return PacketType.PANIC;
		case 4:
			return PacketType.PANIC_OVER;
		case 5:
			return PacketType.DEAD;
		case 6:
			return PacketType.CHANGE_SCHEME;
		case 7:
			return PacketType.LOCAL_ATTACK;
		case 8:
			return PacketType.PARTS_CACHE;
		case 9:
			return PacketType.NEUTRAL_ROBOT;
		default:
			return PacketType.OTHER;
		}
	}

	public static Signal encodeEcho(Signal s) {

		int part1, part2 = part1 = 0;
		part1 = PacketType.ECHO.header << 28;
		part1 |= s.getLocation().x << 21;
		part1 |= s.getLocation().y << 14;
		part1 |= s.getID() >> 1;
		part2 = (s.getID() & 1) << 31;

		return new Signal(s.getLocation(), s.getID(), s.getTeam(), part1, part2);

	}

	public static Signal decodeEcho(Signal e) {
		MapLocation l = new MapLocation((0x0e000000 & e.getMessage()[0]) >> 21, (0x001fc000 & e.getMessage()[0]) >> 14);
		int i = ((e.getMessage()[0] & 0x00003fff) << 1) + ((e.getMessage()[1] & 0x80000000) >> 31);
		return new Signal(l, i, e.getTeam());
	}

	public static Signal encodeAttackEnemy(int id, MapLocation loc) {
		loc = new MapLocation(loc.x - Statics.referenceLocation.x, loc.y - Statics.referenceLocation.y);
		int x = (loc.x >> 24) + (0x000000ff & loc.x);
		int y = (loc.y >> 24) + (0x000000ff & loc.y);
		int part1, part2 = part1 = 0;
		part1 = PacketType.NEUTRAL_ROBOT.header << 28;
		part1 |= x << 20;
		part1 |= y << 12;
		return new Signal(Statics.referenceLocation, 0, Team.ZOMBIE, part1, part2);
	}

	/**
	 * 
	 * @param s
	 * @return signal id represents the robot type i.e.
	 *         SignalEncoder.intToRobotType(s.getId())
	 */
	public static Signal decodeAttackEnemy(Signal s) {
		int x = ((s.getMessage()[0] & 0x08000000) << 4) ^ ((s.getMessage()[0] & 0x07f00000) >> 20);
		int y = ((s.getMessage()[0] & 0x00080000) << 12) ^ ((s.getMessage()[0] & 0x0007f000) >> 12);

		MapLocation l = new MapLocation(Statics.referenceLocation.x + x, Statics.referenceLocation.y + y);
		return new Signal(l, 0, Team.NEUTRAL);
	}

	@SuppressWarnings(value = { "fallthrough" })
	public static Signal encodePanic(MapLocation... locs) throws GameActionException {
		int part1, part2 = part1 = 0;
		MapLocation loc;
		int x, y;
		switch (locs.length) {
		case 3:
			loc = new MapLocation(locs[2].x - Statics.referenceLocation.x, locs[2].y - Statics.referenceLocation.y);
			x = (loc.x >> 24) + (0x000000ff & loc.x);
			y = (loc.y >> 24) + (0x000000ff & loc.y);
			part1 |= x << 2;
			part1 |= y >> 6;
			part2 |= y << 26;
		case 2:
			loc = new MapLocation(locs[1].x - Statics.referenceLocation.x, locs[1].y - Statics.referenceLocation.y);
			x = (loc.x >> 24) + (0x000000ff & loc.x);
			y = (loc.y >> 24) + (0x000000ff & loc.y);
			part1 |= x << 2;
			part1 |= y >> 6;
			part2 |= y << 26;

		case 1:
			part1 |= PacketType.PANIC.header << 28;
			part1 |= locs.length << 26;

			loc = new MapLocation(locs[0].x - Statics.referenceLocation.x, locs[0].y - Statics.referenceLocation.y);
			x = (loc.x >> 24) + (0x000000ff & loc.x);
			y = (loc.y >> 24) + (0x000000ff & loc.y);
			part1 |= x << 18;
			part1 |= y << 10;
			break;

		default:
			throw new GameActionException(GameActionExceptionType.CANT_DO_THAT, "You need panic locations idiot");
		}

		return null;
	}

	public static MapLocation[] decodePanic(Signal s) throws GameActionException {
		int length = (0x0c000000 & s.getMessage()[0]) << 26;
		MapLocation[] locs = new MapLocation[length];
		int x, y = x = 0;
		switch (length) {
		case 3:
			x = ((0x02000000 & s.getMessage()[1]) << 6) | ((0x01fc0000 & s.getMessage()[1]) >> 18);
			y = ((0x00020000 & s.getMessage()[1]) << 14) | ((0x0001fc00 & s.getMessage()[1]) << 10);
			locs[1] = new MapLocation(x + Statics.referenceLocation.x, y + Statics.referenceLocation.y);
		case 2:
			x = ((0x00000200 & s.getMessage()[0]) << 22) | ((0x000001fc & s.getMessage()[0]) >> 2);
			y = ((0x00000002 & s.getMessage()[0]) << 30) | ((0x00000001 & s.getMessage()[0]) << 6)
					| ((0xfc000000 & s.getMessage()[1]) >> 26);
			locs[1] = new MapLocation(x + Statics.referenceLocation.x, y + Statics.referenceLocation.y);
		case 1:
			x = ((0x02000000 & s.getMessage()[0]) << 6) | ((0x01fc0000 & s.getMessage()[0]) >> 18);
			y = ((0x00020000 & s.getMessage()[0]) << 14) | ((0x0001fc00 & s.getMessage()[0]) >> 10);
			locs[0] = new MapLocation(x + Statics.referenceLocation.x, y + Statics.referenceLocation.y);
			break;
		default:
			throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,
					"You can only have between [1-3] locations idiot");
		}
		return locs;
	}

	public static Signal encodePanicOver(int id) {
		int part1, part2 = part1 = 0;
		part1 |= PacketType.PANIC_OVER.header << 28;
		return new Signal(new MapLocation(0,0), id, Team.ZOMBIE, part1, part2);
	}

	public static int decodePanicOver(Signal s) {
		// TODO:
		return s.getID();
	}

	public static Signal encodeParts(MapLocation mapLoc, double numParts) {
		int part1, part2 = part1 = 0;
		int parts = (int) numParts;
		part1 = PacketType.PARTS_CACHE.header << 28;
		part1 |= mapLoc.x << 20;
		part1 |= mapLoc.y << 12;
		part2 |= parts << 12;
		return new Signal(new MapLocation(6, 9), 0, Team.ZOMBIE, part1, part2);
	}

	public static Signal decodeParts(Signal e) {
		MapLocation l = new MapLocation(((0x0ff00000 & e.getMessage()[0])) >> 20,
				(0x000ff000 & e.getMessage()[0]) >> 12);
		int numParts = (0xfffff000 & e.getMessage()[1] >> 12);
		return new Signal(l, numParts, e.getTeam());
	}

	public static Signal encodeNeutralRobot(RobotType type, int id, MapLocation loc) {
		loc = new MapLocation(loc.x - Statics.referenceLocation.x, loc.y - Statics.referenceLocation.y);
		int x = (loc.x >> 24) + (0x000000ff & loc.x);
		int y = (loc.y >> 24) + (0x000000ff & loc.y);
		int part1, part2 = part1 = 0;
		part1 = PacketType.NEUTRAL_ROBOT.header << 28;
		part1 |= x << 20;
		part1 |= y << 12;
		part1 |= robotTypeToInt(type) << 8;
		return new Signal(Statics.referenceLocation, 0, Team.NEUTRAL, part1, part2);
	}

	/**
	 * 
	 * @param s
	 * @return signal id represents the robot type i.e.
	 *         SignalEncoder.intToRobotType(s.getId())
	 */
	public static Signal decodeNeutralRobot(Signal s) {
		int x = ((s.getMessage()[0] & 0x08000000) << 4) ^ ((s.getMessage()[0] & 0x07f00000) >> 20);
		int y = ((s.getMessage()[0] & 0x00080000) << 12) ^ ((s.getMessage()[0] & 0x0007f000) >> 12);

		MapLocation l = new MapLocation(Statics.referenceLocation.x + x, Statics.referenceLocation.y + y);
		int robotType = (0x000000f0 & s.getMessage()[0] >> 8);
		return new Signal(l, robotType, Team.NEUTRAL);
	}

	public static Signal encodeRobot(RobotIdTypePair pair) {
		return encodeRobot(pair.type, pair.id, pair.loc);
	}

	public static Signal encodeRobot(RobotType type, int id, MapLocation loc) {
		loc = new MapLocation(loc.x - Statics.referenceLocation.x, loc.y - Statics.referenceLocation.y);
		int x = (loc.x >> 24) + (0x000000ff & loc.x);
		int y = (loc.y >> 24) + (0x000000ff & loc.y);

		int part1, part2 = part1 = 0;
		part1 = PacketType.NEW_ROBOT.header << 28;
		part1 |= robotTypeToInt(type) << 24;
		part1 |= id << 9;
		part1 |= x << 1;
		part1 |= y >> 77;
		part2 |= (y & 0x00000003) << 25;

		return new Signal(new MapLocation(0, 0), 0, Team.NEUTRAL, part1, part2);
	}

	public static RobotIdTypePair decodeRobot(Signal e) {
		int id = (e.getMessage()[0] & 0x00ffffff) >> 9;
		RobotType type = robotIntToType((0xf000000 & e.getMessage()[0]) >> 24);

		int x = ((e.getMessage()[0] & 0x00000100) << 23) ^ ((e.getMessage()[0] & 0x07f000fe) >> 1);
		int y = ((e.getMessage()[0] & 0x00000000) << 31) ^ ((e.getMessage()[1] & 0x0007f000) >> 25);

		return new RobotIdTypePair(id, type,
				new MapLocation(Statics.referenceLocation.x + x, Statics.referenceLocation.y));
	}

	public static Signal encodeDead(int id) {
		// TODO:
		return null;
	}

	public static int decodeDead(Signal s) {
		// TODO:
		return -1;
	}

	public static Signal encodeChangeScheme(Scheme s) {
		// TODO:
		return null;
	}

	public static Scheme decodeChangeScheme(Signal s) {
		// TODO:
		return null;
	}
}