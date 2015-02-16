package com.bokwas.server.backgroundtasks;

import java.util.concurrent.LinkedBlockingQueue;


public class TaskManager {

	private static TaskManager instance = null;
	private LinkedBlockingQueue<Task> queue;
	private Thread thread;

	private TaskManager() {
		queue = new LinkedBlockingQueue<Task>();
		thread = new Thread(new TaskConsumer());
		thread.start();
	}

	public static TaskManager getInstance() {
		synchronized (TaskManager.class) {
			if (instance == null) {
				return instance = new TaskManager();
			}
		}
		return instance;
	}

	public void addTask(Task task) {
		queue.offer(task);
		if(!thread.isAlive()) {
			thread = new Thread(new TaskConsumer());
			thread.start();
		}
	}

	class TaskConsumer implements Runnable {

		@Override
		public void run() {
			try {
				Task task = queue.take();
				task.execute();
			} catch (InterruptedException e) {
				if (Thread.interrupted()) {
					Thread.currentThread().interrupt();
				}
				e.printStackTrace();
			}
		}

	}

}