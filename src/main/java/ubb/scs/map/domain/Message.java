package ubb.scs.map.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Message extends Entity<UUID> {
    private UUID id;
    private User from;
    private List<User> to;
    private String text;
    private LocalDateTime date;
    private Message reply;

    public Message(User from, List<User> to, String text, LocalDateTime date) {
        this.id = UUID.randomUUID();
        this.from = from;
        this.to = to;
        this.text = text;
        this.date = date;
        this.reply = null;
    }
    public Message(User from, List<User> to, String text) {
        this.id = UUID.randomUUID();
        this.from = from;
        this.to = to;
        this.text = text;
        this.date = LocalDateTime.now();
        this.reply = null;
    }

    public UUID getId() { return id; }

    public User getFrom() { return from; }

    public List<User> getTo() { return to; }

    public String getText() { return text; }

    public LocalDateTime getDate() { return date; }

    public Message getReply() { return reply; }

    @Override
    public void setId(UUID id) { this.id = id; }

    public void setFrom(User from) { this.from = from; }

    public void setTo(List<User> to) { this.to = to; }

    public void setText(String text) { this.text = text; }

    public void setDate(LocalDateTime date) { this.date = date; }

    public void setReply(Message reply) { this.reply = reply; }
}
