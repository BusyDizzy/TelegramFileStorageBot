package com.java.model;

public class RabbitQueue {
    public static final String DOC_MESSAGE_UPDATE = "doc_message_update";
    public static final String TEXT_MESSAGE_UPDATE = "text_message_update";

    public static final String ANSWER_MESSAGE = "answer_message";

    // Adding Dead-letter queues
    public static final String DLX_DOC_MESSAGE_UPDATE = "dlx_doc_message_update";
    public static final String DLX_TEXT_MESSAGE_UPDATE = "dlx_text_message_update";
    public static final String DLX_ANSWER_MESSAGE = "dlx_answer_message";
}