package com.carcalendar.dmdev.pccontrol;

import android.content.AsyncTaskLoader;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ControlActivity extends AppCompatActivity {

    private static String[] operations;
    private Button connect;
    private TextView result;
    private StringBuilder resultBuilder;
    private Spinner commands;
    private Button action;
    private EditText IPText;
    private TextView connectionStatus;
    private EditText additionalData;
    private Button clearBtn;

    private Socket socket = null;
    private ObjectOutputStream toSer = null;
    private ObjectInputStream fromSer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        operations = getResources().getStringArray(R.array.commands);
        result = (TextView) findViewById(R.id.resultTV);
        result.setMovementMethod(new ScrollingMovementMethod());
        resultBuilder = new StringBuilder();
        connect = (Button) findViewById(R.id.connectBtn);
        commands = (Spinner) findViewById(R.id.spinner);
        action = (Button) findViewById(R.id.initiateBtn);
        IPText = (EditText) findViewById(R.id.IPEditText);
        connectionStatus = (TextView) findViewById(R.id.connectionTV);
        additionalData = (EditText)findViewById(R.id.commandData);
        clearBtn = (Button) findViewById(R.id.clearBtn);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Connect().execute();
            }
        });

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new WorkHorse().execute();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.setText("");
                resultBuilder.delete(0,resultBuilder.length());
            }
        });


    }


    private class Connect extends AsyncTask<Void,Void,Void>{

        private String status;
        private String IP;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            IP = IPText.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket = new Socket(IP, 9999);
                toSer = new ObjectOutputStream(socket.getOutputStream());
                fromSer = new ObjectInputStream(socket.getInputStream());
                status ="Connected !";
            } catch (IOException e) {
                e.printStackTrace();
                status ="Conn problem !";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            connectionStatus.setText(status);
        }
    }

    private class WorkHorse extends AsyncTask<Void,Void,Void>{

        String command;
        String commandData;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            command = (String) commands.getSelectedItem();
            commandData = additionalData.getText().toString();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                switch (command) {
                    case "KILLPROCESS":
                        System.out.println("Enter the process to kill ...");
                        String prcsToKill = commandData;
                        System.out.println("Sending to server : " + prcsToKill);
                        toSer.writeUTF(command + " " + prcsToKill);
                        toSer.flush();
                        addToBuilder(fromSer.readUTF());
                        break;
                    case "GETPROCESSES":
                        toSer.writeUTF(command);
                        toSer.flush();
                        ArrayList<String> processes = (ArrayList<String>) fromSer.readObject();
                        resultBuilder.append("Below is list of processes" + "\n");
                        for (int i = 0; i < processes.size(); i++) {
                            resultBuilder.append(processes.get(i));
                        }
                        addToBuilder("\nEnd of list");
                        break;
                    case "HELP":
                        for (String x: operations){
                            resultBuilder.append(x +"\n");
                        }
                        addToBuilder("");
                        break;
                    case "EXIT":
                        finish();
                        break;
                    case "CHECKCONN":
                        try {
                            toSer.writeUTF(command);
                            toSer.flush();
                            addToBuilder(fromSer.readUTF());
                        } catch (IOException ex) {
                            addToBuilder("Server not responding !!!");
                        }
                            break;
                    case "STOPSER":
                        toSer.writeUTF(command);
                        toSer.flush();
                        break;
                    case "FILE":
                        //TODO : next time
                        break;
                    case "FILELIST":
                        //TODO : next time
                        break;
                    case "OPEN":
                        //TODO : next time
                        break;
                    case "SLEEP":
                        toSer.writeUTF(command);
                        toSer.flush();
                        resultBuilder.append("Send to server : " + command +"\n");
                        addToBuilder(fromSer.readUTF());
                        break;
                    case "RESTART":
                        toSer.writeUTF(command);
                        toSer.flush();
                        addToBuilder(fromSer.readUTF() + " check connection !");
                        break;
                    case "SHUTDOWN":
                        toSer.writeUTF(command);
                        toSer.flush();
                        addToBuilder(fromSer.readUTF() + " check connection !");
                        break;
                    case "FILEDEL":
                        // TODO : next time
                        break;
                }
            }catch (IOException e){

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            result.setText(resultBuilder.toString());
        }
    }
    private void addToBuilder(String data){
        resultBuilder.append(data);
        resultBuilder.append("\n");
        resultBuilder.append("Done");
        resultBuilder.append("\n");
    }
}
