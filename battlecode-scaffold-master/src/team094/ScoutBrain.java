package team094;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.BREAKPOINT;

import battlecode.common.*;

public class ScoutBrain implements Brain {
	MapLocation enemyCom, teamCom;

	// Queue<Signal> broadcastQueue;
	LinkedList<Signal> broadcastQueue;
	int index;
	RobotController rc;
	public void initialize() {
		enemyCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam().opponent()));
		teamCom = Statics.com(rc.getInitialArchonLocations(rc.getTeam()));
		broadcastQueue = new LinkedList<>();
		index = 0;
	}

	public void radiate() throws GameActionException {
		Direction d = teamCom.directionTo(rc.getLocation());
		if (rc.canMove(d))
			rc.move(d);
	}

	public void senseBroadcast() {
		RobotInfo[] robots = rc.senseNearbyRobots();
		MapLocation[] parts = rc.sensePartLocations(-1);

		for (MapLocation part : parts) {
			if (!broadcastQueue.contains(SignalEncoder.encodeParts(part, rc.senseParts(part)))) {
				broadcastQueue.add(SignalEncoder.encodeParts(part, rc.senseParts(part)));
			}
		}

		for (RobotInfo info : robots) {
			if (info.team == rc.getTeam())
				continue;
			if (rc.getTeam().opponent() == info.team
					&& (info.type == RobotType.ZOMBIEDEN || info.type == RobotType.ARCHON)) {
				if (!broadcastQueue.contains(SignalEncoder.encodeRobot(info.type, info.ID, info.location)))
				broadcastQueue.add(SignalEncoder.encodeRobot(info.type, info.ID, info.location));
			}
			if (info.team.equals(Team.NEUTRAL)) {
				if (!broadcastQueue.contains(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location)))
				broadcastQueue.add(SignalEncoder.encodeNeutralRobot(info.type, info.ID, info.location));
			}
		}

	}

	public void runTurn() throws GameActionException {
		senseBroadcast();
		// radiate(); // TODO: working movement
		broadcast();
	}

	public void broadcast() throws GameActionException {
		if (rc.isCoreReady() && index < broadcastQueue.size()) {
			Signal s = broadcastQueue.get(index);
			index++;
			rc.broadcastMessageSignal(s.getMessage()[0], s.getMessage()[1], 2 * rc.getType().sensorRadiusSquared);
		}
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
			try {
				runTurn();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * public void run(){ while (true)Clock.yield(); // what
	 * the hell is this }
	 */
}