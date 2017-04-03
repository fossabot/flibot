/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 schors
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package org.schors.flibot.commands;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.streams.Pump;
import org.schors.flibot.FileNameParser;
import org.schors.flibot.Util;
import org.schors.vertx.telegram.bot.api.methods.SendDocument;
import org.schors.vertx.telegram.bot.commands.BotCommand;
import org.schors.vertx.telegram.bot.commands.CommandContext;

import java.io.File;

@BotCommand(regexp = "^/d")
public class DownloadCommand extends FlibotCommand {

    private FileNameParser fileNameParser = new FileNameParser();

    {
        fileNameParser
                .add(new FileNameParser.FileType("mobi") {
                    @Override
                    public String parse(String url) {
                        String[] parts = url.split("/");
                        return parts[parts.length - 2] + "." + parts[parts.length - 1];
                    }
                })
                .add(new FileNameParser.FileType("\\w+\\+zip") {
                    @Override
                    public String parse(String url) {
                        String[] parts = url.split("/");
                        return parts[parts.length - 2] + "." + parts[parts.length - 1] + ".zip";
                    }
                })
                .add(new FileNameParser.FileType("djvu") {
                    @Override
                    public String parse(String url) {
                        String[] parts = url.split("/");
                        return parts[parts.length - 1] + ".djvu";
                    }
                })
                .add(new FileNameParser.FileType("pdf") {
                    @Override
                    public String parse(String url) {
                        String[] parts = url.split("/");
                        return parts[parts.length - 1] + ".pdf";
                    }
                })
                .add(new FileNameParser.FileType("doc") {
                    @Override
                    public String parse(String url) {
                        String[] parts = url.split("/");
                        return parts[parts.length - 1] + ".doc";
                    }
                })
                .add(new FileNameParser.FileType("\\w+\\+rar") {
                    @Override
                    public String parse(String url) {
                        String[] parts = url.split("/");
                        return parts[parts.length - 2] + "." + parts[parts.length - 1] + ".rar";
                    }
                })
        ;
    }

    public DownloadCommand() {
    }

    @Override
    public void execute(String text, CommandContext context) {
        String url = getCache().getIfPresent(Util.normalizeCmd(text));
        if (url != null) {
            download(url, event -> {
                if (event.succeeded()) {
                    sendFile(context, (SendDocument) event.result());
                } else {
                    sendReply(context, "Error happened :(");
                }
            });
        } else {
            sendReply(context, "Expired command");
        }
    }

    private void download(String url, Handler<AsyncResult<Object>> handler) {
        getClient().get(url, res -> {
            if (res.statusCode() == 200) {
                try {
                    File book = File.createTempFile(fileNameParser.parse(url), null);
                    getBot().getVertx().fileSystem().open(book.getAbsolutePath(), new OpenOptions().setWrite(true), event -> {
                        if (event.succeeded()) {
                            Pump.pump(res
                                            .endHandler(done -> {
                                                event.result().close();
                                                handler.handle(Util.result(true, new SendDocument().setDocument(book.getAbsolutePath()).setCaption("book"), null));
                                            })
                                            .exceptionHandler(e -> handler.handle(Util.result(false, null, e))),
                                    event.result())
                                    .start();
                        } else {
                            handler.handle(Util.result(false, null, event.cause()));
                        }
                    });
                } catch (Exception e) {
                    handler.handle(Util.result(false, null, e));
                }
            }
        }).exceptionHandler(e -> handler.handle(Util.result(false, null, e)));
    }

    /*    private void download(String url, Handler<AsyncResult<Object>> handler) {
        vertx.executeBlocking(future -> {
            HttpGet httpGet = new HttpGet(rootOPDS + url);
            try {
                CloseableHttpResponse response = httpclient.execute(httpGet, context);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String fileName = "tmp";
                    if (url.contains("mobi")) {
                        String[] parts = url.split("/");
                        fileName = parts[parts.length - 2] + "." + parts[parts.length - 1] + ".mobi";
                    } else if (url.contains("djvu")) {
                        String[] parts = url.split("/");
                        fileName = parts[parts.length - 2] + "." + parts[parts.length - 1] + ".djvu";
                    } else {
                        String[] parts = url.split("/");
                        fileName = parts[parts.length - 2] + "." + parts[parts.length - 1] + ".zip";
                    }
                    HttpEntity ht = response.getEntity();
                    BufferedHttpEntity buf = new BufferedHttpEntity(ht);
                    File book = File.createTempFile("flibot_" + Long.toHexString(System.currentTimeMillis()), null);
                    buf.writeTo(new FileOutputStream(book));
                    final SendDocument sendDocument = new SendDocument();
                    sendDocument.setNewDocument(book.getAbsolutePath(), fileName);
                    sendDocument.setCaption("book");
                    future.complete(sendDocument);
                }
            } catch (Exception e) {
                log.warn(e, e);
                future.fail(e);
            }
        }, res -> {
            handler.handle(res);
        });
    }*/
}