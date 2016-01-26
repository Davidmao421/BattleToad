package camperteam;


import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class ViperBrain implements Brain {

	public MapLocation enemyCom, teamCom;

	public void initialize(RobotController rc) {
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
	}

	public void attack(RobotController rc, RobotInfo enemy) throws GameActionException {

		if (rc.canAttackLocation(enemy.location) && rc.isCoreReady() && rc.isWeaponReady()) {
			rc.attackLocation(enemy.location);
			return;
		}

		if (rc.canMove(rc.getLocation().directionTo(enemy.location)) && rc.isCoreReady()) {
			rc.move(rc.getLocation().directionTo(enemy.location));
			return;
		}
	}

	/*
	 * public void pussy(RobotController rc, RobotInfo[] enemies){ RobotInfo
	 * away = CompareStuff.moveAwayFrom(enemies, rc.getLocation()); if (away !=
	 * null) { if (rc.isCoreReady() &&
	 * rc.canMove(away.location.directionTo(rc.getLocation())))
	 * Statics.moveTo(CompareStuff.moveAwayFrom(enemies,
	 * rc.getLocation()).location .directionTo(rc.getLocation()), rc); } }
	 */
	public void runTurn(RobotController rc) throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo closestEnemy = Statics.closestRobot(enemyCom, robots);

		if (closestEnemy != null) {
			if (enemyCom.distanceSquaredTo(closestEnemy.location) < closestEnemy.location.distanceSquaredTo(teamCom)) {
				attack(rc, closestEnemy);
				return;
			}

			robots = rc.senseNearbyRobots(-1, Team.ZOMBIE);
			closestEnemy = Statics.closestRobot(enemyCom, robots);

			if (Statics.sqrDist(enemyCom, closestEnemy.location) > Statics.sqrDist(closestEnemy.location, teamCom)) {
				attack(rc, closestEnemy);
				return;
			}
		} else {
			if (rc.canMove(rc.getLocation().directionTo(enemyCom)))
				rc.move(rc.getLocation().directionTo(enemyCom));
		}
	}

	public void run(RobotController rc) {
		try {
			initialize(rc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			Clock.yield();
			if (!rc.isCoreReady())
				continue;
			try {
				runTurn(rc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
