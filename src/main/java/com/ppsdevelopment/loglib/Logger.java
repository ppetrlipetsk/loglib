package com.ppsdevelopment.loglib;


import com.ppsdevelopment.filelib.TextFileWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *  Ведение логов работы программы.
 *  Позволяет выводить лог в файл или на консоль.
 *  Чтобы использовать методы класса, надо создать экземпляр, указать имя файла, имя лога и размер буффера.
 *
 */

public class Logger implements AutoCloseable{
    private final static HashMap<String,Logger> loggers;
    private static boolean exitWhenError;
    private static final int LINESLIMIT=30;
    private int linesLimit;
    private BufferedWriter logHandler;
    private LinkedList linesBuffer;
    private final String fileName;

    static {
        loggers=new HashMap<>();
        exitWhenError=false;
    }

    /**
     * @return Возвращает размер буфера журнала (количество строк)
     */
    public int getLinesLimit() {
        return linesLimit;
    }

    /**
     * Устанавливает размер буфера журнала
     * @param linesLimit количество строк
     */

    public void setLinesLimit(int linesLimit) {
        this.linesLimit = linesLimit;
    }

    /**
     * возвращает имя файла журнала
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Флаг, устанавливает правило обработки ошибок ввода-вывода.
     * Если false, то, при возникновении ошибки, она будет перехвачена и работа логгера продолжится.
     * Если true, то при возникновении ошибки, будет возбуждено исключение и работа логгера прервана.
     * @return Если false, то, при возникновении ошибки, она будет перехвачена и работа логгера продолжится.
     *         Если true, то при возникновении ошибки, будет возбуждено исключение и работа логгера прервана.
     */
    public static boolean isExitWhenError() {
        return exitWhenError;
    }

    /**
     * Флаг, устанавливает правило обработки ошибок ввода-вывода.
     * Если false, то, при возникновении ошибки, она будет перехвачена и работа логгера продолжится.
     * Если true, то при возникновении ошибки, будет возбуждено исключение и работа логгера прервана.
     */
    public static void setExitWhenError(boolean exitWhenError) {
        Logger.exitWhenError = exitWhenError;
    }

    /**
     * Возвращает экземпляр логгера с именем loggerName
     * @param loggerName - имя логгера
     * @return Возвращает экземпляр логгера с именем loggerName
     */
    public static Logger getLogger(String loggerName){
        return loggers.getOrDefault(loggerName, null);
    }
//
//    public static void appLog(String message, boolean echo){
//        if (echo) System.out.println(message);
//        putLineToLog(APPLOG,message);
//    }

    /**
     * Помещает строку message в буффер логгеров, указанных в параметре logs.
     * Если echo=true, вывод дублируется на консоль.
     * @param logs - массив, содержат имена логгеров
     * @param message - строка вывода
     * @param echo - если true, то вывод дублируется на консоль
     */
    public static void putLineToLogs(String[] logs, String message, boolean echo){
        if (echo) System.out.println(message);
        if (logs!=null){
            for (String log : logs) {
                putLineToLog(log, message);
            }
        }
    }

    /**
     * Помещает строку message в буффер логгеров, указанных в параметре logs.
     * Если echo=true, вывод дублируется на консоль.
     * @param log - имя лога
     * @param message - строка сообщения
     * @param echo - если true, то дублирует вывод на консоль
     */
    public static void putLineToLog(String log, String message, boolean echo) {
        if (echo) System.out.println(message);
        putLineToLog(log,message);
    }


    /**
     * Помещает строку message в буффер логгеров, указанных в параметре logs.     *
     * @param log - имя лога
     * @param message - строка сообщения
     */

    public static void putLineToLog(String log, String message) {
        Logger logger;
        if (loggers.containsKey(log)) {
            logger = loggers.get(log);
            if (logger != null) logger.putLine(message);
        }
    }

    private Logger(String fileName) throws IOException {
        linesLimit=LINESLIMIT;
        this.logHandler = TextFileWriter.initWriter(fileName);
        linesBuffer =new LinkedList();
        this.fileName=fileName;
    }

    /**
     * Конструктор, создает экземпляр класса Logger.
     * @param loggerName - имя лога
     * @param fileName - имя файла
     * @param ll - лимит буффера
     */
    public Logger(String loggerName, String fileName, int ll) throws IOException {
        this(fileName);
        linesLimit=ll;
        loggers.put(loggerName,this);
    }

    public static void closeAll() throws IOException {
        if (loggers!=null){
            for (Map.Entry<String, Logger> entry : loggers.entrySet()) {
                    entry.getValue().close();
            }
        }
    }

    public void init() throws IOException {
        logHandler=TextFileWriter.initWriter(this.fileName);
    }

    /**
     * Помещает строку в буффер логгера.
     * @param message - строка сообщения
     * @param echo - если true, то дублировать вывод на консоль.
     */
    public void put(String message, boolean echo) {
        if (echo) System.out.println(message);
        put(message);
    }

    /**
     * Помещает строку в буффер логгера.
     * @param message - строка сообщения
     */

    public void put(String message){
        flushWhenOverLimit();
        linesBuffer.add(message);
    }

    /**
     * Помещает строку в буффер логгера.
     * @param message - строка сообщения
     * @param echo - если true, то дублировать вывод на консоль.
     */
    public void putLine(String message,boolean echo) {
        if(echo) System.out.println(message);
        putLine(message);
    }

    /**
     * Помещает сообщение с символом перевода строки, в буффер логгера.
     * @param message - строка сообщения
     */
    public void putLine(String message) {
        flushWhenOverLimit();
        //linesBuffer.add((new StringBuilder(message).append("\n")).toString());
        put(message + "\n");
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
                errorProcessing();
            }
        }
    }

    private void errorProcessing(){
        System.out.println("Ошибка записи файла журнала");
        if (exitWhenError) System.exit(1);
    }

    /**
     * Закрывает логгер, при этом сбрасывает содержимое буффера в поток вывода.
     * @throws IOException
     */
    public void close() throws IOException {
        flush();
        TextFileWriter.close(logHandler);
    }
    /**
     * Закрывает логгер, при этом сбрасывает содержимое буффера в поток вывода, если параметр flush=true.
     * @param flush- если true, сбрасывает содержимое буфера в поток вывода, перед тем, как закрыть поток.
     * @throws IOException
     */
    public void closeSilence(boolean flush) {
        if (flush) flush();
        try {
            TextFileWriter.close(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод позволяет создать логгеры на основе их имен.
     * @param loggers - имена логгеров
     * @param linesLimit - размер строкового буффера
     * @throws IOException
     */
    public static void createLoggers(String[] loggers, int linesLimit) throws IOException {
        for(String log:loggers){
            new Logger(log,log,linesLimit);
        }
    }

}
