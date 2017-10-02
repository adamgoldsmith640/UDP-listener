package meh.yetanother;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    private static final int PORT= 8888;
    private static final int TIMEOUT_MS = 500;
    boolean listen = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> listItems = new ArrayList<>();
        ListView listview = (ListView) findViewById(R.id.lvMain);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listview.setAdapter(adapter);

        start();

        //just to show these parts are in different threads
        String temp = Thread.currentThread().getName();
        output("onCreate: " + temp);
    }

    public void stop(View view) {
        listen = false;
    }

    public void start(View view) {
        start();
    }

    public void start() {
        listen = true;
        UpdateView task = new UpdateView();
        task.execute();
    }

    protected void output(final String message) {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                adapter.add(message);
                adapter.notifyDataSetChanged();
            }
        }));
    }



    private class UpdateView extends AsyncTask<URL, Integer, Long> {
        protected Long doInBackground(URL... urls) {
            byte[] buf = new byte[1500];

            try {
                DatagramSocket socket = new DatagramSocket(PORT);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                while(listen) {
                    socket.receive(packet);
                    output(new String(packet.getData(), 0, packet.getLength()));
                    output("after");

                    try {
                        Thread.sleep(2000);
                    }
                    catch(InterruptedException e) {
                        output("Interrupted Exception: " + e.toString() );
                        e.printStackTrace();
                    }
                }
            }
            catch(IOException e) {
                output("IO Exception: " + e.toString() );
                e.printStackTrace();
            }
            catch(Exception e) {
                output("Exception: " + e.toString() );
                e.printStackTrace();
            }

            return 0L;
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }
    }
}
