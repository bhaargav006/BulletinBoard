import java.io.IOException;

/***
 * Threads run periodically to sync all the servers
 */
public class QuorumSyncThread extends  Thread {

    boolean stop = false;
    @Override
    public void run() {
        while(!stop) {
            try {
                sleep(30000);
                ServerHelper.synch(4);
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
