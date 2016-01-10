package team094;

import java.util.*;
import battlecode.common.*;

public class ArchonBrain implements Brain {
	// still doesn't account for own location
	private Map<Integer, MapLocation> archonStarts = new HashMap<>(6);
	private Map<Integer, RobotType> robots = new HashMap<>(100);

	private MapLocation com(Collection<MapLocation> locs) {
		int x, y = x = 0;
		for (MapLocation loc : locs) {
			x += loc.x;
			y += loc.y;
		}
		return new MapLocation(x / Math.max(locs.size(), 1), y / Math.max(1, locs.size()));

	}

	private void intialize(RobotController rc) {
		try {
			rc.broadcastSignal(500);

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

			Signal[] signals = rc.emptySignalQueue();
			for (Signal s : signals) {
				RobotIdTypePair pair = SignalEncoder.decodeRobot(s);
				if (robots.containsKey(pair.id))
					continue;
				robots.put(pair.id, pair.type);
			}
			RobotInfo[] nearbyGuards =  rc.senseNearbyRobots();
			for (RobotInfo nej: nearbyGuards){
				if (nej.type.equals(RobotType.GUARD)){
					numGuards +=1;
				}
			}

			MapLocation com = com(archonStarts.values());
			rc.setIndicatorString(1, "("+com.x+", "+com.y+")");
			if (!(com.distanceSquaredTo(rc.getLocation()) <= 4 || !rc.canMove(rc.getLocation().directionTo(com)))) {
				rc.move(rc.getLocation().directionTo(com));
			}
			else {
				ArrayList<Direction> dirs = new ArrayList<Direction>(Arrays.asList(Statics.directions));
				Direction[] dirs2 = new Direction[dirs.size()];
				int i=0;
				while(dirs.size()>0){
					Direction d=dirs.get((int)(8*Math.random()));
					if (numGuards > 8)
						break;
					if (rc.canBuild(d, RobotType.GUARD)) {
						rc.build(d, RobotType.GUARD);
//						numGuards++;
//						numGuards = 0;
						rc.setIndicatorString(2, numGuards + " guards total");
						break;
					}			
					dirs2[i++]=d;
				}
				for(; dirs.size()>0;i++)
					dirs2[i] = dirs.remove((int)(dirs.size()*Math.random()));
				
				for (Direction d : dirs2) {
					if (rc.canBuild(d, RobotType.SOLDIER)) {
						rc.build(d, RobotType.SOLDIER);
					}
				}

				RobotInfo[] nearby = rc.senseNearbyRobots();
				for (RobotInfo info : nearby) {
					if (robots.containsKey(info.ID) || info.team != rc.getTeam())
						continue;
					robots.put(info.ID, info.type);
					rc.broadcastMessageSignal(SignalEncoder.encodeRobot(info.type, info.ID, com).getMessage()[0], 0, 70);
				}
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
