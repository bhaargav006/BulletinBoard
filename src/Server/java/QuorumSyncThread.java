import java.io.IOException;

public class QuorumSyncThread extends  Thread {

    boolean stop = false;
    @Override
    public void run() {
        while(!stop) {
            try {
                sleep(1000);
                ServerHelper.synch();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void stopSync() {
        stop = true;
    }
}
