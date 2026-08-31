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
    title.getAnnotation(JsonApiAttribute) != null

    and:
    RecordComponent body = component(AnnotatedArticleRecord, "body")
    body.getAnnotation(JsonApiAttribute) != null

    and:
    RecordComponent authorId = component(AnnotatedArticleRecord, "authorId")
    authorId.getAnnotation(JsonApiRelationship) != null

    and:
    RecordComponent comments = component(AnnotatedArticleRecord, "comments")
    comments.getAnnotation(JsonApiRelationship) != null
  }

  def "POJO declares resource type and property annotations on fields, getters, and constructor parameters"() {
    expect:
    AnnotatedPersonPojo.getAnnotation(JsonApiResource).type() == "people"

    and:
    Field idField = AnnotatedPersonPojo.getDeclaredField("id")
    idField.getAnnotation(JsonApiId) != null

    and:
    Field nameField = AnnotatedPersonPojo.getDeclaredField("name")
    nameField.getAnnotation(JsonApiAttribute) != null

    and:
    Field emailField = AnnotatedPersonPojo.getDeclaredField("email")
    emailField.getAnnotation(JsonApiAttribute) != null

    and:
    Field articlesField = AnnotatedPersonPojo.getDeclaredField("articleIds")
    articlesField.getAnnotation(JsonApiRelationship) != null

    and:
    Field managerField = AnnotatedPersonPojo.getDeclaredField("managerId")
    managerField.getAnnotation(JsonApiRelationship) != null

    and:
    Method getId = AnnotatedPersonPojo.getDeclaredMethod("getId")
    getId.getAnnotation(JsonApiId) != null

    and:
    Method getName = AnnotatedPersonPojo.getDeclaredMethod("getName")
    getName.getAnnotation(JsonApiAttribute) != null

    and:
    Method getEmail = AnnotatedPersonPojo.getDeclaredMethod("getEmail")
    getEmail.getAnnotation(JsonApiAttribute) != null

    and:
    Method getArticleIds = AnnotatedPersonPojo.getDeclaredMethod("getArticleIds")
    getArticleIds.getAnnotation(JsonApiRelationship) != null

    and:
    Method getManagerId = AnnotatedPersonPojo.getDeclaredMethod("getManagerId")
    getManagerId.getAnnotation(JsonApiRelationship) != null

    and:
    Constructor<?> ctor = AnnotatedPersonPojo.declaredConstructors[0]
    ctor.parameters[0].getAnnotation(JsonApiId) != null
    ctor.parameters[1].getAnnotation(JsonApiAttribute) != null
    ctor.parameters[2].getAnnotation(JsonApiAttribute) != null
    ctor.parameters[3].getAnnotation(JsonApiRelationship) != null
    ctor.parameters[4].getAnnotation(JsonApiRelationship) != null
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
