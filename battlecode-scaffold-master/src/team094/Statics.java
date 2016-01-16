package team094;

import java.awt.Robot;
import java.util.List;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Statics {

	public static Direction[] directions = Direction.values();
	
	public static int maxCord = 80;

	public static RobotInfo closestRobot(MapLocation loc, RobotInfo[] info){
		if (info.length == 0) return null;
		RobotInfo closest = info[0];
		int closestDist = Integer.MAX_VALUE;
		for (int i = 1; i < info.length; i++){
			int dist = sqrDist(loc, info[i].location);
			if (dist < closestDist){
				closestDist = dist;
				closest = info[i];
			}
		}
		return closest;
	}
	
	public static int sqrDist(MapLocation loc1 , MapLocation loc2){
		return (loc1.x-loc2.x)*(loc1.x-loc2.x) + (loc1.y-loc2.y)*(loc1.y-loc2.y);
	}
	
	public static MapLocation com(MapLocation[] locs){
		int x,y=x=0; 
		for (MapLocation l : locs){
			x += l.x;
			y += l.y;
		}
		y/=Math.max(1, locs.length);
		x/=Math.max(1, locs.length);
		return new MapLocation(x,y);
	}
	
	public static MapLocation closestLoc(MapLocation loc, MapLocation[] list){
		MapLocation closest = null; 
		int dist = Integer.MAX_VALUE;
		for (MapLocation l : list){
			int td = sqrDist(loc,l);
			if (td < dist){
				dist = td;
				closest = l;
			}
		}
		return closest;
	}

	protected static boolean navigateTo(MapLocation goal, RobotController rc) throws GameActionException {
		if (goal == rc.getLocation()) {
			return false;
		}
		Direction toEnemy;
		toEnemy = rc.getLocation().directionTo(goal);
		for (int i = 0; i < 2; i++) {
			if (rc.canMove(toEnemy)) {
				rc.move(toEnemy);
				return true;
			}
			if (rc.getID() % 2 == 0) {
				toEnemy = toEnemy.rotateLeft();
			} else {
				toEnemy = toEnemy.rotateRight();
			}
		}
		if (rc.getType().canClearRubble()) {
			// failed to move, look to clear rubble
			MapLocation ahead = rc.getLocation().add(rc.getLocation().directionTo(goal));
			if (rc.senseRubble(ahead) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				rc.clearRubble(rc.getLocation().directionTo(goal));
				return true;
			}
		}
		return false;
	}

	protected static void moveTo(Direction direction, RobotController rc) throws GameActionException {
		try {
			if (rc.isCoreReady()) {
				for (int i = 0; i < 2; i++) {
					if (rc.canMove(direction) && rc.isCoreReady()) {
						rc.move(direction);
					}
					if (Math.random() < 0.5) {
						direction = direction.rotateLeft();
					} else {
						direction = direction.rotateRight();
					}
				}
				if (rc.getType().canClearRubble()) {
					// failed to move, look to clear rubble
					MapLocation ahead = rc.getLocation().add(direction);
					if ((rc.senseRubble(ahead) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH)&& rc.isCoreReady()) {
						rc.clearRubble(direction);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected static MapLocation averageLocation(RobotInfo[] robotArray) {
		int xpos = 0, ypos = 0;
		for (RobotInfo friend : robotArray) {
			xpos += friend.location.x;
			ypos += friend.location.y;
		}
		xpos = xpos / robotArray.length;
		ypos = ypos / robotArray.length;
		MapLocation goal = new MapLocation(xpos, ypos);
		return goal;
	}

	protected static MapLocation reflect(MapLocation averageLocation, RobotController rc) {
		int xpos = rc.getLocation().x * 2 - averageLocation.x;
		int ypos = rc.getLocation().y * 2 - averageLocation.y;
		return new MapLocation(xpos, ypos);
	}
}