package simpleplayer;

import java.awt.Robot;

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

	public static Signal encodeEcho(Signal s) {

		int part1, part2 = part1 = 0;
		part1 = PacketType.ECHO.header << 28;
		part1 |= s.getLocation().x << 21;
		part1 |= s.getLocation().y << 14;
		part1 |= s.getID() >> 1;
		part2 = (s.getID() & 1) << 31;

		return new Signal(s.getLocation(), s.getID(), s.getTeam(), part1, part2);

	}

	public static PacketType getPacketType(Signal s) {
		switch ((s.getMessage()[0] & 0xf0000000) >> 24) {
		case 0:
			return PacketType.ECHO;
		default:
			return PacketType.OTHER;
		}
	}

	public static Signal decodeEcho(Signal e) {
		MapLocation l = new MapLocation((0x0e000000 & e.getMessage()[0]) >> 21, (0x001fc000 & e.getMessage()[0]) >> 28);
		int i = ((e.getMessage()[0] & 0x00003fff) << 1) + ((e.getMessage()[1] & 0x80000000) >> 31);
		return new Signal(l, i, e.getTeam());
	}

	public static Signal encodeRobot(RobotType type, int id) {
		int part1, part2 = part1 = 0;
		part1 = PacketType.NEW_ROBOT.header << 28;
		part1 |= robotTypeToInt(type) << 24;
		part1 |= id << 9;
	
		return new Signal(new MapLocation(0, 0), 0, Team.NEUTRAL,part1, part2);
	}

}
