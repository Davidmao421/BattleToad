package simpleplayer;

import battlecode.common.*;

public class GuardBrain implements Brain {

	private int archon;
	private MapLocation archonLoc;
	private MapLocation com;

	@Override
	public void run(RobotController rc) {
		intialize(rc);

		while (true) {
			Clock.yield();
			try {
				runTurn(rc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void intialize(RobotController rc) {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal s : signals) {
			if (SignalEncoder.getPacketType(s) == PacketType.NEW_ROBOT) {
				RobotIdTypePair p = SignalEncoder.decodeRobot(s);
				if (p.getId() == rc.getID()) {
					archon = s.getID();
					com = p.getLoc();
					break;
				}
			}
		}
	}

	public void runTurn(RobotController rc) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(archonLoc);
		if (dist > 4) {
			rc.move(rc.getLocation().directionTo(archonLoc));
		} else if (dist < 4) {
			rc.move(archonLoc.directionTo(rc.getLocation()));
		} else {
			for (Direction x : Statics.directions)
				if (rc.canMove(x)) {
					rc.move(x);
					break;
				}
		}

		// MapLocation toAttack;
	}

}