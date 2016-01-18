package team094;


import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class ViperBrain implements Brain {

	public static MapLocation enemyCom, teamCom;
	public static RobotController rc;
	
	public void initialize() {
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
	}

	public void attack(RobotInfo enemy) throws GameActionException {

		if (rc.canAttackLocation(enemy.location) && rc.isCoreReady() && rc.isWeaponReady()) {
			rc.attackLocation(enemy.location);
			return;
		}

		if (rc.canMove(rc.getLocation().directionTo(enemy.location)) && rc.isCoreReady()) {
			rc.move(rc.getLocation().directionTo(enemy.location));
			return;
		}
	}

	public static RobotInfo viperCompare(RobotInfo[] enemies) {
		boolean hasFound = false;
		RobotInfo start=null;
		int i = 0;
		while(hasFound==false&&i<enemies.length) {
			if(enemyCom.distanceSquaredTo(enemies[i].location) < enemies[i].location.distanceSquaredTo(teamCom)) {
				hasFound=true;
				start=enemies[i];
			}
			else
				i++;
		}
		if(start!=null) {
			for(RobotInfo r: enemies) {
				if(!CompareStuff.isInfected(r)&&r.location.distanceSquaredTo(rc.getLocation())<start.location.distanceSquaredTo(rc.getLocation())&&enemyCom.distanceSquaredTo(start.location) < start.location.distanceSquaredTo(teamCom)) {
					start=r;
				}
					
			}
			return start;
		}
		else
			return null;
	}
	
	/*
	 * public void pussy(RobotController rc, RobotInfo[] enemies){ RobotInfo
	 * away = CompareStuff.moveAwayFrom(enemies, rc.getLocation()); if (away !=
	 * null) { if (rc.isCoreReady() &&
	 * rc.canMove(away.location.directionTo(rc.getLocation())))
	 * Statics.moveTo(CompareStuff.moveAwayFrom(enemies,
	 * rc.getLocation()).location .directionTo(rc.getLocation()), rc); } }
	 */
	public void runTurn() throws GameActionException {
		RobotInfo[] zombies = rc.senseNearbyRobots(-1, Team.ZOMBIE);
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo away = CompareStuff.moveAwayFrom(zombies, rc.getLocation());
		RobotInfo away2 = CompareStuff.moveAwayFrom(robots, rc.getLocation());
		if (away != null) {
			if (rc.isCoreReady() && rc.canMove(away.location.directionTo(rc.getLocation())))
				Statics.moveTo(away.location.directionTo(rc.getLocation()), rc); // TODO:
		}
		else if (away2 != null) {
			if (rc.isCoreReady() && rc.canMove(away2.location.directionTo(rc.getLocation())))
				Statics.moveTo(away2.location.directionTo(rc.getLocation()), rc); // TODO:
		}
		else {
			RobotInfo bestEnemy = null;
			if(robots != null)
				bestEnemy = viperCompare(robots);
			RobotInfo bestZombie = null;
			if(zombies != null)
				bestZombie = viperCompare(zombies);
			if (bestEnemy != null) {
				attack(bestEnemy);
				return;
			}
			else if(bestZombie != null) {
				attack(bestZombie);
				return;
			} else {
				if (rc.canMove(rc.getLocation().directionTo(enemyCom)))
					rc.move(rc.getLocation().directionTo(enemyCom));
			}
		}
	}

	public void run(RobotController rc1) {
		rc=rc1;
		try {
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			Clock.yield();
			if (!rc.isCoreReady())
				continue;
			try {
				runTurn();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
