package com.example

import org.intellij.lang.annotations.Language

@Language("JSON")
val response = """{
    "data": {
      "viewer": {
        "__typename": "Viewer", 
        "shelf": {
          "__typename": "Shelf",
          "id": "shelf-id",
          "books": [
            {
              "__typename": "Book",
              "id": "id=1",
              "name": "ok-2"
            },
            {
              "__typename": "Book",
              "id": "id=2",
              "name": "ok-2"
            }
          ]
        }
      }
    }
  }"""
