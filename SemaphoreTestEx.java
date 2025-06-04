package kr.choonie.test.working;

import java.util.concurrent.*;

//세마포어 테스트
//쓰레드 - 각각의 투표자
//공유자원 - 세마포어, 큐(투표소 이름 저장 해 놓음)
class VotePerson extends Thread {

    final static int capacity = 2;
    static Semaphore semaphore = new Semaphore(capacity, true);
    BlockingQueue<String> queue;

    public VotePerson(String name, BlockingQueue<String> queue) {
        super(name);
        this.queue = queue;
    }

    public void run() {

        try {
            semaphore.acquire(); //세마포어 허용치 1개 할당

            Thread.sleep(500);

            String queueName = queue.take(); //큐안에 요소 투표소이름 pop

            System.out.printf("%s 이 %s에서 투표하였습니다. \n",Thread.currentThread().getName(), queueName);

            queue.put(queueName); //동기화 작업 끝내기 전 다시 큐안에 투표소이름 넣어줌
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            semaphore.release(); //세마포어 허용 반납
        }

    }


}


public class SemaphoreTestEx {

    public static void main(String[] args) {


        BlockingQueue<String> queue = new LinkedBlockingQueue<>(3);
        queue.add("투표소 정읍");
        queue.add("투표소 전주");
        queue.add("투표소 익산");
        int length = 100;
        for (int i = 0; i < length; i++) {
            Thread t = new VotePerson("투표자".concat(String.valueOf(i+1)), queue);
            t.start();
        }



    }


}
