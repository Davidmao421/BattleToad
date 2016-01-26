package team094;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import battlecode.common.*;
import team094.SimpleEncoder.MessageType;

public class ScoutBrain implements Brain {
	MapLocation enemyCom, teamCom;

	// Queue<Signal> broadcastQueue;
	Queue<Signal> broadcastQueue;
	Set<Signal> sentSignals;
	RobotController rc;
	boolean radiate;

	private MapLocation center;

	private int radius;
	int messagesSent;

	public void initialize() {
		messagesSent = 0;
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		broadcastQueue = new LinkedList<>();
		sentSignals = new HashSet<>();
		radiate = true;
	}

	public void addBroadcast(Signal s) {
		if (CompareStuff.containsSignal(s, broadcastQueue) || CompareStuff.containsSignal(s, sentSignals))
			return;
		broadcastQueue.add(s);
	}

	public void radiate() throws GameActionException {
		Direction d = teamCom.directionTo(rc.getLocation());
		MapLocation currentLoc = rc.getLocation();
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo r : robots) {
			if (currentLoc.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared
					|| currentLoc.add(d).distanceSquaredTo(r.location) <= r.type.attackRadiusSquared) {
				radiate = false;
				return;
			}
		}
		if (radiate == true) {
			if (rc.canMove(d))
				Statics.moveTo(d, rc);
			else {
				radiate = false;
			}
		}
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

	public void senseEnemies() throws GameActionException {
		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), -1);
		for (RobotInfo r : enemies) {
			messagesSent++; if (messagesSent < 20) rc.broadcastMessageSignal(SimpleEncoder.encodeType(MessageType.ENEMY),
					SimpleEncoder.encodeLocation(r.location), rc.getType().sensorRadiusSquared);
		}
	}

	public void runTurn() throws GameActionException {
		/*
		 * senseBroadcast(); if (broadcast()) return; rc.setIndicatorString(0,
		 * "Radiate: " + radiate); if (radiate == true) { radiate(); // TODO:
		 * working movement } else if (radiate == false) { move(); }
		 */
		move();
		senseEnemies();
		messagesSent = 0;
	}

	private void processSignals(RobotController rc) throws GameActionException {
		Signal[] signals = rc.emptySignalQueue();
		for (Signal s : signals) {
			if (s.getTeam() == rc.getTeam() && s.getMessage() != null) {
				MessageType type = SimpleEncoder.decodeType(s.getMessage()[0]);
				switch (type) {
				case CENTERHERE:
					center = SimpleEncoder.decodeLocation(s.getMessage()[1]);
					break;
				case LEADERCHECK:
					break;
				case MOVETO:
					break;
				case NEUTRALARCHON:
					break;
				case RADIUS:
					center = s.getLocation();
					radius = s.getMessage()[1];
					break;
				case ZOMBIEDEN:
					break;
				case TURRETQUORUM:
					messagesSent++; if (messagesSent < 20) rc.broadcastSignal(rc.getLocation().distanceSquaredTo(s.getLocation()));
					break;
				default:
					break;
				}
			}
		}
	}

	public void move() throws GameActionException {
		if (!rc.isCoreReady())
			return;

		MapLocation startingArchon = rc.getInitialArchonLocations(rc.getTeam())[0];
		int dist = rc.getLocation().distanceSquaredTo(startingArchon);
		if (dist > radius + 40) {
			Statics.moveTo(rc.getLocation().directionTo(startingArchon), rc);
			return;
		}
		if (dist < radius) {
			Statics.moveTo(rc.getLocation().directionTo(startingArchon).opposite(), rc);
			return;
		}

		Statics.moveTo(rc.getLocation().directionTo(startingArchon).rotateLeft().rotateLeft(), rc);

	}

	public boolean broadcast() throws GameActionException {
		if (rc.isCoreReady() && !broadcastQueue.isEmpty()) {
			Signal s = broadcastQueue.remove();
			messagesSent++; if (messagesSent < 20) rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], 1600); // TODO:
			// rc.setIndicatorString(0, "Broadcast queue: " +
			// broadcastQueue.size());
			messagesSent++;
			if (messagesSent < 20)
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