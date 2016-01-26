package team094old;

import battlecode.common.*;

public class LocationInfo {

		public MapLocation location;
		public RobotInfo robotinfo;
		public double parts, rubble;
		public int roundfound;
		public LocationInfo(MapLocation loc, RobotInfo info, double partsthere, double rubblethere, int round ){
			location = loc;
			robotinfo = info;
			parts = partsthere;
			rubble = rubblethere;
			roundfound = round;
		}

}
