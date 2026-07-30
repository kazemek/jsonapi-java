package io.github.kazemek.jsonapi.annotation

import io.github.kazemek.jsonapi.annotation.fixtures.AnnotatedArticleRecord
import io.github.kazemek.jsonapi.annotation.fixtures.AnnotatedPersonPojo
import io.github.kazemek.jsonapi.annotation.fixtures.ResourceBase
import io.github.kazemek.jsonapi.annotation.fixtures.ResourceChild
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.RecordComponent
import spock.lang.Specification

class AnnotationUsageFixtureSpec extends Specification {

  def "record declares resource type and property annotations on record components"() {
    expect:
    AnnotatedArticleRecord.getAnnotation(JsonApiResource).type() == "articles"

    and:
    RecordComponent id = component(AnnotatedArticleRecord, "id")
    id.getAnnotation(JsonApiId) != null

    and:
    RecordComponent title = component(AnnotatedArticleRecord, "title")
    title.getAnnotation(JsonApiAttribute).name() == "headline"

    and:
    RecordComponent body = component(AnnotatedArticleRecord, "body")
    body.getAnnotation(JsonApiAttribute).name() == ""

    and:
    RecordComponent authorId = component(AnnotatedArticleRecord, "authorId")
    authorId.getAnnotation(JsonApiRelationship).name() == "author"

    and:
    RecordComponent comments = component(AnnotatedArticleRecord, "comments")
    comments.getAnnotation(JsonApiRelationship).name() == ""
  }

  def "POJO declares resource type and property annotations on fields, getters, and constructor parameters"() {
    expect:
    AnnotatedPersonPojo.getAnnotation(JsonApiResource).type() == "people"

    and:
    Field idField = AnnotatedPersonPojo.getDeclaredField("id")
    idField.getAnnotation(JsonApiId) != null

    and:
    Field nameField = AnnotatedPersonPojo.getDeclaredField("name")
    nameField.getAnnotation(JsonApiAttribute).name() == "full-name"

    and:
    Field emailField = AnnotatedPersonPojo.getDeclaredField("email")
    emailField.getAnnotation(JsonApiAttribute).name() == ""

    and:
    Field articlesField = AnnotatedPersonPojo.getDeclaredField("articleIds")
    articlesField.getAnnotation(JsonApiRelationship).name() == "articles"

    and:
    Field managerField = AnnotatedPersonPojo.getDeclaredField("managerId")
    managerField.getAnnotation(JsonApiRelationship).name() == ""

    and:
    Method getId = AnnotatedPersonPojo.getDeclaredMethod("getId")
    getId.getAnnotation(JsonApiId) != null

    and:
    Method getName = AnnotatedPersonPojo.getDeclaredMethod("getName")
    getName.getAnnotation(JsonApiAttribute).name() == "full-name"

    and:
    Method getEmail = AnnotatedPersonPojo.getDeclaredMethod("getEmail")
    getEmail.getAnnotation(JsonApiAttribute).name() == ""

    and:
    Method getArticleIds = AnnotatedPersonPojo.getDeclaredMethod("getArticleIds")
    getArticleIds.getAnnotation(JsonApiRelationship).name() == "articles"

    and:
    Method getManagerId = AnnotatedPersonPojo.getDeclaredMethod("getManagerId")
    getManagerId.getAnnotation(JsonApiRelationship).name() == ""

    and:
    Constructor<?> ctor = AnnotatedPersonPojo.declaredConstructors[0]
    ctor.parameters[0].getAnnotation(JsonApiId) != null
    ctor.parameters[1].getAnnotation(JsonApiAttribute).name() == "full-name"
    ctor.parameters[2].getAnnotation(JsonApiAttribute).name() == ""
    ctor.parameters[3].getAnnotation(JsonApiRelationship).name() == "articles"
    ctor.parameters[4].getAnnotation(JsonApiRelationship).name() == ""
  }

  def "JsonApiResource is not inherited by subclasses"() {
    expect:
    ResourceBase.getAnnotation(JsonApiResource).type() == "base"
    ResourceChild.getAnnotation(JsonApiResource) == null
    ResourceChild.getDeclaredAnnotation(JsonApiResource) == null
  }

  private static RecordComponent component(Class<?> type, String name) {
    type.recordComponents.find { it.name == name }
  }
}
