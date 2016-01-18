package team094;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import battlecode.common.*;

public class ScoutBrain implements Brain {
	MapLocation enemyCom, teamCom;

	// Queue<Signal> broadcastQueue;
	Queue<Signal> broadcastQueue;
	Set<Signal> sentSignals;
	RobotController rc;
	boolean radiate;

	boolean rotatesLeft, rotatesRight;

	public void initialize() {
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		broadcastQueue = new LinkedList<>();
		sentSignals = new HashSet<>();
		radiate = true;

		int dist = rc.getLocation().distanceSquaredTo(teamCom);
		for (MapLocation loc : rc.getInitialArchonLocations(rc.getTeam())) {
			if (loc.distanceSquaredTo(teamCom) - dist < 1) {
				if (rotatesLeft) {
					rotatesRight = rotatesLeft;
					continue;
				}
				rotatesLeft = !rotatesLeft;
			}
		}
	}

	public void addBroadcast(Signal s) {
		if (CompareStuff.containsSignal(s, broadcastQueue) || CompareStuff.containsSignal(s, sentSignals))
			return;
		broadcastQueue.add(s);
	}

	public void radiate() throws GameActionException {
		Direction d = teamCom.directionTo(enemyCom);
		if (rotatesLeft)
			d = d.rotateLeft();
		if (rotatesRight)
			d = d.rotateRight();

		MapLocation currentLoc = rc.getLocation();
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo r : robots) {
			if (currentLoc.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared
					|| currentLoc.add(d).distanceSquaredTo(r.location) <= r.type.attackRadiusSquared) {
				radiate = false;
				return;
			}
		}
		radiate = Statics.moveTo(d, rc);
	}

	public void senseBroadcast() {
		RobotInfo[] robots = rc.senseNearbyRobots();
		MapLocation[] parts = rc.sensePartLocations(-1);

		// rc.setIndicatorString(1, "Nearby Robots: " + robots.length);
		// rc.setIndicatorString(2, "Nearby parts: " + parts.length);

		for (MapLocation part : parts) {
			if (!broadcastQueue.contains(SignalEncoder.encodeParts(part, rc.senseParts(part)))) {
				addBroadcast(SignalEncoder.encodeParts(part, rc.senseParts(part)));
			}
		}

		for (RobotInfo info : robots) {
			if (info.team == rc.getTeam())
				continue;
			if (rc.getTeam().opponent() == info.team
					&& (info.type == RobotType.ZOMBIEDEN || info.type == RobotType.ARCHON)) {
				// addBroadcast(SignalEncoder.encodeRobot(info.type, info.ID,
				// info.location, rc.getLocation()));
			}

			if (info.team == Team.NEUTRAL) {
				addBroadcast(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location));
			}
		}

	}

	public void runTurn() throws GameActionException {
		senseBroadcast();
		if (broadcast())
			return;
		rc.setIndicatorString(0, "Radiate: " + radiate);
		if (radiate) {
			radiate(); // TODO: working movement
		} else if (radiate == false) {
			move();
		}

	}

	public void move() throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();
		Direction d = teamCom.directionTo(rc.getLocation());
		Direction[] directions = Statics.directions;
		MapLocation currentLoc = rc.getLocation();
		MapLocation bigThreat = null;
		double damage = 0;
		if (robots.length != 0) {
			for (RobotInfo r : robots) {
				if (currentLoc.distanceSquaredTo(r.location) <= Math.pow((Math.sqrt(r.type.attackRadiusSquared) + 1),
						2)) {
					if (bigThreat == null) {
						bigThreat = r.location;
						damage = r.type.attackPower;
					} else {
						if (damage < r.type.attackPower) {
							bigThreat = r.location;
							damage = r.type.attackPower;
						}
					}
				}
			}
		}
		if ((bigThreat != null)) {
			Statics.moveTo(bigThreat.directionTo(rc.getLocation()), rc);
		} else {
			int k = (int) (Math.random() * 8);
			for (int i = 0; i < 8; i++) {
				Direction dir = directions[(i + k) % 8];
				boolean shouldMove = true;
				for (RobotInfo r : robots) {
					if (currentLoc.add(dir).distanceSquaredTo(r.location) <= Math
							.pow((Math.sqrt(r.type.attackRadiusSquared) + 1), 2)) {
						shouldMove = false;
					}
					if (shouldMove == true && rc.canMove(dir) && rc.isCoreReady()) {
						rc.move(dir);
						return;
					}
				}
			}
		}

	}

	public boolean broadcast() throws GameActionException {
		if (rc.isCoreReady() && !broadcastQueue.isEmpty()) {
			Signal s = broadcastQueue.remove();
			rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], 1600); // TODO:
			// rc.setIndicatorString(0, "Broadcast queue: " +
			// broadcastQueue.size());
			rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], 1600);
			sentSignals.add(s);
			broadcast();
			return true;
		}
		return false;
	}

	public void run(RobotController rc1) {
		rc = rc1;
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