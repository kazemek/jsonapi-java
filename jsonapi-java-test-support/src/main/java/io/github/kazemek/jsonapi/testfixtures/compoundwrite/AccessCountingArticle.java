package io.github.kazemek.jsonapi.testfixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person;
import java.util.List;

/** Mutable article that counts relationship getter reads for traversal-scoped assertions. */
@JsonApiResource(type = "articles")
public final class AccessCountingArticle {

  @JsonApiId public final String id;
  private final Person author;
  private final List<Comment> comments;
  public int authorReads;
  public int commentsReads;

  public AccessCountingArticle(String id, Person author, List<Comment> comments) {
    this.id = id;
    this.author = author;
    this.comments = comments;
  }

  @JsonApiRelationship
  public Person getAuthor() {
    authorReads++;
    return author;
  }

  @JsonApiRelationship
  public List<Comment> getComments() {
    commentsReads++;
    return comments;
  }
}
