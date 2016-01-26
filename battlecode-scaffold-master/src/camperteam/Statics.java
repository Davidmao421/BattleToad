package camperteam;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Statics {

	public static Direction[] directions = Direction.values();

	public static MapLocation referenceLocation;

	public static int maxCord = 80;

	public static MapLocation[] combineLocs(MapLocation[]... arrs) {
		int length = 0;
		for (MapLocation[] arr : arrs)
			length += arr.length;
		int i = 0;
		MapLocation[] all = new MapLocation[length];
		for (MapLocation[] arr : arrs)
			for (MapLocation loc : arr)
				all[i++] = loc;

		return all;
	}

	public static RobotInfo[] combineRobotInfo(RobotInfo[]... arrs) {
		int length = 0;
		for (RobotInfo[] arr : arrs)
			length += arr.length;
		int i = 0;
		RobotInfo[] all = new RobotInfo[length];
		for (RobotInfo[] arr : arrs)
			for (RobotInfo loc : arr)
				all[i++] = loc;

		return all;
	}

	@Deprecated
	public static Object[] combineArrays(Object[]... arrs) {
		int length = 0;
		for (Object[] arr : arrs) {
			length += arr.length;
		}
		System.out.println("length: = " + length);
		Object[] hi = new Object[length];
		int i = 0;
		for (Object[] arr : arrs) {
			for (Object o : arr) {
				hi[i++] = o;
			}
		}
		System.out.println("final i: =" + i);
		if (length == 0 && i == 0) {
			hi = new MapLocation[] { new MapLocation(0, 0) };
			System.out.println("test: " + hi);
		}
		return hi;
	}

	public static boolean contains(Object obj, Object[] arr) {
		for (Object i : arr)
			if (i.equals(obj))
				return true;
		return false;
	}

	public static boolean runAway(RobotController rc, RobotInfo[] friends, RobotInfo[] enemies)
			throws GameActionException {
		if (friends.length > enemies.length)
			return false;

		MapLocation enemyCom = com(enemies);
		Direction d = enemyCom.directionTo(rc.getLocation());
		if (rc.canMove(d) && rc.isCoreReady())
			moveTo(d, rc);
		else
			return false;
		return true;
	}

	public static RobotInfo closestRobot(MapLocation loc, RobotInfo[] info, int minDist) {
		if (info.length == 0)
			return null;
		RobotInfo closest = info[0];
		int closestDist = Integer.MAX_VALUE;
		for (int i = 1; i < info.length; i++) {
			int dist = sqrDist(loc, info[i].location);
			if (dist < closestDist && dist > minDist) {
				closestDist = dist;
				closest = info[i];
			}
		}
		return closest;
	}
	
	public static MapLocation closestRobot(MapLocation loc, MapLocation[] info, int minDist) {
		if (info.length == 0)
			return null;
		MapLocation closest = info[0];
		int closestDist = Integer.MAX_VALUE;
		for (int i = 1; i < info.length; i++) {
			int dist = sqrDist(loc, info[i]);
			if (dist < closestDist && dist > minDist) {
				closestDist = dist;
				closest = info[i];
			}
		}
		return closest;
	}

	public static RobotInfo closestRobot(MapLocation loc, RobotInfo[] info) {
		return closestRobot(loc, info, 0);
	}

	public static int sqrDist(MapLocation loc1, MapLocation loc2) {
		return (loc1.x - loc2.x) * (loc1.x - loc2.x) + (loc1.y - loc2.y) * (loc1.y - loc2.y);
	}

	public static MapLocation com(MapLocation[] locs) {
		int x, y = x = 0;
		for (MapLocation l : locs) {
			x += l.x;
			y += l.y;
		}
		y /= Math.max(1, locs.length);
		x /= Math.max(1, locs.length);
		return new MapLocation(x, y);
	}

	public static MapLocation com(RobotInfo[] locs) {
		int x, y = x = 0;
		for (RobotInfo i : locs) {
			x += i.location.x;
			y += i.location.y;
		}
		y /= Math.max(1, locs.length);
		x /= Math.max(1, locs.length);
		return new MapLocation(x, y);
	}

	public static MapLocation closestLoc(MapLocation loc, MapLocation[] list) {
		MapLocation closest = null;
		int dist = Integer.MAX_VALUE;
		for (MapLocation l : list) {
			int td = sqrDist(loc, l);
			if (td < dist) {
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
		if (!rc.isCoreReady()) {
			return false;
		}
		Direction toGoal;
		Direction testDir;
		toGoal = rc.getLocation().directionTo(goal);
		for (int i : new int[] { 0, 1, -1}) {
			testDir = directions[(toGoal.ordinal()+i)%8];
			if (rc.canMove(testDir)) {
				rc.move(testDir);
				return true;
			}
		}
		if (rc.getType().canClearRubble()) {
			for (int i : new int[] { 0, 1, -1}) {
				testDir = directions[(toGoal.ordinal()+i)%8];
				if (rc.canMove(testDir)) {
					rc.move(testDir);
					return true;
				}
			}
			MapLocation ahead = rc.getLocation().add(toGoal);
			if (rc.senseRubble(ahead) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				rc.clearRubble(rc.getLocation().directionTo(goal));
				return true;
			}
		}
		return false;
	}

	public static void moveTo(Direction direction, RobotController rc) throws GameActionException {
		try {
			if (rc.isCoreReady()) {
				for (int i : new int[] { 0, 1, -1, 2, -2 }) {
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
					if ((rc.senseRubble(ahead) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) && rc.isCoreReady()) {
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