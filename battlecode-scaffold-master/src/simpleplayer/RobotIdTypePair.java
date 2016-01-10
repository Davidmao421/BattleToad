package simpleplayer;

import battlecode.common.RobotType;

public class RobotIdTypePair {
	public int id;
	public RobotType type;
	public RobotIdTypePair(int id, RobotType type) {
		super();
		this.id = id;
		this.type = type;
	}
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
}
