package com.jakesmommy;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class LeaderElection implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int TIME_OUT = 3000;
    private ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException {
        LeaderElection LeaderElection = new LeaderElection();
        LeaderElection.connectToZookeeper();
        LeaderElection.run();
        LeaderElection.close();

    }

    public void close() throws InterruptedException {
        System.out.println("--- close ---");
        this.zooKeeper.close(3000);
    }

    public void run() throws InterruptedException {
        // main program to wait and listen
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, TIME_OUT,this);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        Event.EventType eventType = watchedEvent.getType();

        switch (eventType){
            case None:
                if(watchedEvent.getState()==Event.KeeperState.SyncConnected){
                    System.out.println("Zookeeper connected");
                } else {
                    // if terminated notify the held main thread so it can proceed to the next instruction.
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper Event");
                        zooKeeper.notifyAll();
                    }
                }
        }


    }
}