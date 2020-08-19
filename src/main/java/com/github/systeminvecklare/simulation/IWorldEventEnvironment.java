package com.github.systeminvecklare.simulation;

public interface IWorldEventEnvironment {
	<T> T getEntity(String id, Class<T> type);
	void removeEntity(String id);
	void addEntity(String id, IPredictable predictable);
}
