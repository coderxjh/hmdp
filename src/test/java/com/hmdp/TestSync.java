package com.hmdp;

/**
 * @author xjh
 * @create 2023-03-17 20:19
 */
public class TestSync {

    public static void main(String[] args) throws InterruptedException {
        Room room = new Room();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                room.increment();
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                room.decrement();
            }
        }, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("a=" + room.getCount());
    }
}

class Room {
    int count;

    void increment() {
        synchronized (this){
            count++;
        }
    }

    void decrement() {
        synchronized (this){
            count--;
        }
    }


    int getCount(){
        return count;
    }
}