package simpleplayer;

import battlecode.common.MapLocation;
import battlecode.common.Signal;

//public class SignalFactoryEnocderFactoryFactoryJavaFactory{
public class SignalEncoder {

	public static Signal encodeEcho(Signal s) {

		int part1, part2 = part1 = 0;
		part1 = PacketType.ECHO.header << 28;
		part1 |= s.getLocation().x << 21;
		part1 |= s.getLocation().y << 14;
		part1 |= s.getID() >> 1;
		part2 = (s.getID()&1) << 31;

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
	
	public static Signal decodeEcho(Signal e){
		MapLocation l = new MapLocation((0x0e000000 & e.getMessage()[0])>>21, (0x001fc000 & e.getMessage()[0]) >> 28);
		int i = ((e.getMessage()[0]&0x00003fff) << 1)+((e.getMessage()[1]&0x80000000) >> 31);
		return new Signal(l, i, e.getTeam());
	}

}
