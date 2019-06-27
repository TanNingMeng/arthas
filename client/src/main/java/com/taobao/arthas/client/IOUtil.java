package com.taobao.arthas.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;

/***
 * This is a utility class providing a reader/writer capability required by the
 * weatherTelnet, rexec, rshell, and rlogin example programs. The only point of
 * the class is to hold the static method readWrite which spawns a reader thread
 * and a writer thread. The reader thread reads from a local input source
 * (presumably stdin) and writes the data to a remote output destination. The
 * writer thread reads from a remote input source and writes to a local output
 * destination. The threads terminate when the remote input source closes.
 ***/

public final class IOUtil {

    public static final void readWrite(final InputStream remoteInput, final OutputStream remoteOutput,
                    final InputStream localInput, final Writer localOutput) {
        Thread reader, writer;

        reader = new Thread() {
            @Override
            public void run() {
                int ch;

                try {
                    while (!interrupted() && (ch = localInput.read()) != -1) {      // 如果不是线程中断，而且读取的数据不是空的
                        remoteOutput.write(ch);                                     // 写入
                        remoteOutput.flush();                                       // 刷新
                    }
                } catch (IOException e) {
                    // e.printStackTrace();                                         // 啥也不处理？
                }
            }
        };

        writer = new Thread() {
            @Override
            public void run() {
                try {
                    InputStreamReader reader = new InputStreamReader(remoteInput);
                    while (true) {                                                  // 不断循环？
                        int singleChar = reader.read();                             //
                        if (singleChar == -1) {
                            break;
                        }
                        localOutput.write(singleChar);                              // 写入
                        localOutput.flush();                                        // 刷新
                    }
                } catch (IOException e) {
                    e.printStackTrace();                                            // 系统退出
                    System.exit(1);
                }
            }
        };

        writer.setPriority(Thread.currentThread().getPriority() + 1);

        /* 上面设置了优先级，下面又设置为"伴随线程" */
        writer.start();
        reader.setDaemon(true);
        reader.start();

        try {
            // TODO 这个是干啥的？搞清楚
            writer.join();
            reader.interrupt();
        } catch (InterruptedException e) {
            // Ignored
        }
    }

}