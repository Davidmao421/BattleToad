package simpleplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class RobotIdTypePair {
	public int id;
	public RobotType type;
	public MapLocation loc;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public RobotType getType() {
		return type;
	}
	public void setType(RobotType type) {
		this.type = type;
	}
	public MapLocation getLoc() {
		return loc;
	}
	public void setLoc(MapLocation loc) {
		this.loc = loc;
	}
	public RobotIdTypePair(int id, RobotType type, MapLocation loc) {
		super();
		this.id = id;
		this.type = type;
		this.loc = loc;
	}

}
