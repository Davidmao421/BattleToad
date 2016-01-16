package team094;


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
	public static Signal encodeParts(MapLocation mapLoc, double numParts){
		int part1, part2 = part1 = 0;
		int parts = (int) numParts;
		part1 = PacketType.PARTS_CACHE.header << 28;
		part1 |= mapLoc.x << 20;
		part1 |= mapLoc.y << 12;
		part2 |= parts << 12;
		return new Signal (new MapLocation(6,9),0,Team.ZOMBIE, part1, part2);
	}
	public static Signal decodeParts(Signal e){
		MapLocation l = new MapLocation(((0x0ff00000 & e.getMessage()[0])) >> 20, (0x000ff000 & e.getMessage()[0]) >> 12);
		int numParts = (0xfffff000 & e.getMessage()[1] >> 12);
		return new Signal(l,numParts, e.getTeam());
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
		default:
			return PacketType.OTHER;
		}
	}

	public static Signal encodeRobot(RobotIdTypePair pair){
		return encodeRobot(pair.type, pair.id, pair.loc);
	}

	public static Signal encodeRobot(RobotType type, int id, MapLocation loc) {
		int part1, part2 = part1 = 0;
		part1 = PacketType.NEW_ROBOT.header << 28;
		part1 |= robotTypeToInt(type) << 24;
		part1 |= id << 9;
		part1 |= loc.x << 2;
		part1 |= loc.y >> 5;
		part2 |= (loc.y & 0x00000003) << 27;
	
		return new Signal(new MapLocation(0, 0), 0, Team.NEUTRAL,part1, part2);
	}
	
	public static RobotIdTypePair decodeRobot(Signal e){
		int id = (e.getMessage()[0]&0x00ffffff)>>9;
		RobotType type = robotIntToType((0xf000000&e.getMessage()[0])>>24);
		int x = (e.getMessage()[0] >> 2) & 0x0000007f;
		int y = ((e.getMessage()[0] & 0x00000003) << 5) & (e.getMessage()[1] >> 27);
		
		return new RobotIdTypePair(id, type, new MapLocation(x, y));
	}
	
	public static Signal attackEnemy(RobotIdTypePair pair){
		return attackEnemy(pair.id,pair.loc);
	}
	
	public static Signal attackEnemy(int enemyID, MapLocation loc){
		int part1, part2 = part1 = 0;
		part1 = PacketType.ATTACK_ENEMY.header << 28;
		part1 |=  enemyID << 13;
		part1 |= loc.x << 6;
		part1 |= loc.y >> 1;
		part2 = loc.y << 31;
		return new Signal(loc,enemyID,Team.NEUTRAL,part1,part2);
	}
	
	public static RobotIdTypePair decodeEnemy(Signal s){
		int id = (s.getMessage()[0]&0x0fffffff) >> 13;
		int x = (s.getMessage()[0]&0x000000ff) >> 6;
		int y = (s.getMessage()[0]&0x0000003f) << 1;
		y+= s.getMessage()[1]>>31;
		return new RobotIdTypePair(id, null, new MapLocation(x, y));
	}
	
	public static Signal encodePanic(int id, MapLocation... locs){
		//TODO: 
		return null;
	}
	
	public static Panic decodePanic(Signal s){
		//TODO:
		return null;
	}

	public static Signal encodePanicOver(int id){
		//TODO: 
		return null;
	}
	
	public static int decodePanicOver(Signal s){
		//TODO:
		return -1;
	}
	
	public static Signal encodeDead(int id){
		//TODO: 
		return null;
	}
	
	public static int decodeDead(Signal s){
		//TODO:
		return -1;
	}
	
	public static Signal encodeChangeScheme(Scheme s){
		//TODO: 
		return null;
	}
	
	public static Scheme decodeChangeScheme(Signal s){
		//TODO:
		return null;
	}
}