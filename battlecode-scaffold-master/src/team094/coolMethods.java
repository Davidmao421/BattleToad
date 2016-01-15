package team094;

import battlecode.common.*;

public class coolMethods {
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
		for (int i = 0; i < 2; i++) {
			if (rc.canMove(direction)) {
				rc.move(direction);
			}
			if (rc.getID() % 2 == 0) {
				direction = direction.rotateLeft();
			} else {
				direction = direction.rotateRight();
			}
		}
		if (rc.getType().canClearRubble()) {
			// failed to move, look to clear rubble
			MapLocation ahead = rc.getLocation().add(direction);
			if (rc.senseRubble(ahead) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				rc.clearRubble(direction);
			}
		}
		}catch(Exception e){
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
