package com.github.systeminvecklare.simulation;

public interface IWorldEvent {
	/**
	 * Events happen in time order. If two events have the same timestamp the 
	 * one with the lexographically lowest uniqueId happens first.
	 * 
	 * This ensures an unambigious event order.
	 * @return
	 */
	long getTime();
	String getUniqueId();
	void happen(IWorldEventEnvironment environment);
}
