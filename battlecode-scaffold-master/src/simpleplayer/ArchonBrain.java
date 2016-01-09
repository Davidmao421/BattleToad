package simpleplayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class ArchonBrain implements Brain {
	// still doesn't account for own location
	private boolean firstRun = true;
	private Map<Integer, MapLocation> archonStarts = new HashMap<>(6);

	private MapLocation com(Collection<MapLocation> locs, MapLocation self) {
		int x = self.x;
		int y = self.y;
		for (MapLocation loc : locs) {
			x += loc.x;
			y += loc.y;
		}
		if (locs.size() != 0) {
			return new MapLocation(x / Math.max(1, (locs.size() + 1)), y / Math.max(1, (locs.size() + 1)));
		} else {
			return null;
		}
	}

	private MapLocation start;

	@Override
	public void run(RobotController rc) {
		if (start == null)
			start = rc.getLocation();

		if (firstRun) {
			try {
				firstRun = false;
				// rc.broadcastSignal(20000);//2*sightRange ); //maximize
				// broadcast
				// range without costing extra stuff
				rc.broadcastSignal(100 * 100);// May as well broadcast the
												// entire
												// map.
				Clock.yield();

				Signal[] signals = rc.emptySignalQueue();
				for (Signal s : signals)
					archonStarts.put(s.getID(), s.getLocation());

			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

 
		 while (true) {
		try {

			Signal[] signals = rc.emptySignalQueue();
			int numGuards = 0;

			MapLocation com = com(archonStarts.values(), start);
			// TODO: make building occur in all directions.
			if (com.distanceSquaredTo(rc.getLocation()) <= 4 || !rc.canMove(rc.getLocation().directionTo(com))) {
				for (Direction d : Direction.values()) {
					if (numGuards < 4 && rc.canBuild(d, RobotType.GUARD)) {
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
			} else {
				rc.move(rc.getLocation().directionTo(com));

			}
			Clock.yield();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		 }
	}

}
