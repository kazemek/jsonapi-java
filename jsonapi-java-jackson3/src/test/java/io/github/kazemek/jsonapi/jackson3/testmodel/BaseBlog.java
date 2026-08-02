package io.github.kazemek.jsonapi.jackson3.testmodel;

public abstract class BaseBlog {

  protected String id;
  protected String name;

  protected BaseBlog() {}

  protected BaseBlog(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
