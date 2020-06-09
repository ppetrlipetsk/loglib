package com.ppsdevelopment.loglib;

import com.ppsdevelopment.filelib.TextFileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Logger {
    public static final String ERRORLOG= "ErrorLog";
    public static final String APPLOG= "AppLog";

    private static HashMap<String,Logger> loggers;
    private static boolean exitWhenError;

    private final int LINESLIMIT=30;
    BufferedWriter logHandler;
    int linesLimit;
    LinkedList linesBuffer;
    private final String fileName;

    static {
        loggers=new HashMap<>();
        exitWhenError=false;
    }

    public static boolean isExitWhenError() {
        return exitWhenError;
    }

    public static void setExitWhenError(boolean exitWhenError) {
        Logger.exitWhenError = exitWhenError;
    }

    public static Logger getLogger(String loggerName){
        if (loggers.containsKey(loggerName)) return loggers.get(loggerName);
        else
            return null;
    }

    public static void putLineToLogs(String[] logs, String message, boolean echo){
        if (echo) System.out.println(message);
        if (logs!=null){
            for (int i=0;i<logs.length;i++)
            {
                Logger logger=null;
                if (loggers.containsKey(logs[i])) {
                    logger = loggers.get(logs[i]);
                    if (logger != null) logger.putLine(message);
                }
            }
        }
    }

    private Logger(String fileName) {
        try {
            this.logHandler = TextFileWriter.initWriter(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        linesBuffer =new LinkedList();
        this.fileName=fileName;
    }

    public Logger(String loggerName, String fileName, int ll) {
        this(fileName);
        linesLimit=ll;
        loggers.put(loggerName,this);
    }

    public static void closeAll() {
        if (loggers!=null){
            for (Map.Entry<String, Logger> entry : loggers.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void init() throws IOException {
        logHandler=TextFileWriter.initWriter(this.fileName);
    }

    public void put(String message, boolean echo) {
        if (echo) System.out.println(message);
        put(message);
    }

    public void put(String message){
        flushWhenOverLimit();
        linesBuffer.add(message);
    }

    public void putLine(String message,boolean echo) {
        if(echo) System.out.println(message);
        putLine(message);
    }

    public void putLine(String message) {
        flushWhenOverLimit();
        linesBuffer.add((new StringBuilder(message).append("\n")).toString());
    }

    private void flushWhenOverLimit() {
        if (linesBuffer.size()>=linesLimit) flush();
    }

    private void flush()  {
        if (linesBuffer.size()>0) {
            StringBuilder s = new StringBuilder();
            try {
                for (int i = 0; i < linesBuffer.size(); i++) {
                    s.append(linesBuffer.get(i));
                }
                TextFileWriter.write(logHandler, s.toString());
                linesBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
                if (logHandler!=null) closeSilence(false);
                errorProcessing("Ошибка записи файла журнала");
            }
        }
    }


    public void errorProcessing(String message){
        System.out.println(message);
        errorProcessing();
    }

    public void errorProcessing(){
        if (exitWhenError) System.exit(1);
    }

    public void close() throws IOException {
                flush();
        TextFileWriter.close(logHandler);
    }

    public void closeSilence(boolean flush) {
        if (flush) flush();
        try {
            TextFileWriter.close(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
