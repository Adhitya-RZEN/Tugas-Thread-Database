package com.socials.model;

public class Post {
    private int id;
    private String author;
    private String content;
    private String createdAt;
    public Post() {}
    public Post(int id, String author, String content, String createdAt){
        this.id = id; this.author = author; this.content = content; this.createdAt = createdAt;
    }
    public Post(String author, String content){
        this.author = author; this.content = content;
    }
    public int getId(){ return id; }
    public String getAuthor(){ return author; }
    public String getContent(){ return content; }
    public String getCreatedAt(){ return createdAt; }
    public void setId(int id){ this.id = id; }
    public void setCreatedAt(String createdAt){ this.createdAt = createdAt; }
    @Override public String toString(){
        return String.format("#%d %s (%s): %s", id, author, createdAt==null?"-":createdAt, content);
    }
}
