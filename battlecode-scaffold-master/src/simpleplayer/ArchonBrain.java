package simpleplayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.omg.CORBA.INITIALIZE;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class ArchonBrain implements Brain {
	// still doesn't account for own location
	private Map<Integer, MapLocation> archonStarts = new HashMap<>(6);
	private Map<Integer, RobotType> robots = new HashMap<>(100);
	private MapLocation start;

	private MapLocation com(Collection<MapLocation> locs) {
		int x, y = x = 0;
		for (MapLocation loc : locs) {
			x += loc.x;
			y += loc.y;
		}
		return new MapLocation(x / Math.max(1, (locs.size() + 1)), y / Math.max(1, (locs.size() + 1)));

	}

	private void intialize(RobotController rc) {
		try {

			rc.broadcastSignal(20000);

			Clock.yield();

			Signal[] signals = rc.emptySignalQueue();
			for (Signal s : signals)
				archonStarts.put(s.getID(), s.getLocation());

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void runTurn(RobotController rc) {
		try {
			int numGuards = 0;

			MapLocation com = com(archonStarts.values());
			if (com.distanceSquaredTo(rc.getLocation()) <= 4 || !rc.canMove(rc.getLocation().directionTo(com))) {
				for (Direction d : Direction.values()) {
					if (numGuards > 4)
						break;
					if (rc.canBuild(d, RobotType.GUARD)) {
						rc.build(d, RobotType.GUARD);
						numGuards++;
						rc.setIndicatorString(2, numGuards + " guards total");
					}
				}
				for (Direction d : Direction.values()) {
					if (rc.canBuild(d, RobotType.SOLDIER)) {
						rc.build(d, RobotType.SOLDIER);
					}
				}
//				rc.broadcastMessageSignal(message1, message2, radiusSquared);
			} else {
				rc.move(rc.getLocation().directionTo(com));

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run(RobotController rc) {
		intialize(rc);

		while (true) {
			Clock.yield();
			runTurn(rc);
		}
	}

}
