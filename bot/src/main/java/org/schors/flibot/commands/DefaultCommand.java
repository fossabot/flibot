/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016  schors
 *
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
import org.schors.flibot.Search;
import org.schors.flibot.SendMessageList;
import org.schors.vertx.telegram.bot.commands.CommandContext;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class DefaultCommand extends FlibotCommand {

    public DefaultCommand() {
        super("no need in regexp");
    }

    @Override
    public void execute(String s, CommandContext commandContext) {
        String userName = commandContext.getUpdate().getMessage().getFrom().getUserName();
        Search search = getSearches().get(userName);
        if (search != null) {
            getSearches().remove(userName);
            switch (search.getSearchType()) {
                case AUTHOR: {
                    getAuthor(s.trim().replaceAll(" ", "+"), event -> {
                        if (event.succeeded()) {
                            sendReply(commandContext.getUpdate(), (SendMessageList) event.result());
                        } else {
                            sendReply(commandContext.getUpdate(), "Error happened :(");
                        }
                    });
                    break;
                }
                case BOOK: {
                    getBook(s.trim().replaceAll(" ", "+"), event -> {
                        if (event.succeeded()) {
                            sendReply(commandContext.getUpdate(), (SendMessageList) event.result());
                        } else {
                            sendReply(commandContext.getUpdate(), "Error happened :(");
                        }
                    });
                    break;
                }
            }
        } else {
            search = new Search();
            search.setToSearch(s.trim().replaceAll(" ", "+"));
            getSearches().put(userName, search);
            KeyboardButton authorButton = new KeyboardButton();
            authorButton.setText("/author");
            KeyboardButton bookButton = new KeyboardButton();
            bookButton.setText("/book");
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(authorButton);
            keyboardRow.add(bookButton);
            List<KeyboardRow> keyboardRows = new ArrayList<KeyboardRow>();
            keyboardRows.add(keyboardRow);
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setKeyboard(keyboardRows);
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setSelective(true);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(commandContext.getUpdate().getMessage().getChatId());
            sendMessage.setReplyMarkup(keyboardMarkup);
            sendMessage.setText("What to search, author or book?");
            sendReply(commandContext.getUpdate(), sendMessage);
        }
    }

    private void getAuthor(String author, Handler<AsyncResult<Object>> handler) {
        doGenericRequest("/opds" + String.format(authorSearch, author), event -> handler.handle(event));
    }

    private void getBook(String book, Handler<AsyncResult<Object>> handler) {
        doGenericRequest("/opds" + String.format(bookSearch, book), event -> handler.handle(event));
    }
}
